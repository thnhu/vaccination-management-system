package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;
import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.service.AvailableSlotService;
import Vaccination.Management.System.service.RecommendationService;
import Vaccination.Management.System.service.VaccinationRecordService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutorService {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final VaccinationRecordService vaccinationRecordService;
    private final RecommendationService recommendationService;
    private final AvailableSlotService availableSlotService;

    public String execute(Long citizenId, String toolName, String inputJson) {
        return switch (toolName) {
            case "get_vaccination_history" -> runGetVaccinationHistory(citizenId);
            case "get_recommended_schedule" -> runGetRecommendedSchedule(citizenId);
            case "get_available_slots" -> runGetAvailableSlots(inputJson);
            default -> "{\"error\":\"Unknown tool: " + toolName + "\"}";
        };
    }

    private String runGetVaccinationHistory(Long citizenId) {
        try {
            List<VaccinationRecordResponse> records =
                    vaccinationRecordService.getVaccinationRecords(citizenId, citizenId, "ROLE_CITIZEN");
            if (records.isEmpty()) return "{\"records\":[],\"message\":\"Chưa có lịch sử tiêm\"}";
            return MAPPER.writeValueAsString(Map.of("records", records));
        } catch (Exception e) {
            return "{\"error\":\"Không thể lấy lịch sử tiêm: " + sanitize(e.getMessage()) + "\"}";
        }
    }

    private String runGetRecommendedSchedule(Long citizenId) {
        try {
            List<RecommendationResponse> recs = recommendationService.getRecommendations(citizenId);
            if (recs.isEmpty()) return "{\"recommendations\":[],\"message\":\"Chưa có lịch sử tiêm để tính khuyến nghị\"}";
            return MAPPER.writeValueAsString(Map.of("recommendations", recs));
        } catch (Exception e) {
            return "{\"error\":\"Không thể tính lịch khuyến nghị: " + sanitize(e.getMessage()) + "\"}";
        }
    }

    private String runGetAvailableSlots(String inputJson) {
        try {
            Map<String, Object> input = MAPPER.readValue(inputJson, new TypeReference<>() {});
            Long facilityId = Long.valueOf(input.get("facility_id").toString());
            LocalDate startDate = LocalDate.parse((String) input.get("start_date"));
            LocalDate endDate = LocalDate.parse((String) input.get("end_date"));

            List<AvailableSlotResponse> slots =
                    availableSlotService.getAvailableSlots(facilityId, startDate, endDate);
            if (slots.isEmpty()) return "{\"slots\":[],\"message\":\"Không có slot trống trong khoảng thời gian này\"}";
            return MAPPER.writeValueAsString(Map.of("slots", slots));
        } catch (Exception e) {
            return "{\"error\":\"Không thể lấy thông tin slot: " + sanitize(e.getMessage()) + "\"}";
        }
    }

    private String sanitize(String msg) {
        if (msg == null) return "unknown error";
        return msg.replace("\"", "'").replace("\n", " ");
    }
}
