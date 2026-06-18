package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentStatusHistoryResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;
import Vaccination.Management.System.model.entity.Appointment;
import Vaccination.Management.System.model.entity.AppointmentStatusHistory;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.FacilityCapacity;
import Vaccination.Management.System.model.entity.MedicalStaffProfile;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import Vaccination.Management.System.model.entity.VaccinationRecord;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.repository.AppointmentRepository;
import Vaccination.Management.System.repository.AppointmentStatusHistoryRepository;
import Vaccination.Management.System.repository.FacilityCapacityRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.repository.MedicalStaffProfileRepository;
import Vaccination.Management.System.repository.UserRepository;
import Vaccination.Management.System.repository.VaccinationRecordRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImp implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository statusHistoryRepository;
    private final VaccineRepository vaccineRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityCapacityRepository facilityCapacityRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccineDoseScheduleRepository vaccineDoseScheduleRepository;
    private final UserRepository userRepository;
    private final MedicalStaffProfileRepository medicalStaffProfileRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, Long citizenId) {

        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (citizen.getRole() != UserRole.CITIZEN) {
            throw new AppException(ErrorCode.CITIZEN_ROLE_REQUIRED);
        }

        Vaccine vaccine = vaccineRepository.findById(request.getVaccineId())
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        if (!vaccine.isActive()) {
            throw new AppException(ErrorCode.VACCINE_INACTIVE);
        }

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        if (!facility.isActive()) {
            throw new AppException(ErrorCode.FACILITY_INACTIVE);
        }

        FacilityCapacity capacity = facilityCapacityRepository
                .findActiveCapacity(facility.getId(), request.getPreferredDate())
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_CAPACITY_NOT_FOUND));

        long bookedSlots = appointmentRepository.countBookedSlots(
                request.getFacilityId(),
                request.getPreferredDate()
        );
        if (bookedSlots >= capacity.getMaxSlotsPerDay()) {
            throw new AppException(ErrorCode.APPOINTMENT_SLOT_FULL);
        }

        boolean hasDuplicate = appointmentRepository.existsScheduled(
                citizenId,
                request.getVaccineId()
        );
        if (hasDuplicate) {
            throw new AppException(ErrorCode.APPOINTMENT_DUPLICATE);
        }

        Optional<VaccinationRecord> lastRecord = vaccinationRecordRepository
                .findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
                        citizenId, request.getVaccineId(), RecordStatus.VALID);

        int nextDoseNumber = lastRecord
                .map(r -> r.getDoseSchedule().getDoseNumber() + 1)
                .orElse(1);

        VaccineDoseSchedule nextSchedule = vaccineDoseScheduleRepository
                .findByVaccineIdAndDoseNumber(request.getVaccineId(), nextDoseNumber)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINATION_SERIES_COMPLETED));

        lastRecord.ifPresent(r -> {
            if (nextSchedule.getDaysAfterPrevious() != null) {
                LocalDate lastDate = r.getAdministeredAt().toLocalDate();
                LocalDate earliest = lastDate.plusDays(nextSchedule.getDaysAfterPrevious());
                if (request.getPreferredDate().isBefore(earliest)) {
                    throw new AppException(ErrorCode.APPOINTMENT_DOSE_INTERVAL);
                }
            }
        });

        Appointment appointment = Appointment.builder()
                .citizen(citizen)
                .vaccine(vaccine)
                .facility(facility)
                .doseSchedule(nextSchedule)
                .preferredDate(request.getPreferredDate())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointment = appointmentRepository.save(appointment);

        return toResponse(appointment);
    }

    @Override
    public List<AppointmentSummary> getMyAppointments(Long citizenId) {
        return appointmentRepository.findByCitizenId(citizenId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        AppointmentStatus previous = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CANCELLED);

        appointment = appointmentRepository.save(appointment);
        recordHistory(appointment, previous, AppointmentStatus.CANCELLED, reason, actor);

        return toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse markNoShow(Long appointmentId, Long staffId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        AppointmentStatus previous = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.NO_SHOW);

        appointment = appointmentRepository.save(appointment);
        recordHistory(appointment, previous, AppointmentStatus.NO_SHOW, null, staff);

        return toResponse(appointment);
    }

    @Override
    public List<AppointmentSummary> getTodayAppointments(Long staffId) {
        MedicalStaffProfile profile = medicalStaffProfileRepository.findByUserId(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return appointmentRepository
                .findByFacilityIdAndDate(profile.getFacility().getId(), LocalDate.now())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<AppointmentStatusHistoryResponse> getAppointmentHistory(Long appointmentId) {
        appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        return statusHistoryRepository
                .findByAppointmentIdOrderByChangedAtAsc(appointmentId)
                .stream()
                .map(h -> AppointmentStatusHistoryResponse.builder()
                        .id(h.getId())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .reason(h.getReason())
                        .changedByName(h.getChangedBy().getFullName())
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();
    }

    // --- Private helpers ---

    private void recordHistory(Appointment appointment, AppointmentStatus from,
                               AppointmentStatus to, String reason, User changedBy) {
        statusHistoryRepository.save(
                AppointmentStatusHistory.builder()
                        .appointment(appointment)
                        .fromStatus(from)
                        .toStatus(to)
                        .reason(reason)
                        .changedBy(changedBy)
                        .build()
        );
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .citizenId(a.getCitizen().getId())
                .citizenName(a.getCitizen().getFullName())
                .vaccineId(a.getVaccine().getId())
                .vaccineName(a.getVaccine().getName())
                .facilityId(a.getFacility().getId())
                .facilityName(a.getFacility().getName())
                .preferredDate(a.getPreferredDate())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private AppointmentSummary toSummary(Appointment a) {
        return AppointmentSummary.builder()
                .id(a.getId())
                .vaccineName(a.getVaccine().getName())
                .facilityName(a.getFacility().getName())
                .preferredDate(a.getPreferredDate())
                .status(a.getStatus())
                .build();
    }
}
