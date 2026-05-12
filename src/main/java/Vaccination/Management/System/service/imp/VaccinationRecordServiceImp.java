package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinationrecord.RecordVaccinationRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.model.entity.*;
import Vaccination.Management.System.model.enums.*;
import Vaccination.Management.System.repository.*;
import Vaccination.Management.System.service.VaccinationRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccinationRecordServiceImp implements VaccinationRecordService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VaccinationRecordResponse recordVaccination(RecordVaccinationRequest request, Long staffId) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }

        VaccineBatch batch = vaccineBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));

        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new AppException(ErrorCode.BATCH_UNAVAILABLE);
        }

        if (batch.getRemaining() <= 0) {
            throw new AppException(ErrorCode.BATCH_DEPLETED);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VaccinationRecord record = VaccinationRecord.builder()
                .appointment(appointment)
                .citizen(appointment.getCitizen())
                .vaccine(appointment.getVaccine())
                .facility(appointment.getFacility())
                .batch(batch)
                .doseNumber(request.getDoseNumber())
                .administeredAt(LocalDateTime.now())
                .administeredBy(staff)
                .reactionLevel(ReactionLevel.NONE)
                .reactionNote(request.getReactionNote())
                .status(RecordStatus.VALID)
                .dataSource(DataSource.SYSTEM)
                .verified(true)
                .createdBy(staffId)
                .build();

        vaccinationRecordRepository.save(record);

        batch.setRemaining(batch.getRemaining() - 1);
        if (batch.getRemaining() == 0) {
            batch.setStatus(BatchStatus.DEPLETED);
        }
        vaccineBatchRepository.save(batch);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedBy(staffId);
        appointmentRepository.save(appointment);

        return toResponse(record);
    }

    @Override
    public List<VaccinationRecordResponse> getVaccinationRecords(Long citizenId,
                                                                 Long requesterId,
                                                                 String requesterRole) {
        if (requesterRole.equals("ROLE_CITIZEN") && !requesterId.equals(citizenId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return vaccinationRecordRepository.findByCitizenId(citizenId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Mapper ---

    private VaccinationRecordResponse toResponse(VaccinationRecord r) {
        return VaccinationRecordResponse.builder()
                .id(r.getId())
                .citizenId(r.getCitizen().getId())
                .citizenName(r.getCitizen().getFullName())
                .vaccineId(r.getVaccine().getId())
                .vaccineName(r.getVaccine().getName())
                .facilityId(r.getFacility().getId())
                .facilityName(r.getFacility().getName())
                .batchNumber(r.getBatch().getBatchNumber())
                .doseNumber(r.getDoseNumber())
                .administeredAt(r.getAdministeredAt())
                .administeredByName(r.getAdministeredBy().getFullName())
                .reactionLevel(r.getReactionLevel())
                .reactionNote(r.getReactionNote())
                .status(r.getStatus())
                .dataSource(r.getDataSource())
                .verified(r.isVerified())
                .createdAt(r.getCreatedAt())
                .build();
    }
}