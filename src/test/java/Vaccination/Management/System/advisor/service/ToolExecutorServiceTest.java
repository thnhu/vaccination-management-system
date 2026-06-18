package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;
import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.service.AvailableSlotService;
import Vaccination.Management.System.service.RecommendationService;
import Vaccination.Management.System.service.VaccinationRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolExecutorServiceTest {

    @Mock private VaccinationRecordService vaccinationRecordService;
    @Mock private RecommendationService recommendationService;
    @Mock private AvailableSlotService availableSlotService;

    @InjectMocks
    private ToolExecutorService toolExecutorService;

    @Test
    void execute_getVaccinationHistory_returnsSerialized() {
        VaccinationRecordResponse record = VaccinationRecordResponse.builder()
                .id(1L).citizenId(10L).vaccineName("Vắc xin X")
                .doseNumber(1).status(RecordStatus.VALID)
                .administeredAt(LocalDateTime.of(2026, 1, 1, 9, 0))
                .reactions(List.of())
                .build();
        when(vaccinationRecordService.getVaccinationRecords(eq(10L), eq(10L), eq("ROLE_CITIZEN")))
                .thenReturn(List.of(record));

        String result = toolExecutorService.execute(10L, "get_vaccination_history", "{}");

        assertThat(result).contains("records");
        assertThat(result).contains("Vắc xin X");
    }

    @Test
    void execute_getVaccinationHistory_returnsEmptyMessage_whenNoRecords() {
        when(vaccinationRecordService.getVaccinationRecords(any(), any(), any()))
                .thenReturn(List.of());

        String result = toolExecutorService.execute(10L, "get_vaccination_history", "{}");

        assertThat(result).contains("Chưa có lịch sử tiêm");
    }

    @Test
    void execute_getRecommendedSchedule_returnsSerialized() {
        RecommendationResponse rec = RecommendationResponse.builder()
                .vaccineId(10L).vaccineName("Vắc xin X")
                .currentDoseNumber(1).nextDoseNumber(2)
                .earliestDate(LocalDate.of(2026, 2, 1))
                .status("PENDING").build();
        when(recommendationService.getRecommendations(10L)).thenReturn(List.of(rec));

        String result = toolExecutorService.execute(10L, "get_recommended_schedule", "{}");

        assertThat(result).contains("recommendations");
        assertThat(result).contains("PENDING");
    }

    @Test
    void execute_getRecommendedSchedule_returnsEmpty_whenNoRecords() {
        when(recommendationService.getRecommendations(10L)).thenReturn(List.of());

        String result = toolExecutorService.execute(10L, "get_recommended_schedule", "{}");

        assertThat(result).contains("Chưa có lịch sử tiêm");
    }

    @Test
    void execute_getAvailableSlots_returnsSerialized() {
        AvailableSlotResponse slot = AvailableSlotResponse.builder()
                .facilityId(1L).facilityName("Cơ sở A")
                .date(LocalDate.of(2027, 1, 10))
                .maxSlots(10).bookedSlots(3).availableSlots(7)
                .build();
        when(availableSlotService.getAvailableSlots(eq(1L), any(), any()))
                .thenReturn(List.of(slot));

        String inputJson = "{\"facility_id\":1,\"start_date\":\"2027-01-10\",\"end_date\":\"2027-01-10\"}";
        String result = toolExecutorService.execute(10L, "get_available_slots", inputJson);

        assertThat(result).contains("slots");
        assertThat(result).contains("Cơ sở A");
    }

    @Test
    void execute_getAvailableSlots_returnsNoSlotMessage_whenEmpty() {
        when(availableSlotService.getAvailableSlots(any(), any(), any())).thenReturn(List.of());

        String inputJson = "{\"facility_id\":1,\"start_date\":\"2027-01-10\",\"end_date\":\"2027-01-10\"}";
        String result = toolExecutorService.execute(10L, "get_available_slots", inputJson);

        assertThat(result).contains("Không có slot trống");
    }

    @Test
    void execute_unknownTool_returnsErrorJson() {
        String result = toolExecutorService.execute(10L, "unknown_tool", "{}");
        assertThat(result).contains("Unknown tool");
    }

    @Test
    void execute_serviceThrowsException_returnsErrorJson() {
        when(vaccinationRecordService.getVaccinationRecords(any(), any(), any()))
                .thenThrow(new RuntimeException("DB connection error"));

        String result = toolExecutorService.execute(10L, "get_vaccination_history", "{}");

        assertThat(result).contains("error");
    }
}
