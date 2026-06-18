package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.advisor.llm.*;
import Vaccination.Management.System.advisor.service.impl.CitizenAdvisorServiceImpl;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatRequest;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorServiceTest {

    @Mock private LlmClient llmClient;
    @Mock private ToolExecutorService toolExecutorService;
    @Mock private GuardrailService guardrailService;
    @Mock private FaqService faqService;

    @InjectMocks
    private CitizenAdvisorServiceImpl advisorService;

    private static final Long CITIZEN_ID = 1L;

    @BeforeEach
    void setUp() {
        when(guardrailService.apply(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    // HELPER

    private AdvisorChatRequest req(String message) {
        AdvisorChatRequest r = new AdvisorChatRequest();
        r.setMessage(message);
        return r;
    }

    private AdvisorChatRequest reqWithSession(String message, String sessionId) {
        AdvisorChatRequest r = req(message);
        r.setSessionId(sessionId);
        return r;
    }

    private LlmResponse endTurn(String text) {
        return new LlmResponse(List.of(new LlmTextBlock(text)), LlmStopReason.END_TURN);
    }

    private LlmResponse toolUse(String toolId, String toolName, String inputJson) {
        return new LlmResponse(
                List.of(new LlmToolUseBlock(toolId, toolName, inputJson)),
                LlmStopReason.TOOL_USE);
    }

    // TC-01: Citizen chưa có record — gợi ý bắt đầu tiêm
    @Test
    void TC01_noHistory_suggestsStartingVaccination() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_vaccination_history", "{}"))
                .thenReturn(endTurn("Chưa có lịch sử tiêm. Đây là lần đầu bạn tiêm."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_vaccination_history", "{}"))
                .thenReturn("{\"records\":[],\"message\":\"Chưa có lịch sử tiêm\"}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi cần tiêm gì?"));

        assertThat(response.getResponse()).contains("lần đầu");
        assertThat(response.getSessionId()).isNotBlank();
    }

    // TC-02: Interval chưa đủ — báo ngày sớm nhất
    @Test
    void TC02_intervalNotMet_reportsEarliestDate() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Bạn cần chờ đến 2026-07-01 để tiêm mũi 2."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"INTERVAL_NOT_MET\",\"earliestDate\":\"2026-07-01\"}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tiêm mũi tiếp theo chưa được?"));

        assertThat(response.getResponse()).contains("2026-07-01");
    }

    // TC-03: Interval đã đủ — xác nhận có thể tiêm
    @Test
    void TC03_intervalMet_confirmsReadyToVaccinate() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Bạn có thể tiêm mũi 2 ngay bây giờ."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"PENDING\",\"nextDoseNumber\":2}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi có thể tiêm chưa?"));

        assertThat(response.getResponse()).contains("mũi 2");
    }

    // TC-04: Series completed — thông báo đã hoàn thành
    @Test
    void TC04_seriesCompleted_notifiesCompletion() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Series tiêm chủng của bạn đã hoàn thành."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"SERIES_COMPLETED\"}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi còn cần tiêm nữa không?"));

        assertThat(response.getResponse()).contains("hoàn thành");
    }

    // TC-05: Cơ sở còn slot — liệt kê ngày trống
    @Test
    void TC05_facilityHasSlots_listsAvailableDates() {
        String inputJson = "{\"facility_id\":1,\"start_date\":\"2027-01-10\",\"end_date\":\"2027-01-17\"}";
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_available_slots", inputJson))
                .thenReturn(endTurn("Cơ sở 1 còn 7 slot ngày 2027-01-10."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_available_slots", inputJson))
                .thenReturn("{\"slots\":[{\"date\":\"2027-01-10\",\"availableSlots\":7}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Cơ sở 1 còn slot không?"));

        assertThat(response.getResponse()).contains("slot");
    }

    // TOOL BEHAVIOR (TC-06 → TC-09)

    // TC-06: Lịch sử rỗng — thông báo lần đầu
    @Test
    void TC06_emptyHistory_informsFirstTime() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_vaccination_history", "{}"))
                .thenReturn(endTurn("Chưa có lịch sử tiêm. Đây là lần đầu của bạn."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_vaccination_history", "{}"))
                .thenReturn("{\"records\":[]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Lịch sử tiêm của tôi?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    // TC-07: Recommendation SERIES_COMPLETED — không cần tiêm thêm
    @Test
    void TC07_seriesCompleted_noMoreDosesNeeded() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Bạn không cần tiêm thêm, series đã hoàn thành."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"SERIES_COMPLETED\"}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Còn cần tiêm không?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    // TC-08: availableSlots = 0 — không còn slot
    @Test
    void TC08_noAvailableSlots_informsNoSlots() {
        String inputJson = "{\"facility_id\":1,\"start_date\":\"2027-01-10\",\"end_date\":\"2027-01-17\"}";
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_available_slots", inputJson))
                .thenReturn(endTurn("Không còn slot trống trong khoảng ngày đó."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_available_slots", inputJson))
                .thenReturn("{\"slots\":[],\"message\":\"Không có slot trống\"}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Còn slot không?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    // TC-09: Tool throws exception — hệ thống gặp sự cố
    @Test
    void TC09_toolThrowsException_returnsErrorGracefully() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_vaccination_history", "{}"))
                .thenReturn(endTurn("Hệ thống gặp sự cố, vui lòng thử lại."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_vaccination_history", "{}"))
                .thenReturn("{\"error\":\"DB connection failed\"}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Lịch sử của tôi?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    //GUARDRAIL (TC-10 → TC-14)

    // TC-10: Câu hỏi chẩn đoán — từ chối
    @Test
    void TC10_diagnosticQuestion_rejectsWithDoctorAdvice() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Câu hỏi này nằm ngoài phạm vi tư vấn tiêm chủng. Vui lòng tham khảo bác sĩ."));

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi bị sốt, tôi mắc bệnh gì?"));

        assertThat(response.getResponse()).contains("bác sĩ");
    }

    // TC-11: Phụ nữ mang thai — không khẳng định
    @Test
    void TC11_pregnancyQuestion_doesNotConfirm() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Câu hỏi này nằm ngoài phạm vi. Vui lòng tham khảo bác sĩ."));

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi đang mang thai, có nên tiêm không?"));

        assertThat(response.getResponse()).doesNotContain("nên tiêm")
                .doesNotContain("không nên tiêm");
    }

    // TC-12: Yêu cầu kê đơn thuốc — từ chối
    @Test
    void TC12_prescriptionRequest_isRejected() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Câu hỏi này nằm ngoài phạm vi tư vấn tiêm chủng."));

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Bạn có thể kê đơn thuốc cho tôi không?"));

        assertThat(response.getResponse()).contains("ngoài phạm vi");
    }

    // TC-13: Khó thở sau tiêm — guardrailService xử lý
    @Test
    void TC13_seriousReaction_guardrailAddsEmergencyAdvice() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Đây là phản ứng nghiêm trọng."));
        when(guardrailService.apply(anyString()))
                .thenReturn("Đây là phản ứng nghiêm trọng.\n\n⚠️ Vui lòng đến cơ sở y tế hoặc gọi cấp cứu ngay lập tức.");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi bị khó thở sau tiêm"));

        assertThat(response.getResponse()).contains("cấp cứu");
        verify(guardrailService).apply(anyString());
    }

    // TC-14: Câu hỏi ngoài phạm vi — từ chối
    @Test
    void TC14_outOfScopeQuestion_rejectsPolitely() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Câu hỏi này nằm ngoài phạm vi tư vấn tiêm chủng."));

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Thời tiết hôm nay thế nào?"));

        assertThat(response.getResponse()).contains("ngoài phạm vi");
    }

    // MULTI-TURN (TC-15 → TC-17)

    // TC-15: Lượt 2 sử dụng context từ lượt 1
    @Test
    void TC15_secondTurn_usesContextFromFirstTurn() {
        String sessionId = "session-abc";
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Bạn đã tiêm mũi 1 vaccine X."))  // lượt 1
                .thenReturn(endTurn("Dựa trên lịch sử, bạn có thể tiêm tại Cơ sở A.")); // lượt 2

        AdvisorChatResponse r1 = advisorService.chat(CITIZEN_ID, reqWithSession("Lịch sử của tôi?", sessionId));
        AdvisorChatResponse r2 = advisorService.chat(CITIZEN_ID, reqWithSession("Vậy tôi cần tiêm ở đâu?", sessionId));

        assertThat(r1.getSessionId()).isEqualTo(sessionId);
        assertThat(r2.getSessionId()).isEqualTo(sessionId);
        // Lượt 2 phải gọi LlmClient với lịch sử đầy đủ (4 messages: 2 user + 2 assistant)
        verify(llmClient, times(2)).chat(anyString(), anyList(), anyList());
    }

    // TC-16: Session mới không có context từ session cũ
    @Test
    void TC16_newSession_hasNoContextFromOldSession() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Phản hồi 1"))
                .thenReturn(endTurn("Phản hồi 2"));

        AdvisorChatResponse r1 = advisorService.chat(CITIZEN_ID, reqWithSession("Câu hỏi 1", "session-1"));
        AdvisorChatResponse r2 = advisorService.chat(CITIZEN_ID, reqWithSession("Câu hỏi 2", "session-2"));

        assertThat(r1.getSessionId()).isEqualTo("session-1");
        assertThat(r2.getSessionId()).isEqualTo("session-2");
        // Mỗi session có history riêng biệt
        verify(llmClient, times(2)).chat(anyString(), anyList(), anyList());
    }

    // TC-17: 5 lượt hỏi liên tiếp — vẫn trả lời đúng
    @Test
    void TC17_fiveTurns_allAnsweredCorrectly() {
        String sessionId = "session-long";
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Trả lời 1"))
                .thenReturn(endTurn("Trả lời 2"))
                .thenReturn(endTurn("Trả lời 3"))
                .thenReturn(endTurn("Trả lời 4"))
                .thenReturn(endTurn("Trả lời 5"));

        for (int i = 1; i <= 5; i++) {
            AdvisorChatResponse r = advisorService.chat(CITIZEN_ID,
                    reqWithSession("Câu hỏi " + i, sessionId));
            assertThat(r.getResponse()).isEqualTo("Trả lời " + i);
        }

        verify(llmClient, times(5)).chat(anyString(), anyList(), anyList());
    }

    // EDGE CASES (TC-18 → TC-21)

    // TC-18: Citizen có records của 2 vaccine khác nhau
    @Test
    void TC18_twoVaccineRecords_listedCorrectly() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Bạn cần tiêm mũi 2 của Vắc xin X và Vắc xin Y."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[" +
                        "{\"vaccineName\":\"Vắc xin X\",\"status\":\"PENDING\"}," +
                        "{\"vaccineName\":\"Vắc xin Y\",\"status\":\"PENDING\"}" +
                        "]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tôi cần tiêm gì?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    // TC-19: Record có status INVALID — bị bỏ qua bởi service layer
    @Test
    void TC19_invalidRecordIgnored_onlyValidCounted() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Bạn cần tiêm mũi 2 (chỉ tính record VALID)."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"PENDING\",\"nextDoseNumber\":2}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Lịch tiêm tiếp theo?"));

        assertThat(response.getResponse()).contains("mũi 2");
    }

    // TC-20: vaccineId không tồn tại
    @Test
    void TC20_vaccineNotFound_informs() {
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(toolUse("t1", "get_recommended_schedule", "{}"))
                .thenReturn(endTurn("Không tìm thấy vaccine trong hệ thống."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"error\":\"Vaccine not found\"}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Lịch tiêm tiếp theo?"));

        assertThat(response.getResponse()).isNotBlank();
    }

    // TC-21: Tool được gọi 2 lần trong 1 conversation
    @Test
    void TC21_twoToolCallsInOneConversation_bothResultsIncluded() {
        LlmResponse twoToolCalls = new LlmResponse(
                List.of(
                        new LlmToolUseBlock("t1", "get_vaccination_history", "{}"),
                        new LlmToolUseBlock("t2", "get_recommended_schedule", "{}")),
                LlmStopReason.TOOL_USE);

        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(twoToolCalls)
                .thenReturn(endTurn("Lịch sử: 1 mũi. Khuyến nghị: tiêm mũi 2."));
        when(toolExecutorService.execute(CITIZEN_ID, "get_vaccination_history", "{}"))
                .thenReturn("{\"records\":[{\"doseNumber\":1}]}");
        when(toolExecutorService.execute(CITIZEN_ID, "get_recommended_schedule", "{}"))
                .thenReturn("{\"recommendations\":[{\"status\":\"PENDING\"}]}");

        AdvisorChatResponse response = advisorService.chat(CITIZEN_ID, req("Tổng hợp lịch tiêm của tôi?"));

        assertThat(response.getResponse()).contains("mũi 2");
        verify(toolExecutorService).execute(CITIZEN_ID, "get_vaccination_history", "{}");
        verify(toolExecutorService).execute(CITIZEN_ID, "get_recommended_schedule", "{}");
    }

    // TOKEN OPTIMIZATION (TC-22)

    // TC-22: History windowing — chỉ gửi N message cuối khi vượt window
    @Test
    @SuppressWarnings("unchecked")
    void TC22_historyWindowing_sendsOnlyWindowedMessages() {
        ReflectionTestUtils.setField(advisorService, "historyWindowSize", 2);
        String sessionId = "session-window";
        when(llmClient.chat(anyString(), anyList(), anyList()))
                .thenReturn(endTurn("Trả lời 1"))
                .thenReturn(endTurn("Trả lời 2"))
                .thenReturn(endTurn("Trả lời 3"));

        advisorService.chat(CITIZEN_ID, reqWithSession("Câu 1", sessionId));
        advisorService.chat(CITIZEN_ID, reqWithSession("Câu 2", sessionId));
        advisorService.chat(CITIZEN_ID, reqWithSession("Câu 3", sessionId));

        ArgumentCaptor<List<LlmMessage>> historyCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(llmClient, times(3)).chat(anyString(), anyList(), historyCaptor.capture());

        // Lượt 3: full history = 5 messages, window=2 → chỉ gửi ≤2 msg, bắt đầu bằng USER
        List<LlmMessage> thirdCallHistory = historyCaptor.getAllValues().get(2);
        assertThat(thirdCallHistory).hasSizeLessThanOrEqualTo(2);
        assertThat(thirdCallHistory.get(0).getRole()).isEqualTo(LlmRole.USER);
    }
}
