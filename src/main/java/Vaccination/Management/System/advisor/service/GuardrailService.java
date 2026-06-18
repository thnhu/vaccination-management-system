package Vaccination.Management.System.advisor.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuardrailService {

    private static final List<String> EMERGENCY_KEYWORDS = List.of(
            "khó thở", "sốc phản vệ", "ngất xỉu", "co giật", "ngừng tim", "mất ý thức"
    );

    private static final List<String> OUT_OF_SCOPE_SIGNALS = List.of(
            "nằm ngoài phạm vi",
            "ngoài phạm vi tư vấn",
            "không thuộc phạm vi",
            "không thể tư vấn về",
            "không hỗ trợ vấn đề này"
    );

    private static final String OUT_OF_SCOPE_RESPONSE =
            "Câu hỏi này nằm ngoài phạm vi tư vấn tiêm chủng. Vui lòng tìm kiếm thông tin này từ các nguồn chính thức khác." +
            "\n\nTôi là Vaccination Advisor và chỉ có thể hỗ trợ:" +
            "\n- Tra cứu lịch sử tiêm chủng" +
            "\n- Tính toán mũi tiêm tiếp theo" +
            "\n- Tìm lịch hẹn tiêm trống" +
            "\n\nNếu bạn có câu hỏi về tiêm chủng, tôi sẵn sàng giúp đỡ.";

    private static final String EMERGENCY_SUFFIX =
            "\n\n⚠️ Nếu bạn đang gặp phản ứng nghiêm trọng sau tiêm, " +
            "vui lòng đến cơ sở y tế hoặc gọi cấp cứu ngay lập tức.";

    public String apply(String response) {
        if (response == null || response.isBlank()) return response;

        String lower = response.toLowerCase();

        if (OUT_OF_SCOPE_SIGNALS.stream().anyMatch(lower::contains)) {
            return OUT_OF_SCOPE_RESPONSE;
        }

        boolean hasEmergency = EMERGENCY_KEYWORDS.stream().anyMatch(lower::contains);
        boolean alreadyAdvised = lower.contains("cơ sở y tế ngay") || lower.contains("cấp cứu ngay");

        if (hasEmergency && !alreadyAdvised) {
            return response + EMERGENCY_SUFFIX;
        }

        return response;
    }
}
