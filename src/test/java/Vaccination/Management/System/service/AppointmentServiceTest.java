package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentStatusHistoryResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;
import Vaccination.Management.System.model.entity.*;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import Vaccination.Management.System.model.enums.FacilityType;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.repository.*;
import Vaccination.Management.System.repository.MedicalStaffProfileRepository;
import Vaccination.Management.System.service.imp.AppointmentServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AppointmentStatusHistoryRepository statusHistoryRepository;
    @Mock private VaccineRepository vaccineRepository;
    @Mock private FacilityRepository facilityRepository;
    @Mock private FacilityCapacityRepository facilityCapacityRepository;
    @Mock private VaccinationRecordRepository vaccinationRecordRepository;
    @Mock private VaccineDoseScheduleRepository vaccineDoseScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private MedicalStaffProfileRepository medicalStaffProfileRepository;

    @InjectMocks
    private AppointmentServiceImp appointmentService;

    private User citizen;
    private User staff;
    private Vaccine vaccine;
    private Facility facility;
    private FacilityCapacity capacity;
    private VaccineDoseSchedule dose1;
    private VaccineDoseSchedule dose2;
    private CreateAppointmentRequest request;
    private static final LocalDate PREFERRED_DATE = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        citizen = User.builder().id(1L).role(UserRole.CITIZEN).fullName("Nguyễn Văn A").build();
        staff = User.builder().id(2L).role(UserRole.MEDICAL_STAFF).fullName("Bác sĩ B").build();

        vaccine = Vaccine.builder().id(10L).name("Vắc xin X").active(true).build();

        facility = Facility.builder().id(20L).name("Cơ sở Y").active(true)
                .facilityType(FacilityType.VACCINATION_CENTER).build();

        capacity = FacilityCapacity.builder().id(30L).facility(facility)
                .maxSlotsPerDay(50).effectiveFrom(LocalDate.of(2026, 1, 1)).build();

        dose1 = VaccineDoseSchedule.builder().id(100L).vaccine(vaccine)
                .doseNumber(1).daysAfterPrevious(null).build();
        dose2 = VaccineDoseSchedule.builder().id(101L).vaccine(vaccine)
                .doseNumber(2).daysAfterPrevious(30).build();

        request = new CreateAppointmentRequest();
        request.setVaccineId(10L);
        request.setFacilityId(20L);
        request.setPreferredDate(PREFERRED_DATE);
    }

    //  createAppointment

    @Test
    void createAppointment_success_firstDose() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, PREFERRED_DATE)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, PREFERRED_DATE)).thenReturn(0L);
        when(appointmentRepository.existsScheduled(1L, 10L)).thenReturn(false);
        when(vaccinationRecordRepository.findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
                1L, 10L, RecordStatus.VALID)).thenReturn(Optional.empty());
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 1))
                .thenReturn(Optional.of(dose1));

        Appointment savedAppointment = Appointment.builder()
                .id(999L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.SCHEDULED).createdAt(LocalDateTime.now()).build();
        when(appointmentRepository.save(any())).thenReturn(savedAppointment);

        AppointmentResponse response = appointmentService.createAppointment(request, 1L);

        assertThat(response.getId()).isEqualTo(999L);
        assertThat(response.getCitizenId()).isEqualTo(1L);
        assertThat(response.getVaccineId()).isEqualTo(10L);
        assertThat(response.getFacilityId()).isEqualTo(20L);
        assertThat(response.getPreferredDate()).isEqualTo(PREFERRED_DATE);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(statusHistoryRepository, never()).save(any(AppointmentStatusHistory.class));
    }

    @Test
    void createAppointment_success_secondDose_afterValidInterval() {
        LocalDate lastAdminDate = LocalDate.of(2026, 4, 15);
        VaccinationRecord lastRecord = VaccinationRecord.builder()
                .id(50L).citizen(citizen).vaccine(vaccine)
                .doseSchedule(dose1)
                .administeredAt(lastAdminDate.atStartOfDay())
                .status(RecordStatus.VALID).build();

        LocalDate preferredDate2 = lastAdminDate.plusDays(30);
        request.setPreferredDate(preferredDate2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, preferredDate2)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, preferredDate2)).thenReturn(5L);
        when(appointmentRepository.existsScheduled(1L, 10L)).thenReturn(false);
        when(vaccinationRecordRepository.findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
                1L, 10L, RecordStatus.VALID)).thenReturn(Optional.of(lastRecord));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));

        Appointment saved = Appointment.builder()
                .id(1000L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose2).preferredDate(preferredDate2)
                .status(AppointmentStatus.SCHEDULED).createdAt(LocalDateTime.now()).build();
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponse response = appointmentService.createAppointment(request, 1L);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void createAppointment_throwsUserNotFound_whenCitizenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void createAppointment_throwsCitizenRoleRequired_whenUserIsStaff() {
        User nonCitizen = User.builder().id(1L).role(UserRole.MEDICAL_STAFF).fullName("Nhân viên").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(nonCitizen));

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CITIZEN_ROLE_REQUIRED));
    }

    @Test
    void createAppointment_throwsVaccineNotFound_whenVaccineMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void createAppointment_throwsVaccineInactive_whenVaccineDisabled() {
        Vaccine inactive = Vaccine.builder().id(10L).name("Vắc xin X").active(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_INACTIVE));
    }

    @Test
    void createAppointment_throwsFacilityNotFound_whenFacilityMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Test
    void createAppointment_throwsFacilityInactive_whenFacilityDisabled() {
        Facility inactive = Facility.builder().id(20L).name("Cơ sở Y").active(false)
                .facilityType(FacilityType.VACCINATION_CENTER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_INACTIVE));
    }

    @Test
    void createAppointment_throwsCapacityNotFound_whenNoConfig() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, PREFERRED_DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_CAPACITY_NOT_FOUND));
    }

    @Test
    void createAppointment_throwsSlotFull_whenAllSlotsBooked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, PREFERRED_DATE)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, PREFERRED_DATE)).thenReturn(50L);

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_SLOT_FULL));
    }

    @Test
    void createAppointment_throwsDuplicate_whenPendingAppointmentExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, PREFERRED_DATE)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, PREFERRED_DATE)).thenReturn(0L);
        when(appointmentRepository.existsScheduled(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_DUPLICATE));
    }

    @Test
    void createAppointment_throwsSeriesCompleted_whenNoDoseScheduleForNextDose() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, PREFERRED_DATE)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, PREFERRED_DATE)).thenReturn(0L);
        when(appointmentRepository.existsScheduled(1L, 10L)).thenReturn(false);
        when(vaccinationRecordRepository.findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
                1L, 10L, RecordStatus.VALID)).thenReturn(Optional.empty());
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINATION_SERIES_COMPLETED));
    }

    @Test
    void createAppointment_throwsDoseInterval_whenBookedTooEarly() {
        LocalDate lastAdminDate = LocalDate.of(2026, 4, 15);
        VaccinationRecord lastRecord = VaccinationRecord.builder()
                .id(50L).citizen(citizen).vaccine(vaccine)
                .doseSchedule(dose1)
                .administeredAt(lastAdminDate.atStartOfDay())
                .status(RecordStatus.VALID).build();

        LocalDate tooEarlyDate = lastAdminDate.plusDays(10);
        request.setPreferredDate(tooEarlyDate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(20L, tooEarlyDate)).thenReturn(Optional.of(capacity));
        when(appointmentRepository.countBookedSlots(20L, tooEarlyDate)).thenReturn(0L);
        when(appointmentRepository.existsScheduled(1L, 10L)).thenReturn(false);
        when(vaccinationRecordRepository.findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
                1L, 10L, RecordStatus.VALID)).thenReturn(Optional.of(lastRecord));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));

        assertThatThrownBy(() -> appointmentService.createAppointment(request, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_DOSE_INTERVAL));
    }

    // ── getMyAppointments

    @Test
    void getMyAppointments_returnsListMappedToSummary() {
        Appointment a = Appointment.builder()
                .id(1L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.SCHEDULED).build();

        when(appointmentRepository.findByCitizenId(1L)).thenReturn(List.of(a));

        List<AppointmentSummary> result = appointmentService.getMyAppointments(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getVaccineName()).isEqualTo("Vắc xin X");
        assertThat(result.get(0).getFacilityName()).isEqualTo("Cơ sở Y");
        assertThat(result.get(0).getPreferredDate()).isEqualTo(PREFERRED_DATE);
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void getMyAppointments_returnsEmptyList_whenNone() {
        when(appointmentRepository.findByCitizenId(1L)).thenReturn(List.of());

        List<AppointmentSummary> result = appointmentService.getMyAppointments(1L);

        assertThat(result).isEmpty();
    }

    // cancelAppointment

    @Test
    void cancelAppointment_success_fromScheduled() {
        Appointment scheduled = Appointment.builder()
                .id(6L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.SCHEDULED).createdAt(LocalDateTime.now()).build();

        when(appointmentRepository.findById(6L)).thenReturn(Optional.of(scheduled));
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statusHistoryRepository.save(any())).thenReturn(new AppointmentStatusHistory());

        AppointmentResponse response = appointmentService.cancelAppointment(6L, 1L, "Bận");

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(statusHistoryRepository).save(argThat(h ->
                h.getToStatus() == AppointmentStatus.CANCELLED && "Bận".equals(h.getReason())));
    }

    @Test
    void cancelAppointment_throwsInvalidStatus_whenAlreadyCancelled() {
        Appointment cancelled = Appointment.builder()
                .id(8L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.CANCELLED).build();

        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(8L, 1L, null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_INVALID_STATUS));
    }

    //  markNoShow

    @Test
    void markNoShow_success() {
        Appointment scheduled = Appointment.builder()
                .id(11L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.SCHEDULED).createdAt(LocalDateTime.now()).build();

        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(scheduled));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statusHistoryRepository.save(any())).thenReturn(new AppointmentStatusHistory());

        AppointmentResponse response = appointmentService.markNoShow(11L, 2L);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    @Test
    void markNoShow_throwsInvalidStatus_whenCancelled() {
        Appointment cancelled = Appointment.builder()
                .id(11L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.CANCELLED).build();

        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> appointmentService.markNoShow(11L, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_INVALID_STATUS));
    }

    // ── getTodayAppointments ──────────────────────────────────────────────────

    @Test
    void getTodayAppointments_success() {
        MedicalStaffProfile profile = MedicalStaffProfile.builder()
                .id(1L).user(staff).facility(facility).staffCode("ST001").build();
        when(medicalStaffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));

        Appointment appt = Appointment.builder()
                .id(50L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(LocalDate.now())
                .status(AppointmentStatus.SCHEDULED).build();
        when(appointmentRepository.findByFacilityIdAndDate(20L, LocalDate.now()))
                .thenReturn(List.of(appt));

        List<AppointmentSummary> result = appointmentService.getTodayAppointments(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
        assertThat(result.get(0).getVaccineName()).isEqualTo("Vắc xin X");
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void getTodayAppointments_throwsException_whenStaffProfileNotFound() {
        when(medicalStaffProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getTodayAppointments(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // ── getAppointmentHistory ─────────────────────────────────────────────────

    @Test
    void getAppointmentHistory_success() {
        Appointment appt = Appointment.builder()
                .id(60L).citizen(citizen).vaccine(vaccine).facility(facility)
                .doseSchedule(dose1).preferredDate(PREFERRED_DATE)
                .status(AppointmentStatus.VACCINATED).build();
        when(appointmentRepository.findById(60L)).thenReturn(Optional.of(appt));

        LocalDateTime changedAt = LocalDateTime.of(2026, 6, 1, 9, 0);
        AppointmentStatusHistory h1 = AppointmentStatusHistory.builder()
                .id(1L)
                .fromStatus(AppointmentStatus.SCHEDULED)
                .toStatus(AppointmentStatus.VACCINATED)
                .reason(null)
                .changedBy(staff)
                .changedAt(changedAt)
                .build();
        when(statusHistoryRepository.findByAppointmentIdOrderByChangedAtAsc(60L))
                .thenReturn(List.of(h1));

        List<AppointmentStatusHistoryResponse> result =
                appointmentService.getAppointmentHistory(60L);

        assertThat(result).hasSize(1);
        AppointmentStatusHistoryResponse r = result.get(0);
        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getFromStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(r.getToStatus()).isEqualTo(AppointmentStatus.VACCINATED);
        assertThat(r.getChangedByName()).isEqualTo("Bác sĩ B");
        assertThat(r.getChangedAt()).isEqualTo(changedAt);
    }

    @Test
    void getAppointmentHistory_throwsException_whenAppointmentNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentHistory(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPOINTMENT_NOT_FOUND));
    }
}
