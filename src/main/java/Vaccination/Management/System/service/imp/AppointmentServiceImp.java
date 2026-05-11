package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;
import Vaccination.Management.System.model.entity.Appointment;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import Vaccination.Management.System.repository.AppointmentRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.repository.UserRepository;
import Vaccination.Management.System.repository.VaccinationRecordRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImp implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VaccineRepository vaccineRepository;
    private final FacilityRepository facilityRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, Long citizenId) {

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

        long bookedSlots = appointmentRepository.countBookedSlots(
                request.getFacilityId(),
                request.getPreferredDate()
        );
        if (bookedSlots >= facility.getMaxSlotsPerDay()) {
            throw new AppException(ErrorCode.APPOINTMENT_SLOT_FULL);
        }

        boolean hasDuplicate = appointmentRepository.existsPendingOrConfirmed(
                citizenId,
                request.getVaccineId()
        );
        if (hasDuplicate) {
            throw new AppException(ErrorCode.APPOINTMENT_DUPLICATE);
        }

        if (vaccine.getDaysBetweenDoses() != null) {
            vaccinationRecordRepository
                    .findLatestDoseDate(citizenId, request.getVaccineId())
                    .ifPresent(lastDate -> {
                        LocalDate earliest = lastDate.plusDays(vaccine.getDaysBetweenDoses());
                        if (request.getPreferredDate().isBefore(earliest)) {
                            throw new AppException(ErrorCode.APPOINTMENT_DOSE_INTERVAL);
                        }
                    });
        }

        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Appointment appointment = Appointment.builder()
                .citizen(citizen)
                .vaccine(vaccine)
                .facility(facility)
                .preferredDate(request.getPreferredDate())
                .status(AppointmentStatus.PENDING)
                .createdBy(citizenId)
                .build();

        return toResponse(appointmentRepository.save(appointment));
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
    public AppointmentResponse confirmAppointment(Long appointmentId, Long staffId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(LocalDateTime.now());
        appointment.setUpdatedBy(staffId);

        return toResponse(appointmentRepository.save(appointment));
    }

    // --- Mappers ---

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
                .cancelReason(a.getCancelReason())
                .confirmedAt(a.getConfirmedAt())
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