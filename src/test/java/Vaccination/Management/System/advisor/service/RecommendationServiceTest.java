package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;
import Vaccination.Management.System.model.entity.*;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.repository.VaccinationRecordRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.service.imp.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private VaccinationRecordRepository vaccinationRecordRepository;
    @Mock private VaccineDoseScheduleRepository vaccineDoseScheduleRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Vaccine vaccine;
    private VaccineDoseSchedule dose1;
    private VaccineDoseSchedule dose2;
    private User citizen;

    @BeforeEach
    void setUp() {
        vaccine = Vaccine.builder().id(10L).name("Vắc xin X").active(true).build();
        dose1 = VaccineDoseSchedule.builder().id(1L).vaccine(vaccine).doseNumber(1)
                .daysAfterPrevious(null).build();
        dose2 = VaccineDoseSchedule.builder().id(2L).vaccine(vaccine).doseNumber(2)
                .daysAfterPrevious(30).build();
        citizen = User.builder().id(1L).build();
    }

    private VaccinationRecord record(int doseNumber, LocalDateTime administeredAt) {
        VaccineDoseSchedule schedule = doseNumber == 1 ? dose1 : dose2;
        return VaccinationRecord.builder()
                .id((long) doseNumber)
                .citizen(citizen)
                .vaccine(vaccine)
                .doseSchedule(schedule)
                .administeredAt(administeredAt)
                .status(RecordStatus.VALID)
                .build();
    }

    @Test
    void getRecommendations_returnsEmpty_whenNoPriorRecords() {
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_returnsPending_whenIntervalMet() {
        LocalDateTime administered = LocalDateTime.now().minusDays(31);
        VaccinationRecord r = record(1, administered);
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(r));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(result.get(0).getNextDoseNumber()).isEqualTo(2);
    }

    @Test
    void getRecommendations_returnsIntervalNotMet_whenTooEarly() {
        LocalDateTime administered = LocalDateTime.now().minusDays(10);
        VaccinationRecord r = record(1, administered);
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(r));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("INTERVAL_NOT_MET");
        assertThat(result.get(0).getEarliestDate())
                .isEqualTo(administered.toLocalDate().plusDays(30));
    }

    @Test
    void getRecommendations_returnsSeriesCompleted_whenNoNextDose() {
        VaccinationRecord r = record(2, LocalDateTime.now().minusDays(40));
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(r));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 3))
                .thenReturn(Optional.empty());

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("SERIES_COMPLETED");
        assertThat(result.get(0).getNextDoseNumber()).isNull();
    }

    @Test
    void getRecommendations_ignoresInvalidRecords() {
        VaccinationRecord validRecord = record(1, LocalDateTime.now().minusDays(31));
        VaccinationRecord invalidRecord = VaccinationRecord.builder()
                .id(99L).citizen(citizen).vaccine(vaccine).doseSchedule(dose1)
                .administeredAt(LocalDateTime.now().minusDays(5))
                .status(RecordStatus.INVALID)
                .build();
        when(vaccinationRecordRepository.findByCitizenId(1L))
                .thenReturn(List.of(validRecord, invalidRecord));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentDoseNumber()).isEqualTo(1);
    }

    @Test
    void getRecommendations_picksHighestDose_forSameVaccine() {
        VaccinationRecord r1 = record(1, LocalDateTime.now().minusDays(60));
        VaccinationRecord r2 = record(2, LocalDateTime.now().minusDays(40));
        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(r1, r2));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 3))
                .thenReturn(Optional.empty());

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentDoseNumber()).isEqualTo(2);
        assertThat(result.get(0).getStatus()).isEqualTo("SERIES_COMPLETED");
    }

    @Test
    void getRecommendations_returnsMultiple_forDifferentVaccines() {
        Vaccine vaccine2 = Vaccine.builder().id(20L).name("Vắc xin Y").active(true).build();
        VaccineDoseSchedule dose1_v2 = VaccineDoseSchedule.builder().id(3L).vaccine(vaccine2)
                .doseNumber(1).daysAfterPrevious(null).build();
        VaccineDoseSchedule dose2_v2 = VaccineDoseSchedule.builder().id(4L).vaccine(vaccine2)
                .doseNumber(2).daysAfterPrevious(21).build();

        VaccinationRecord r1 = record(1, LocalDateTime.now().minusDays(35));
        VaccinationRecord r2 = VaccinationRecord.builder()
                .id(10L).citizen(citizen).vaccine(vaccine2).doseSchedule(dose1_v2)
                .administeredAt(LocalDateTime.now().minusDays(25))
                .status(RecordStatus.VALID).build();

        when(vaccinationRecordRepository.findByCitizenId(1L)).thenReturn(List.of(r1, r2));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(10L, 2))
                .thenReturn(Optional.of(dose2));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(20L, 2))
                .thenReturn(Optional.of(dose2_v2));

        List<RecommendationResponse> result = recommendationService.getRecommendations(1L);

        assertThat(result).hasSize(2);
    }
}
