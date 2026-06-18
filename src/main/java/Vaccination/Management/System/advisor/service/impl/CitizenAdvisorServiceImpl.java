package Vaccination.Management.System.advisor.service.impl;

import Vaccination.Management.System.advisor.llm.*;
import Vaccination.Management.System.advisor.service.CitizenAdvisorService;
import Vaccination.Management.System.advisor.service.FaqService;
import Vaccination.Management.System.advisor.service.GuardrailService;
import Vaccination.Management.System.advisor.service.ToolExecutorService;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatRequest;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitizenAdvisorServiceImpl implements CitizenAdvisorService {

    private final LlmClient llmClient;
    private final ToolExecutorService toolExecutorService;
    private final GuardrailService guardrailService;
    private final FaqService faqService;

    @Value("${advisor.history.window-size:6}")
    private int historyWindowSize;

    private static final int MAX_ITERATIONS = 5;

    private final Map<String, List<LlmMessage>> sessions = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            Bạn là Vaccination Advisor — trợ lý tư vấn tiêm chủng chính thức của hệ thống Quản Lý Tiêm Chủng Quốc Gia.

            Bạn CHỈ có thể:
            1. Tra cứu lịch sử tiêm của công dân bằng get_vaccination_history
            2. Tính mũi tiêm tiếp theo bằng get_recommended_schedule
            3. Tìm lịch hẹn trống bằng get_available_slots

            Bạn TUYỆT ĐỐI KHÔNG:
            - Chẩn đoán bệnh hoặc triệu chứng
            - Kê đơn, tư vấn dùng thuốc
            - Đưa ra kết luận y tế ngoài phạm vi tiêm chủng
            - Khẳng định việc nên/không nên tiêm khi công dân đề cập điều kiện sức khỏe đặc biệt

            Nếu câu hỏi nằm ngoài phạm vi, trả lời ĐÚNG theo mẫu sau (không thay đổi):
            "Câu hỏi này nằm ngoài phạm vi tư vấn tiêm chủng. Vui lòng tìm kiếm thông tin này từ các nguồn chính thức khác.

            Tôi là Vaccination Advisor và chỉ có thể hỗ trợ:
            - Tra cứu lịch sử tiêm chủng
            - Tính toán mũi tiêm tiếp theo
            - Tìm lịch hẹn tiêm trống

            Nếu bạn có câu hỏi về tiêm chủng, tôi sẵn sàng giúp đỡ."
            Nếu công dân báo phản ứng nặng (khó thở, sốc phản vệ): "Vui lòng đến cơ sở y tế ngay lập tức."

            Luôn trả lời bằng tiếng Việt, ngắn gọn, dễ hiểu.
            """;

    private static final List<LlmToolDefinition> TOOL_DEFINITIONS = List.of(
            new LlmToolDefinition(
                    "get_vaccination_history",
                    "Lấy toàn bộ lịch sử tiêm chủng VALID của công dân hiện tại từ hệ thống",
                    Map.of(),
                    List.of()),
            new LlmToolDefinition(
                    "get_recommended_schedule",
                    "Tính toán lịch tiêm mũi tiếp theo dựa trên lịch sử tiêm của công dân",
                    Map.of(),
                    List.of()),
            new LlmToolDefinition(
                    "get_available_slots",
                    "Tìm lịch hẹn còn trống tại một cơ sở tiêm chủng trong một khoảng thời gian",
                    Map.of(
                            "facility_id", Map.of("type", "integer",
                                    "description", "ID của cơ sở tiêm chủng"),
                            "start_date", Map.of("type", "string",
                                    "description", "Ngày bắt đầu tìm kiếm (định dạng YYYY-MM-DD)"),
                            "end_date", Map.of("type", "string",
                                    "description", "Ngày kết thúc tìm kiếm (định dạng YYYY-MM-DD)")),
                    List.of("facility_id", "start_date", "end_date"))
    );

    @Override
    public AdvisorChatResponse chat(Long citizenId, AdvisorChatRequest request) {
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        String faqAnswer = faqService.lookup(request.getMessage());
        if (faqAnswer != null) {
            return new AdvisorChatResponse(faqAnswer, sessionId);
        }

        List<LlmMessage> history = sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(LlmMessage.user(request.getMessage()));

        int iterations = 0;
        while (iterations < MAX_ITERATIONS) {
            iterations++;
            LlmResponse response = llmClient.chat(SYSTEM_PROMPT, TOOL_DEFINITIONS, windowedHistory(history));
            history.add(LlmMessage.assistant(response.getContent()));

            if (response.getStopReason() == LlmStopReason.END_TURN) {
                String text = extractText(response);
                String safeText = guardrailService.apply(text);
                return new AdvisorChatResponse(safeText, sessionId);
            }

            // TOOL_USE path — execute each tool and add results
            List<LlmToolResultBlock> toolResults = new ArrayList<>();
            for (LlmContentBlock block : response.getContent()) {
                if (block instanceof LlmToolUseBlock toolUse) {
                    String result = toolExecutorService.execute(
                            citizenId, toolUse.name(), toolUse.inputJson());
                    toolResults.add(new LlmToolResultBlock(toolUse.id(), result));
                }
            }
            history.add(LlmMessage.toolResults(toolResults));
        }

        return new AdvisorChatResponse("Hệ thống gặp sự cố khi xử lý yêu cầu của bạn. Vui lòng thử lại.", sessionId);
    }

    private List<LlmMessage> windowedHistory(List<LlmMessage> history) {
        if (history.size() <= historyWindowSize) return history;
        List<LlmMessage> trimmed = new ArrayList<>(
                history.subList(history.size() - historyWindowSize, history.size()));
        int start = 0;
        while (start < trimmed.size() && trimmed.get(start).getRole() != LlmRole.USER) {
            start++;
        }
        return trimmed.subList(start, trimmed.size());
    }

    private String extractText(LlmResponse response) {
        return response.getContent().stream()
                .filter(b -> b instanceof LlmTextBlock)
                .map(b -> ((LlmTextBlock) b).text())
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
