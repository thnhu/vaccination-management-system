package Vaccination.Management.System.controller;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.facility.FacilityResponse;
import Vaccination.Management.System.model.enums.FacilityType;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import Vaccination.Management.System.service.FacilityService;
import Vaccination.Management.System.service.VaccineBatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private FacilityService facilityService;
    @MockitoBean private VaccineBatchService vaccineBatchService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    // GET /facilities

    @Test
    void getAllFacilities_returns200WithEmptyArray_whenNoFacilities() throws Exception {
        when(facilityService.getAllFacilities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(0));
    }

    @Test
    void getAllFacilities_returns200WithList() throws Exception {
        when(facilityService.getAllFacilities()).thenReturn(List.of(
                buildResponse(1L, "Bệnh viện A", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null),
                buildResponse(2L, "Phòng khám B", FacilityType.VACCINATION_CENTER, "79", "TP.HCM", null, null)
        ));

        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("Bệnh viện A"))
                .andExpect(jsonPath("$.result[0].facilityType").value("VACCINATION_CENTER"))
                .andExpect(jsonPath("$.result[0].provinceCode").value("01"))
                .andExpect(jsonPath("$.result[0].provinceName").value("Hà Nội"))
                .andExpect(jsonPath("$.result[0].active").value(true))
                .andExpect(jsonPath("$.result[1].facilityType").value("VACCINATION_CENTER"));
    }

    @Test
    void getAllFacilities_responseWrappedWithSuccessCode() throws Exception {
        when(facilityService.getAllFacilities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/facilities"))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void getAllFacilities_facilityWithWard_returnsWardFields() throws Exception {
        when(facilityService.getAllFacilities()).thenReturn(List.of(
                buildResponse(3L, "Trạm y tế", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", "001", "Phường Ba Đình")
        ));

        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].wardCode").value("001"))
                .andExpect(jsonPath("$.result[0].wardName").value("Phường Ba Đình"));
    }

    @Test
    void getAllFacilities_facilityWithoutWard_wardFieldsNull() throws Exception {
        when(facilityService.getAllFacilities()).thenReturn(List.of(
                buildResponse(4L, "Bệnh viện C", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null)
        ));

        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].wardCode").doesNotExist());
    }

    // GET /facilities/{id}

    @Test
    void getFacilityById_returns200WithAllFields() throws Exception {
        FacilityResponse response = FacilityResponse.builder()
                .id(1L).name("Bệnh viện A").facilityType(FacilityType.VACCINATION_CENTER)
                .address("123 Đường ABC")
                .provinceCode("01").provinceName("Hà Nội")
                .wardCode("001").wardName("Phường Ba Đình")
                .phone("0243456789").active(true).build();
        when(facilityService.getFacilityById(1L)).thenReturn(response);

        mockMvc.perform(get("/facilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.name").value("Bệnh viện A"))
                .andExpect(jsonPath("$.result.facilityType").value("VACCINATION_CENTER"))
                .andExpect(jsonPath("$.result.provinceCode").value("01"))
                .andExpect(jsonPath("$.result.provinceName").value("Hà Nội"))
                .andExpect(jsonPath("$.result.wardCode").value("001"))
                .andExpect(jsonPath("$.result.wardName").value("Phường Ba Đình"))
                .andExpect(jsonPath("$.result.phone").value("0243456789"))
                .andExpect(jsonPath("$.result.active").value(true));
    }

    @Test
    void getFacilityById_returns404_whenNotFound() throws Exception {
        when(facilityService.getFacilityById(99L))
                .thenThrow(new AppException(ErrorCode.FACILITY_NOT_FOUND));

        mockMvc.perform(get("/facilities/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("Facility not found"));
    }

    @Test
    void getFacilityById_inactiveFacility_returnsActiveFalse() throws Exception {
        FacilityResponse response = FacilityResponse.builder()
                .id(5L).name("Bệnh viện cũ").facilityType(FacilityType.VACCINATION_CENTER)
                .address("456 Đường XYZ")
                .provinceCode("01").provinceName("Hà Nội")
                .active(false).build();
        when(facilityService.getFacilityById(5L)).thenReturn(response);

        mockMvc.perform(get("/facilities/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.active").value(false));
    }

    // helpers

    private FacilityResponse buildResponse(Long id, String name, FacilityType type,
                                            String provinceCode, String provinceName,
                                            String wardCode, String wardName) {
        return FacilityResponse.builder()
                .id(id).name(name).facilityType(type)
                .address("123 Đường ABC")
                .provinceCode(provinceCode).provinceName(provinceName)
                .wardCode(wardCode).wardName(wardName)
                .active(true).build();
    }
}
