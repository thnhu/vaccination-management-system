package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinationrecord.RecordVaccinationRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationReactionResponse;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.model.entity.*;
import Vaccination.Management.System.model.enums.*;
import Vaccination.Management.System.repository.*;
import Vaccination.Management.System.service.imp.VaccinationRecordServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaccinationRecordServiceTest {

    @Mock private VaccinationRecordRepository vaccinationRecordRepository;
    @Mock private VaccinationReactionRepository vaccinationReactionRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;
    @Mock private VaccineBatchRepository vaccineBatchRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private VaccinationRecordServiceImp vaccinationRecordService;

    private User citizen;
    private User staff;
    private Vaccine vaccine;
    private Facility facility;
    private VaccineDoseSchedule doseSchedule;
    private VaccineBatch batch;
    private Appointment confirmedAppointment;
    private RecordVaccinationRequest request;

    @BeforeEach
    void setUp() {
        citizen = User.builder().id(1L).fullName("Nguyễn Văn A").role(UserRole.CITIZEN).build();
        staff = User.builder().id(2L).fullName("Bác sĩ B").role(UserRole.MEDICAL_STAFF).build();
        vaccine = Vaccine.builder().id(10L).name("Vắc xin X").active(true).build();
        facility = Facility.builder().id(20L).name("Cơ sở Y").active(true).facilityType(FacilityType.VACCINATION_CENTER).build();
        doseSchedule = VaccineDoseSchedule.builder().id(100L).vaccine(vaccine).doseNumber(1).build();
        batch = VaccineBatch.builder()
                .id(30L).vaccine(vaccine).facility(facility)
                .batchNumber("BATCH-001").quantity(100).remaining(50)
                .status(BatchStatus.ACTIVE)
                .expiryDate(LocalDate.of(2027, 1, 1))
                .receivedDate(LocalDate.of(2026, 1, 1))
                .build();
        confirmedAppointment = Appointment.builder()
                .id(5L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(doseSchedule).status(AppointmentStatus.SCHEDULED).build();

        request = new RecordVaccinationRequest();
        request.setAppointmentId(5L);
        request.setBatchId(30L);
    }

    // recordVaccination

    @Test
    void recordVaccination_success() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(batch));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(vaccinationRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vaccineBatchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentStatusHistoryRepository.save(any())).thenReturn(new AppointmentStatusHistory());

        VaccinationRecordResponse response = vaccinationRecordService.recordVaccination(request, 2L);

        assertThat(response.getCitizenId()).isEqualTo(1L);
        assertThat(response.getCitizenName()).isEqualTo("Nguyễn Văn A");
        assertThat(response.getVaccineId()).isEqualTo(10L);
        assertThat(response.getVaccineName()).isEqualTo("Vắc xin X");
        assertThat(response.getFacilityId()).isEqualTo(20L);
        assertThat(response.getFacilityName()).isEqualTo("Cơ sở Y");
        assertThat(response.getBatchNumber()).isEqualTo("BATCH-001");
        assertThat(response.getDoseNumber()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(RecordStatus.VALID);
        assertThat(response.getCorrectionReason()).isNull();
        assertThat(response.getReplacesRecordId()).isNull();

        assertThat(batch.getRemaining()).isEqualTo(49);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(confirmedAppointment.getStatus()).isEqualTo(AppointmentStatus.VACCINATED);

        verify(vaccinationRecordRepository).save(any(VaccinationRecord.class));
        verify(vaccineBatchRepository).save(batch);
        verify(appointmentRepository).save(confirmedAppointment);
        verify(appointmentStatusHistoryRepository).save(any(AppointmentStatusHistory.class));
    }

    @Test
    void recordVaccination_depletesBatch_whenLastDose() {
        batch.setRemaining(1);

        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(batch));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(vaccinationRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vaccineBatchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentStatusHistoryRepository.save(any())).thenReturn(new AppointmentStatusHistory());

        vaccinationRecordService.recordVaccination(request, 2L);

        assertThat(batch.getRemaining()).isEqualTo(0);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.DEPLETED);
    }

    @Test
    void recordVaccination_savesHistoryWithCorrectTransition() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(batch));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(vaccinationRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vaccineBatchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<AppointmentStatusHistory> captor = ArgumentCaptor.forClass(AppointmentStatusHistory.class);
        when(appointmentStatusHistoryRepository.save(captor.capture())).thenReturn(new AppointmentStatusHistory());

        vaccinationRecordService.recordVaccination(request, 2L);

        AppointmentStatusHistory history = captor.getValue();
        assertThat(history.getFromStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(history.getToStatus()).isEqualTo(AppointmentStatus.VACCINATED);
        assertThat(history.getChangedBy().getId()).isEqualTo(2L);
    }

    @Test
    void recordVaccination_throwsAppointmentNotFound_whenAppointmentMissing() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_NOT_FOUND));
    }

    @Test
    void recordVaccination_throwsAppointmentInvalidStatus_whenNotScheduled() {
        Appointment pendingAppointment = Appointment.builder()
                .id(5L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(doseSchedule).status(AppointmentStatus.CANCELLED).build();
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(pendingAppointment));

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_INVALID_STATUS));
    }

    @Test
    void recordVaccination_throwsBatchNotFound_whenBatchMissing() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_NOT_FOUND));
    }

    @Test
    void recordVaccination_throwsBatchUnavailable_whenBatchNotActive() {
        VaccineBatch depletedBatch = VaccineBatch.builder()
                .id(30L).vaccine(vaccine).facility(facility)
                .batchNumber("BATCH-001").quantity(100).remaining(0)
                .status(BatchStatus.DEPLETED)
                .expiryDate(LocalDate.of(2027, 1, 1))
                .receivedDate(LocalDate.of(2026, 1, 1))
                .build();
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(depletedBatch));

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_UNAVAILABLE));
    }

    @Test
    void recordVaccination_throwsBatchDepleted_whenRemainingIsZero() {
        batch.setRemaining(0);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BATCH_DEPLETED));
    }

    @Test
    void recordVaccination_throwsRecordAppointmentMismatch_whenBatchVaccineDiffers() {
        Vaccine otherVaccine = Vaccine.builder().id(99L).name("Vắc xin khác").active(true).build();
        VaccineBatch mismatchBatch = VaccineBatch.builder()
                .id(30L).vaccine(otherVaccine).facility(facility)
                .batchNumber("BATCH-999").quantity(100).remaining(50)
                .status(BatchStatus.ACTIVE)
                .expiryDate(LocalDate.of(2027, 1, 1))
                .receivedDate(LocalDate.of(2026, 1, 1))
                .build();
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(mismatchBatch));

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RECORD_APPOINTMENT_MISMATCH));
    }

    @Test
    void recordVaccination_throwsUserNotFound_whenStaffMissing() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(confirmedAppointment));
        when(vaccineBatchRepository.findById(30L)).thenReturn(Optional.of(batch));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccinationRecordService.recordVaccination(request, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // getVaccinationRecords

    @Test
    void getVaccinationRecords_success_asCitizen_ownRecords() {
        VaccinationRecord record = VaccinationRecord.builder()
                .id(1L).citizen(citizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.VALID).build();
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(record));

        List<VaccinationRecordResponse> result =
                vaccinationRecordService.getVaccinationRecords(1L, 1L, "ROLE_CITIZEN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCitizenId()).isEqualTo(1L);
        assertThat(result.get(0).getVaccineName()).isEqualTo("Vắc xin X");
        assertThat(result.get(0).getBatchNumber()).isEqualTo("BATCH-001");
        assertThat(result.get(0).getDoseNumber()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RecordStatus.VALID);
    }

    @Test
    void getVaccinationRecords_throwsAccessDenied_whenCitizenAccessesOtherRecords() {
        assertThatThrownBy(() ->
                vaccinationRecordService.getVaccinationRecords(2L, 1L, "ROLE_CITIZEN"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));

        verify(vaccinationRecordRepository, never()).findByCitizenId(any());
    }

    @Test
    void getVaccinationRecords_success_asMedicalStaff_anyRecords() {
        User otherCitizen = User.builder().id(3L).fullName("Trần Thị B").role(UserRole.CITIZEN).build();
        VaccinationRecord record = VaccinationRecord.builder()
                .id(2L).citizen(otherCitizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.VALID).build();
        when(vaccinationRecordRepository.findByCitizenId(3L)).thenReturn(List.of(record));

        List<VaccinationRecordResponse> result =
                vaccinationRecordService.getVaccinationRecords(3L, 2L, "ROLE_MEDICAL_STAFF");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCitizenId()).isEqualTo(3L);
        assertThat(result.get(0).getCitizenName()).isEqualTo("Trần Thị B");
    }

    @Test
    void getVaccinationRecords_returnsEmptyList_whenNoRecords() {
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of());

        List<VaccinationRecordResponse> result =
                vaccinationRecordService.getVaccinationRecords(1L, 1L, "ROLE_CITIZEN");

        assertThat(result).isEmpty();
    }

    // invalidateRecord

    @Test
    void invalidateRecord_success() {
        VaccinationRecord record = VaccinationRecord.builder()
                .id(10L).citizen(citizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.VALID).build();

        VaccinationRecord invalidated = VaccinationRecord.builder()
                .id(10L).citizen(citizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.INVALID)
                .correctionReason("Nhập sai thông tin")
                .build();

        when(vaccinationRecordRepository.findById(10L))
                .thenReturn(Optional.of(record))
                .thenReturn(Optional.of(invalidated));
        when(vaccinationReactionRepository.findByRecordId(10L)).thenReturn(List.of());

        VaccinationRecordResponse response =
                vaccinationRecordService.invalidateRecord(10L, 2L, "Nhập sai thông tin");

        assertThat(response.getStatus()).isEqualTo(RecordStatus.INVALID);
        assertThat(response.getCorrectionReason()).isEqualTo("Nhập sai thông tin");
        verify(vaccinationRecordRepository).updateStatusAndReason(10L, RecordStatus.INVALID, "Nhập sai thông tin");
    }

    @Test
    void invalidateRecord_throwsException_whenRecordNotFound() {
        when(vaccinationRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccinationRecordService.invalidateRecord(99L, 2L, "lý do"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RECORD_NOT_FOUND));
    }

    @Test
    void invalidateRecord_throwsException_whenAlreadyInvalid() {
        VaccinationRecord alreadyInvalid = VaccinationRecord.builder()
                .id(11L).citizen(citizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.INVALID).build();

        when(vaccinationRecordRepository.findById(11L)).thenReturn(Optional.of(alreadyInvalid));

        assertThatThrownBy(() -> vaccinationRecordService.invalidateRecord(11L, 2L, "lý do"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RECORD_ALREADY_INVALID));
    }

    // toResponse — with reactions

    @Test
    void getVaccinationRecords_withReactions_mapsAllReactionFields() {
        VaccinationRecord record = VaccinationRecord.builder()
                .id(20L).citizen(citizen).vaccine(vaccine).facility(facility)
                .batch(batch).doseSchedule(doseSchedule)
                .status(RecordStatus.VALID).build();

        VaccinationReaction reaction = VaccinationReaction.builder()
                .id(100L)
                .record(record)
                .symptom("Sốt nhẹ")
                .severity(ReactionLevel.MILD)
                .onsetAt(LocalDate.of(2026, 6, 10).atStartOfDay())
                .resolvedAt(LocalDate.of(2026, 6, 11).atStartOfDay())
                .reportSource(ReportSource.STAFF_OBSERVED)
                .build();

        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(record));
        when(vaccinationReactionRepository.findByRecordId(20L)).thenReturn(List.of(reaction));

        List<VaccinationRecordResponse> result =
                vaccinationRecordService.getVaccinationRecords(1L, 1L, "ROLE_CITIZEN");

        assertThat(result).hasSize(1);
        List<VaccinationReactionResponse> reactions = result.get(0).getReactions();
        assertThat(reactions).hasSize(1);
        VaccinationReactionResponse r = reactions.get(0);
        assertThat(r.getId()).isEqualTo(100L);
        assertThat(r.getSymptom()).isEqualTo("Sốt nhẹ");
        assertThat(r.getSeverity()).isEqualTo(ReactionLevel.MILD);
        assertThat(r.getReportSource()).isEqualTo(ReportSource.STAFF_OBSERVED);
    }
}
