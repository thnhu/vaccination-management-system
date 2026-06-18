package Vaccination.Management.System.advisor.service.impl;

import Vaccination.Management.System.advisor.service.FaqService;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class FaqServiceImpl implements FaqService {

    private record FaqEntry(List<List<String>> keywordSets, String answer) {
        boolean matches(String normalized) {
            return keywordSets.stream().anyMatch(ks ->
                    ks.stream().allMatch(normalized::contains));
        }
    }

    // ── FAQ answers ───────────────────────────────────────────────────────────

    private static final String ANS_SO_SINH =
            "**Lịch tiêm sơ sinh (trong 24 giờ đầu và tháng đầu tiên):**\n" +
            "- Trong vòng 24 giờ sau sinh: Viêm gan B mũi 1, BCG (phòng lao)\n" +
            "- Tháng 1-2: Viêm gan B mũi 2 (nếu dùng lịch 0-1-6)\n\n" +
            "Lưu ý: Đây là lịch tiêm chủng mở rộng quốc gia. Tình trạng sức khỏe của trẻ có thể ảnh hưởng đến lịch tiêm — vui lòng tham khảo bác sĩ.";

    private static final String ANS_2_THANG =
            "**Lịch tiêm tháng thứ 2:**\n" +
            "- Vaccine 5-trong-1 (DPT-VGB-Hib) mũi 1 — phòng bạch hầu, ho gà, uốn ván, viêm gan B, Hib\n" +
            "- Vaccine bại liệt OPV mũi 1\n" +
            "- Vaccine Rotavirus mũi 1 (phòng tiêu chảy do Rotavirus)\n\n" +
            "Lưu ý: Đây là lịch tiêm chủng mở rộng quốc gia. Vui lòng đến cơ sở tiêm chủng để được tư vấn cụ thể.";

    private static final String ANS_3_THANG =
            "**Lịch tiêm tháng thứ 3:**\n" +
            "- Vaccine 5-trong-1 (DPT-VGB-Hib) mũi 2\n" +
            "- Vaccine bại liệt OPV mũi 2\n" +
            "- Vaccine Rotavirus mũi 2\n\n" +
            "Lưu ý: Cần hoàn thành mũi 1 trước khi tiêm mũi 2. Khoảng cách tối thiểu giữa các mũi là 28 ngày.";

    private static final String ANS_4_THANG =
            "**Lịch tiêm tháng thứ 4:**\n" +
            "- Vaccine 5-trong-1 (DPT-VGB-Hib) mũi 3\n" +
            "- Vaccine bại liệt OPV mũi 3\n\n" +
            "Lưu ý: Đây là mũi cuối của series sơ cấp. Trẻ sẽ cần tiêm nhắc lại vào 18 tháng.";

    private static final String ANS_6_THANG =
            "**Lịch tiêm tháng thứ 6:**\n" +
            "- Viêm gan B mũi 3 (nếu dùng lịch 0-1-6)\n" +
            "- Vaccine cúm mũi 1 (khuyến cáo, tiêm hàng năm)\n\n" +
            "Lưu ý: Vaccine cúm nên tiêm nhắc hàng năm trước mùa cúm (tháng 9-11).";

    private static final String ANS_9_THANG =
            "**Lịch tiêm tháng thứ 9:**\n" +
            "- Vaccine Sởi mũi 1\n\n" +
            "Lưu ý: Đây là mũi quan trọng trong phòng chống dịch sởi. Mũi nhắc lại (MR — Sởi-Rubella) sẽ tiêm khi trẻ 18 tháng.";

    private static final String ANS_12_THANG =
            "**Lịch tiêm 12 tháng (1 tuổi):**\n" +
            "- Vaccine Viêm não Nhật Bản (VNNB) mũi 1\n" +
            "- Vaccine Thủy đậu mũi 1\n" +
            "- Vaccine Viêm gan A mũi 1\n\n" +
            "Lưu ý: VNNB cần 3 mũi (mũi 2 sau mũi 1 từ 1-2 tuần, mũi 3 sau mũi 1 một năm).";

    private static final String ANS_18_THANG =
            "**Lịch tiêm 18 tháng:**\n" +
            "- DPT (3-trong-1) mũi 4 — nhắc lại lần 1\n" +
            "- Vaccine MR (Sởi-Rubella) — nhắc lại\n" +
            "- Vaccine Viêm não Nhật Bản mũi 3 (nếu đã tiêm mũi 2 đúng lịch)\n\n" +
            "Lưu ý: Đây là mũi nhắc quan trọng để duy trì miễn dịch lâu dài.";

    private static final String ANS_PHAN_UNG =
            "**Phản ứng sau tiêm — thông tin chung:**\n\n" +
            "Phản ứng thông thường (tự hết trong 1-3 ngày):\n" +
            "- Sốt nhẹ (dưới 38.5°C), đau hoặc sưng tại chỗ tiêm, mệt mỏi, quấy khóc\n\n" +
            "Cần đến cơ sở y tế ngay nếu:\n" +
            "- Sốt cao trên 39°C, co giật\n" +
            "- Khó thở, tím tái, sốc phản vệ\n" +
            "- Sưng lan rộng, đau dữ dội tại chỗ tiêm\n" +
            "- Trẻ bỏ bú hoàn toàn, li bì không phản ứng\n\n" +
            "Nếu bạn đang có phản ứng nặng, vui lòng đến cơ sở y tế ngay lập tức.";

    private static final String ANS_5_TRONG_1 =
            "**Vaccine 5-trong-1 (DPT-VGB-Hib):**\n" +
            "Một mũi tiêm bảo vệ trẻ khỏi 5 bệnh:\n" +
            "1. Bạch hầu (D — Diphtheria)\n" +
            "2. Ho gà (P — Pertussis)\n" +
            "3. Uốn ván (T — Tetanus)\n" +
            "4. Viêm gan B (VGB)\n" +
            "5. Viêm màng não mủ do Hib (Haemophilus influenzae type b)\n\n" +
            "Lịch tiêm: 3 mũi cơ bản (2, 3, 4 tháng) + 1 mũi nhắc (18 tháng).";

    private static final String ANS_VIEM_GAN_B =
            "**Vaccine Viêm gan B:**\n" +
            "- Tổng cộng 3 mũi theo lịch 0-1-6 (sơ sinh, 1 tháng, 6 tháng)\n" +
            "- Hoặc được tích hợp trong vaccine 5-trong-1 (mũi 2, 3, 4 tháng)\n" +
            "- Mũi sơ sinh: tiêm trong 24 giờ đầu sau sinh là hiệu quả nhất\n\n" +
            "Sau đủ 3 mũi, cơ thể được bảo vệ lâu dài (thường trên 20 năm).";

    private static final String ANS_BAI_LIET =
            "**Vaccine Bại liệt (OPV):**\n" +
            "- Dạng uống (OPV) hoặc tiêm (IPV)\n" +
            "- Lịch trong chương trình mở rộng: 3 mũi lúc 2, 3, 4 tháng\n" +
            "- Bảo vệ khỏi bệnh bại liệt — một bệnh có thể gây liệt vĩnh viễn\n\n" +
            "Việt Nam đã được WHO công nhận thanh toán bại liệt từ năm 2000.";

    private static final String ANS_BCG =
            "**Vaccine BCG (phòng lao):**\n" +
            "- Tiêm 1 mũi duy nhất ngay sau sinh (trong 1 tháng đầu)\n" +
            "- Vị trí: bắp tay trái\n" +
            "- Sau 2-4 tuần có thể xuất hiện vết loét nhỏ tại chỗ tiêm — đây là phản ứng bình thường\n\n" +
            "BCG bảo vệ trẻ khỏi các thể lao nặng (lao màng não, lao kê).";

    private static final String ANS_TIEM_CHUNG_LA_GI =
            "**Tiêm chủng là gì?**\n" +
            "Tiêm chủng (vaccine) là đưa một lượng nhỏ kháng nguyên vào cơ thể để kích thích hệ miễn dịch tạo ra kháng thể, giúp cơ thể nhận biết và chống lại mầm bệnh khi bị nhiễm thực sự.\n\n" +
            "Đây là biện pháp phòng bệnh hiệu quả và an toàn nhất hiện nay.";

    private static final String ANS_LOI_ICH =
            "**Lợi ích của tiêm chủng:**\n" +
            "- Bảo vệ cá nhân khỏi nhiều bệnh nguy hiểm (sởi, bại liệt, uốn ván...)\n" +
            "- Tạo miễn dịch cộng đồng — bảo vệ người không thể tiêm (trẻ sơ sinh, người suy giảm miễn dịch)\n" +
            "- Giảm tỷ lệ tử vong và biến chứng nặng\n" +
            "- Đã giúp loại trừ hoàn toàn bệnh đậu mùa và gần như xóa sổ bại liệt toàn cầu";

    // ── FAQ entry table ───────────────────────────────────────────────────────

    private static final List<FaqEntry> FAQ_ENTRIES = List.of(
            new FaqEntry(List.of(
                    List.of("so sinh"),
                    List.of("moi sinh"),
                    List.of("sau sinh"),
                    List.of("0 thang")
            ), ANS_SO_SINH),

            new FaqEntry(List.of(
                    List.of("2 thang", "lich"),
                    List.of("2 thang tuoi"),
                    List.of("hai thang", "tiem")
            ), ANS_2_THANG),

            new FaqEntry(List.of(
                    List.of("3 thang", "lich"),
                    List.of("3 thang tuoi"),
                    List.of("ba thang", "tiem")
            ), ANS_3_THANG),

            new FaqEntry(List.of(
                    List.of("4 thang", "lich"),
                    List.of("4 thang tuoi"),
                    List.of("bon thang", "tiem")
            ), ANS_4_THANG),

            new FaqEntry(List.of(
                    List.of("6 thang", "lich"),
                    List.of("6 thang tuoi"),
                    List.of("sau thang", "tiem")
            ), ANS_6_THANG),

            new FaqEntry(List.of(
                    List.of("9 thang", "lich"),
                    List.of("9 thang tuoi"),
                    List.of("chin thang", "tiem")
            ), ANS_9_THANG),

            new FaqEntry(List.of(
                    List.of("12 thang", "lich"),
                    List.of("12 thang tuoi"),
                    List.of("1 tuoi", "tiem"),
                    List.of("mot tuoi", "tiem")
            ), ANS_12_THANG),

            new FaqEntry(List.of(
                    List.of("18 thang", "lich"),
                    List.of("18 thang tuoi"),
                    List.of("muoi tam thang", "tiem")
            ), ANS_18_THANG),

            new FaqEntry(List.of(
                    List.of("phan ung", "sau tiem"),
                    List.of("phan ung", "tiem chung"),
                    List.of("tac dung phu", "tiem")
            ), ANS_PHAN_UNG),

            new FaqEntry(List.of(
                    List.of("5 trong 1"),
                    List.of("5-trong-1"),
                    List.of("nam trong mot"),
                    List.of("dpt", "vgb", "hib")
            ), ANS_5_TRONG_1),

            new FaqEntry(List.of(
                    List.of("viem gan b", "may mui"),
                    List.of("viem gan b", "bao nhieu mui"),
                    List.of("viem gan b", "lich tiem")
            ), ANS_VIEM_GAN_B),

            new FaqEntry(List.of(
                    List.of("bai liet"),
                    List.of("opv"),
                    List.of("ipv")
            ), ANS_BAI_LIET),

            new FaqEntry(List.of(
                    List.of("bcg"),
                    List.of("vaccine lao"),
                    List.of("phong lao")
            ), ANS_BCG),

            new FaqEntry(List.of(
                    List.of("tiem chung", "la gi"),
                    List.of("vaccine", "la gi"),
                    List.of("vaccine", "khai niem")
            ), ANS_TIEM_CHUNG_LA_GI),

            new FaqEntry(List.of(
                    List.of("loi ich", "tiem"),
                    List.of("tai sao", "tiem chung"),
                    List.of("tac dung", "tiem chung")
            ), ANS_LOI_ICH)
    );

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    public String lookup(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return null;
        String normalized = normalize(userMessage);
        return FAQ_ENTRIES.stream()
                .filter(e -> e.matches(normalized))
                .map(FaqEntry::answer)
                .findFirst()
                .orElse(null);
    }

    // ── Normalization ─────────────────────────────────────────────────────────

    private String normalize(String text) {
        String lower = text.toLowerCase()
                .replace("đ", "d")
                .replace("đ", "d"); // ký tự đ dạng Unicode
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
