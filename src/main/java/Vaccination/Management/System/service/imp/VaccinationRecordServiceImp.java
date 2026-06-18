package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinationrecord.CreateVaccinationReactionRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.RecordVaccinationRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationReactionResponse;
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
    private final VaccinationReactionRepository vaccinationReactionRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VaccinationRecordResponse recordVaccination(RecordVaccinationRequest request, Long staffId) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
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

        // Validate batch vaccine matches appointment vaccine
        if (!batch.getVaccine().getId().equals(appointment.getVaccine().getId())) {
            throw new AppException(ErrorCode.RECORD_APPOINTMENT_MISMATCH);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VaccinationRecord record = VaccinationRecord.builder()
                .appointment(appointment)
                .citizen(appointment.getCitizen())
                .vaccine(appointment.getVaccine())
                .facility(appointment.getFacility())
                .batch(batch)
                .doseSchedule(appointment.getDoseSchedule())
                .administeredAt(LocalDateTime.now())
                .status(RecordStatus.VALID)
                .build();

        vaccinationRecordRepository.save(record);

        if (request.getReactions() != null && !request.getReactions().isEmpty()) {
            for (CreateVaccinationReactionRequest r : request.getReactions()) {
                VaccinationReaction reaction = VaccinationReaction.builder()
                        .record(record)
                        .symptom(r.getSymptom())
                        .severity(r.getSeverity())
                        .onsetAt(r.getOnsetAt())
                        .resolvedAt(r.getResolvedAt())
                        .reportSource(r.getReportSource())
                        .build();
                vaccinationReactionRepository.save(reaction);
            }
        }

        batch.setRemaining(batch.getRemaining() - 1);
        if (batch.getRemaining() == 0) {
            batch.setStatus(BatchStatus.DEPLETED);
        }
        vaccineBatchRepository.save(batch);

        AppointmentStatus previousStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.VACCINATED);
        appointmentRepository.save(appointment);

        appointmentStatusHistoryRepository.save(
                AppointmentStatusHistory.builder()
                        .appointment(appointment)
                        .fromStatus(previousStatus)
                        .toStatus(AppointmentStatus.VACCINATED)
                        .reason(null)
                        .changedBy(staff)
                        .build()
        );

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

    @Override
    @Transactional
    public VaccinationRecordResponse invalidateRecord(Long recordId, Long staffId, String reason) {
        VaccinationRecord record = vaccinationRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        if (record.getStatus() == RecordStatus.INVALID) {
            throw new AppException(ErrorCode.RECORD_ALREADY_INVALID);
        }

        vaccinationRecordRepository.updateStatusAndReason(recordId, RecordStatus.INVALID, reason);

        VaccinationRecord updated = vaccinationRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        return toResponse(updated);
    }

    // --- Mapper ---

    private VaccinationRecordResponse toResponse(VaccinationRecord r) {
        List<VaccinationReactionResponse> reactions = vaccinationReactionRepository
                .findByRecordId(r.getId())
                .stream()
                .map(reaction -> VaccinationReactionResponse.builder()
                        .id(reaction.getId())
                        .symptom(reaction.getSymptom())
                        .severity(reaction.getSeverity())
                        .onsetAt(reaction.getOnsetAt())
                        .resolvedAt(reaction.getResolvedAt())
                        .reportSource(reaction.getReportSource())
                        .reportedAt(reaction.getReportedAt())
                        .build())
                .toList();

        return VaccinationRecordResponse.builder()
                .id(r.getId())
                .citizenId(r.getCitizen().getId())
                .citizenName(r.getCitizen().getFullName())
                .vaccineId(r.getVaccine().getId())
                .vaccineName(r.getVaccine().getName())
                .facilityId(r.getFacility().getId())
                .facilityName(r.getFacility().getName())
                .batchNumber(r.getBatch().getBatchNumber())
                .doseNumber(r.getDoseSchedule().getDoseNumber())
                .administeredAt(r.getAdministeredAt())
                .status(r.getStatus())
                .correctionReason(r.getCorrectionReason())
                .replacesRecordId(r.getReplacesRecord() != null ? r.getReplacesRecord().getId() : null)
                .createdAt(r.getCreatedAt())
                .reactions(reactions)
                .build();
    }
}
