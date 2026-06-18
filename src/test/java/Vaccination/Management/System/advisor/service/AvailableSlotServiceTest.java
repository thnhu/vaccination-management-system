package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.FacilityCapacity;
import Vaccination.Management.System.model.enums.FacilityType;
import Vaccination.Management.System.repository.AppointmentRepository;
import Vaccination.Management.System.repository.FacilityCapacityRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.service.imp.AvailableSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableSlotServiceTest {

    @Mock private FacilityRepository facilityRepository;
    @Mock private FacilityCapacityRepository facilityCapacityRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AvailableSlotServiceImpl availableSlotService;

    private Facility facility;

    @BeforeEach
    void setUp() {
        facility = Facility.builder().id(1L).name("Cơ sở A")
                .facilityType(FacilityType.VACCINATION_CENTER).active(true).build();
    }

    private FacilityCapacity capacity(int maxSlots) {
        return FacilityCapacity.builder()
                .id(1L).facility(facility)
                .maxSlotsPerDay(maxSlots)
                .effectiveFrom(LocalDate.now().minusDays(30))
                .build();
    }

    @Test
    void getAvailableSlots_returnsCorrectSlots_whenSlotsAvailable() {
        LocalDate start = LocalDate.of(2027, 1, 10);
        LocalDate end = LocalDate.of(2027, 1, 10);

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(1L), any())).thenReturn(Optional.of(capacity(10)));
        when(appointmentRepository.countBookedSlots(eq(1L), any())).thenReturn(3L);

        List<AvailableSlotResponse> result = availableSlotService.getAvailableSlots(1L, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaxSlots()).isEqualTo(10);
        assertThat(result.get(0).getBookedSlots()).isEqualTo(3L);
        assertThat(result.get(0).getAvailableSlots()).isEqualTo(7L);
    }

    @Test
    void getAvailableSlots_returnsZeroAvailable_whenFullyBooked() {
        LocalDate date = LocalDate.of(2027, 1, 10);

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(1L), any())).thenReturn(Optional.of(capacity(5)));
        when(appointmentRepository.countBookedSlots(eq(1L), any())).thenReturn(5L);

        List<AvailableSlotResponse> result = availableSlotService.getAvailableSlots(1L, date, date);

        assertThat(result.get(0).getAvailableSlots()).isEqualTo(0L);
    }

    @Test
    void getAvailableSlots_skipsDay_whenNoCapacityConfigured() {
        LocalDate start = LocalDate.of(2027, 1, 10);
        LocalDate end = LocalDate.of(2027, 1, 11);

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(1L), eq(start)))
                .thenReturn(Optional.empty());
        when(facilityCapacityRepository.findActiveCapacity(eq(1L), eq(end)))
                .thenReturn(Optional.of(capacity(10)));
        when(appointmentRepository.countBookedSlots(eq(1L), eq(end))).thenReturn(2L);

        List<AvailableSlotResponse> result = availableSlotService.getAvailableSlots(1L, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(end);
    }

    @Test
    void getAvailableSlots_returnsMultipleDays() {
        LocalDate start = LocalDate.of(2027, 1, 10);
        LocalDate end = LocalDate.of(2027, 1, 12);

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(1L), any())).thenReturn(Optional.of(capacity(10)));
        when(appointmentRepository.countBookedSlots(any(), any())).thenReturn(2L);

        List<AvailableSlotResponse> result = availableSlotService.getAvailableSlots(1L, start, end);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDate()).isEqualTo(start);
        assertThat(result.get(2).getDate()).isEqualTo(end);
    }

    @Test
    void getAvailableSlots_throwsFacilityNotFound_whenFacilityMissing() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                availableSlotService.getAvailableSlots(99L, LocalDate.now(), LocalDate.now()))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }
}
