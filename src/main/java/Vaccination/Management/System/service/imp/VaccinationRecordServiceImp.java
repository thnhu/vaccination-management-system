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

        // Validate batch vaccine matches appointment vaccine
        if (!batch.getVaccine().getId().equals(appointment.getVaccine().getId())) {
            throw new AppException(ErrorCode.RECORD_APPOINTMENT_MISMATCH);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        VaccinationRecord record = VaccinationRecord.builder()
                .appointment(appointment)
                .citizen(appointment.getCitizen())
                .vaccine(appointment.getVaccine())
                .facility(appointment.getFacility())
                .batch(batch)
                .doseNumber(request.getDoseNumber())
                .administeredAt(now)
                .administeredBy(staff)
                .status(RecordStatus.VALID)
                .dataSource(DataSource.SYSTEM)
                .verifiedBy(staff)
                .verifiedAt(now)
                .build();

        vaccinationRecordRepository.save(record);

        batch.setRemaining(batch.getRemaining() - 1);
        if (batch.getRemaining() == 0) {
            batch.setStatus(BatchStatus.DEPLETED);
        }
        vaccineBatchRepository.save(batch);

        appointment.setStatus(AppointmentStatus.COMPLETED);
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

    @Override
    @Transactional
    public VaccinationRecordResponse verifyRecord(Long recordId, Long staffId) {
        VaccinationRecord record = vaccinationRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        if (record.getVerifiedBy() != null) {
            throw new AppException(ErrorCode.RECORD_ALREADY_VERIFIED);
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        record.setVerifiedBy(staff);
        record.setVerifiedAt(LocalDateTime.now());

        return toResponse(vaccinationRecordRepository.save(record));
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
                .status(r.getStatus())
                .replacesRecordId(r.getReplacesRecord() != null ? r.getReplacesRecord().getId() : null)
                .dataSource(r.getDataSource())
                .verifiedById(r.getVerifiedBy() != null ? r.getVerifiedBy().getId() : null)
                .verifiedByName(r.getVerifiedBy() != null ? r.getVerifiedBy().getFullName() : null)
                .verifiedAt(r.getVerifiedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
