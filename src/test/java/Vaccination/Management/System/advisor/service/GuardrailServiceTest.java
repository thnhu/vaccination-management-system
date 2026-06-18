package Vaccination.Management.System.advisor.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuardrailServiceTest {

    private final GuardrailService guardrailService = new GuardrailService();

    @Test
    void apply_returnsOriginal_whenNoKeywords() {
        String response = "Bạn đã tiêm mũi 1, có thể tiêm mũi 2 sau 30 ngày.";
        assertThat(guardrailService.apply(response)).isEqualTo(response);
    }

    @Test
    void apply_appendsEmergencySuffix_whenEmergencyKeywordPresent() {
        String response = "Bạn đang bị khó thở sau tiêm.";
        String result = guardrailService.apply(response);
        assertThat(result).contains("cơ sở y tế hoặc gọi cấp cứu ngay lập tức");
        assertThat(result).startsWith(response);
    }

    @Test
    void apply_doesNotAppendSuffix_whenAlreadyAdvised() {
        String response = "Bạn bị khó thở. Vui lòng đến cơ sở y tế ngay.";
        assertThat(guardrailService.apply(response)).isEqualTo(response);
    }

    @Test
    void apply_returnsOriginal_whenResponseNull() {
        assertThat(guardrailService.apply(null)).isNull();
    }

    @Test
    void apply_returnsOriginal_whenResponseBlank() {
        assertThat(guardrailService.apply("   ")).isEqualTo("   ");
    }

    @Test
    void apply_triggersOnSocPhanVe() {
        String response = "Sau tiêm, tôi bị sốc phản vệ.";
        assertThat(guardrailService.apply(response)).contains("cấp cứu ngay lập tức");
    }
}
