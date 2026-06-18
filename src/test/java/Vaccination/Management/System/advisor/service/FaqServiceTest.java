package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.advisor.service.impl.FaqServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FaqServiceTest {

    private FaqServiceImpl faqService;

    @BeforeEach
    void setUp() {
        faqService = new FaqServiceImpl();
    }

    // ── null / blank input ────────────────────────────────────────────────────

    @Test
    void lookup_null_returnsNull() {
        assertThat(faqService.lookup(null)).isNull();
    }

    @Test
    void lookup_blank_returnsNull() {
        assertThat(faqService.lookup("   ")).isNull();
    }

    @Test
    void lookup_emptyString_returnsNull() {
        assertThat(faqService.lookup("")).isNull();
    }

    // ── keyword: sơ sinh ──────────────────────────────────────────────────────

    @Test
    void lookup_soSinh_returnsAnswer() {
        String answer = faqService.lookup("trẻ sơ sinh cần tiêm gì");
        assertThat(answer).isNotNull().contains("BCG");
    }

    @Test
    void lookup_moiSinh_returnsAnswer() {
        String answer = faqService.lookup("bé mới sinh tiêm vacxin gì");
        assertThat(answer).isNotNull().contains("BCG");
    }

    @Test
    void lookup_sauSinh_returnsAnswer() {
        String answer = faqService.lookup("sau sinh cần làm gì");
        assertThat(answer).isNotNull().contains("BCG");
    }

    // ── keyword: 2 tháng ─────────────────────────────────────────────────────

    @Test
    void lookup_2Thang_withLich_returnsAnswer() {
        String answer = faqService.lookup("lịch tiêm 2 tháng");
        assertThat(answer).isNotNull().contains("DPT");
    }

    @Test
    void lookup_2ThangTuoi_returnsAnswer() {
        String answer = faqService.lookup("bé 2 tháng tuổi");
        assertThat(answer).isNotNull().contains("DPT");
    }

    // ── keyword: 3 tháng ─────────────────────────────────────────────────────

    @Test
    void lookup_3Thang_withLich_returnsAnswer() {
        String answer = faqService.lookup("lịch tiêm 3 tháng");
        assertThat(answer).isNotNull().contains("DPT");
    }

    // ── keyword: 9 tháng ─────────────────────────────────────────────────────

    @Test
    void lookup_9ThangTuoi_returnsAnswer() {
        String answer = faqService.lookup("bé 9 tháng tuổi");
        assertThat(answer).isNotNull().contains("Sởi");
    }

    // ── keyword: 18 tháng ────────────────────────────────────────────────────

    @Test
    void lookup_18Thang_withLich_returnsAnswer() {
        String answer = faqService.lookup("lịch tiêm 18 tháng");
        assertThat(answer).isNotNull().contains("DPT");
    }

    // ── keyword: phản ứng ────────────────────────────────────────────────────

    @Test
    void lookup_phanUngSauTiem_returnsAnswer() {
        String answer = faqService.lookup("phản ứng sau tiêm là gì");
        assertThat(answer).isNotNull().contains("Sốt");
    }

    @Test
    void lookup_tacDungPhu_returnsAnswer() {
        String answer = faqService.lookup("tác dụng phụ tiêm vắc xin");
        assertThat(answer).isNotNull().contains("Sốt");
    }

    // ── keyword: 5-trong-1 ───────────────────────────────────────────────────

    @Test
    void lookup_5Trong1_returnsAnswer() {
        String answer = faqService.lookup("vaccine 5 trong 1 là gì");
        assertThat(answer).isNotNull().contains("Bạch hầu");
    }

    @Test
    void lookup_dptHib_returnsAnswer() {
        String answer = faqService.lookup("DPT VGB HIB là loại nào");
        assertThat(answer).isNotNull().contains("Bạch hầu");
    }

    // ── keyword: viêm gan B ──────────────────────────────────────────────────

    @Test
    void lookup_viemGanB_withLichTiem_returnsAnswer() {
        String answer = faqService.lookup("lịch tiêm viêm gan B mấy mũi");
        assertThat(answer).isNotNull().contains("0-1-6");
    }

    @Test
    void lookup_viemGanB_baoNhieuMui_returnsAnswer() {
        String answer = faqService.lookup("viêm gan B bao nhiêu mũi");
        assertThat(answer).isNotNull().contains("0-1-6");
    }

    // ── keyword: bại liệt ────────────────────────────────────────────────────

    @Test
    void lookup_baiLiet_returnsAnswer() {
        String answer = faqService.lookup("vaccine bại liệt");
        assertThat(answer).isNotNull().contains("OPV");
    }

    @Test
    void lookup_opv_returnsAnswer() {
        String answer = faqService.lookup("OPV là gì");
        assertThat(answer).isNotNull().contains("OPV");
    }

    // ── keyword: BCG ─────────────────────────────────────────────────────────

    @Test
    void lookup_bcg_returnsAnswer() {
        String answer = faqService.lookup("BCG là vaccine gì");
        assertThat(answer).isNotNull().contains("BCG");
    }

    @Test
    void lookup_phongLao_returnsAnswer() {
        String answer = faqService.lookup("vaccine phòng lao tiêm khi nào");
        assertThat(answer).isNotNull().contains("BCG");
    }

    // ── keyword: tiêm chủng là gì ────────────────────────────────────────────

    @Test
    void lookup_tietChungLaGi_returnsAnswer() {
        String answer = faqService.lookup("tiêm chủng là gì");
        assertThat(answer).isNotNull().contains("kháng thể");
    }

    @Test
    void lookup_vaccineLaGi_returnsAnswer() {
        String answer = faqService.lookup("vaccine là gì, khái niệm");
        assertThat(answer).isNotNull().contains("kháng thể");
    }

    // ── keyword: lợi ích ─────────────────────────────────────────────────────

    @Test
    void lookup_loiIch_returnsAnswer() {
        String answer = faqService.lookup("lợi ích của tiêm chủng");
        assertThat(answer).isNotNull().contains("miễn dịch");
    }

    @Test
    void lookup_taiSaoTiemChung_returnsAnswer() {
        String answer = faqService.lookup("tại sao phải tiêm chủng");
        assertThat(answer).isNotNull().contains("miễn dịch");
    }

    // ── unknown input ─────────────────────────────────────────────────────────

    @Test
    void lookup_unknownQuestion_returnsNull() {
        assertThat(faqService.lookup("thời tiết hôm nay thế nào")).isNull();
    }

    @Test
    void lookup_unrelatedContent_returnsNull() {
        assertThat(faqService.lookup("giá vàng hôm nay bao nhiêu")).isNull();
    }

    // ── normalization ─────────────────────────────────────────────────────────

    @Test
    void lookup_uppercaseInput_treatedCaseInsensitively() {
        String answer = faqService.lookup("BCG");
        assertThat(answer).isNotNull();
    }

    @Test
    void lookup_accentedVietnamese_normalizedCorrectly() {
        // "sơ sinh" with full diacritics should match the "so sinh" keyword set
        String answer = faqService.lookup("Trẻ Sơ Sinh cần tiêm những gì?");
        assertThat(answer).isNotNull().contains("BCG");
    }

    @Test
    void lookup_mixedPunctuationAndSpaces_stillMatches() {
        String answer = faqService.lookup("vaccine BCG - phòng lao là gì?");
        assertThat(answer).isNotNull().contains("BCG");
    }
}
