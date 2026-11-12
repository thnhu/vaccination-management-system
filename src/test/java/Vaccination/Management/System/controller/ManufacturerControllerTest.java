package Vaccination.Management.System.controller;

import Vaccination.Management.System.model.entity.Manufacturer;
import Vaccination.Management.System.repository.ManufacturerRepository;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManufacturerController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManufacturerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ManufacturerRepository manufacturerRepository;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void getManufacturers_returnsActiveManufacturers() throws Exception {
        when(manufacturerRepository.findByActiveTrueOrderByName()).thenReturn(List.of(
                Manufacturer.builder().id(1L).name("GSK").countryOfOrigin("Bỉ").build(),
                Manufacturer.builder().id(2L).name("Sanofi").build()));

        mockMvc.perform(get("/manufacturers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("GSK"))
                .andExpect(jsonPath("$.result[0].countryOfOrigin").value("Bỉ"));
    }
}
