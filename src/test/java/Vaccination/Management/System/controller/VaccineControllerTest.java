package Vaccination.Management.System.controller;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.model.enums.VaccineCategory;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import Vaccination.Management.System.service.VaccineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaccineController.class)
@AutoConfigureMockMvc(addFilters = false)
class VaccineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VaccineService vaccineService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // GET /vaccines

    @Test
    void getAllVaccines_returns200WithList() throws Exception {
        VaccineSummary summary = VaccineSummary.builder()
                .id(1L)
                .name("Vắc xin A")
                .category(VaccineCategory.CHILD)
                .totalDoses(2)
                .active(true)
                .build();

        when(vaccineService.getAllVaccines()).thenReturn(List.of(summary));

        mockMvc.perform(get("/vaccines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("Vắc xin A"))
                .andExpect(jsonPath("$.result[0].category").value("CHILD"))
                .andExpect(jsonPath("$.result[0].totalDoses").value(2))
                .andExpect(jsonPath("$.result[0].active").value(true));
    }

    @Test
    void getAllVaccines_returns200WithEmptyArray_whenNoVaccines() throws Exception {
        when(vaccineService.getAllVaccines()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/vaccines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(0));
    }

    @Test
    void getAllVaccines_returnsMultipleVaccines() throws Exception {
        List<VaccineSummary> summaries = List.of(
                VaccineSummary.builder().id(1L).name("Vắc xin A").category(VaccineCategory.CHILD).totalDoses(2).active(true).build(),
                VaccineSummary.builder().id(2L).name("Vắc xin B").category(VaccineCategory.ADULT).totalDoses(1).active(false).build()
        );
        when(vaccineService.getAllVaccines()).thenReturn(summaries);

        mockMvc.perform(get("/vaccines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].name").value("Vắc xin A"))
                .andExpect(jsonPath("$.result[1].active").value(false));
    }

    @Test
    void getAllVaccines_responseWrappedWithSuccessCode() throws Exception {
        when(vaccineService.getAllVaccines()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/vaccines"))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    // GET /vaccines/{id}

    @Test
    void getVaccineById_returns200WithDetail() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 26, 10, 0);
        VaccineResponse response = VaccineResponse.builder()
                .id(1L)
                .name("Vắc xin A")
                .category(VaccineCategory.CHILD)
                .active(true)
                .totalDoses(2)
                .doseSchedules(List.of(
                        VaccineResponse.DoseScheduleInfo.builder().doseNumber(1).daysAfterPrevious(null).build(),
                        VaccineResponse.DoseScheduleInfo.builder().doseNumber(2).daysAfterPrevious(30).build()
                ))
                .diseases(Set.of(
                        VaccineResponse.DiseaseInfo.builder().id(10L).name("Cúm mùa").build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(vaccineService.getVaccineById(1L)).thenReturn(response);

        mockMvc.perform(get("/vaccines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.name").value("Vắc xin A"))
                .andExpect(jsonPath("$.result.category").value("CHILD"))
                .andExpect(jsonPath("$.result.active").value(true))
                .andExpect(jsonPath("$.result.totalDoses").value(2))
                .andExpect(jsonPath("$.result.doseSchedules.length()").value(2))
                .andExpect(jsonPath("$.result.doseSchedules[0].doseNumber").value(1))
                .andExpect(jsonPath("$.result.doseSchedules[1].doseNumber").value(2))
                .andExpect(jsonPath("$.result.doseSchedules[1].daysAfterPrevious").value(30))
                .andExpect(jsonPath("$.result.diseases.length()").value(1));
    }

    @Test
    void getVaccineById_returns404_whenNotFound() throws Exception {
        when(vaccineService.getVaccineById(99L))
                .thenThrow(new AppException(ErrorCode.VACCINE_NOT_FOUND));

        mockMvc.perform(get("/vaccines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.message").value("Vaccine not found"));
    }

    @Test
    void getVaccineById_noDosesAndNoDiseases_returnsEmptyCollections() throws Exception {
        VaccineResponse response = VaccineResponse.builder()
                .id(5L).name("Vắc xin mới").category(VaccineCategory.ALL_AGES)
                .active(true).totalDoses(0)
                .doseSchedules(Collections.emptyList())
                .diseases(Collections.emptySet())
                .build();
        when(vaccineService.getVaccineById(5L)).thenReturn(response);

        mockMvc.perform(get("/vaccines/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalDoses").value(0))
                .andExpect(jsonPath("$.result.doseSchedules").isEmpty())
                .andExpect(jsonPath("$.result.diseases").isEmpty());
    }
}
