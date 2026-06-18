package Vaccination.Management.System.controller;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import Vaccination.Management.System.service.VaccinationRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaccinationRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class VaccinationRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VaccinationRecordService vaccinationRecordService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String VALID_REQUEST_BODY = "{\"appointmentId\":5,\"batchId\":30}";

    private VaccinationRecordResponse buildResponse() {
        return VaccinationRecordResponse.builder()
                .id(99L)
                .citizenId(1L)
                .citizenName("Nguyễn Văn A")
                .vaccineId(10L)
                .vaccineName("Vắc xin X")
                .facilityId(20L)
                .facilityName("Cơ sở Y")
                .batchNumber("BATCH-001")
                .doseNumber(1)
                .administeredAt(LocalDateTime.of(2026, 5, 26, 9, 0))
                .status(RecordStatus.VALID)
                .correctionReason(null)
                .replacesRecordId(null)
                .createdAt(LocalDateTime.of(2026, 5, 26, 9, 0))
                .build();
    }

    // POST /vaccination-records

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns201_whenSuccess() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), eq(2L))).thenReturn(buildResponse());

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(99))
                .andExpect(jsonPath("$.result.citizenId").value(1))
                .andExpect(jsonPath("$.result.citizenName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.result.vaccineId").value(10))
                .andExpect(jsonPath("$.result.vaccineName").value("Vắc xin X"))
                .andExpect(jsonPath("$.result.batchNumber").value("BATCH-001"))
                .andExpect(jsonPath("$.result.doseNumber").value(1))
                .andExpect(jsonPath("$.result.status").value("VALID"));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns404_whenAppointmentNotFound() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("Appointment not found"));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns400_whenAppointmentInvalidStatus() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4005));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns404_whenBatchNotFound() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.BATCH_NOT_FOUND));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(5002))
                .andExpect(jsonPath("$.message").value("Vaccine batch not found"));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns400_whenBatchUnavailable() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.BATCH_UNAVAILABLE));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(5003));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns400_whenBatchDepleted() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.BATCH_DEPLETED));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(5004));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns400_whenRecordAppointmentMismatch() throws Exception {
        when(vaccinationRecordService.recordVaccination(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.RECORD_APPOINTMENT_MISMATCH));

        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(5005));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns404_whenMissingAppointmentId() throws Exception {
        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchId\":30}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(username = "2")
    void recordVaccination_returns404_whenMissingBatchId() throws Exception {
        mockMvc.perform(post("/vaccination-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(5002));
    }

    //GET /vaccination-records/citizens/{citizenId}/vaccination-records

    @Test
    @WithMockUser(username = "1", roles = {"CITIZEN"})
    void getVaccinationRecords_returns200_withList() throws Exception {
        VaccinationRecordResponse r1 = buildResponse();
        VaccinationRecordResponse r2 = VaccinationRecordResponse.builder()
                .id(100L).citizenId(1L).citizenName("Nguyễn Văn A")
                .vaccineId(11L).vaccineName("Vắc xin Y")
                .facilityId(20L).facilityName("Cơ sở Y")
                .batchNumber("BATCH-002").doseNumber(2)
                .administeredAt(LocalDateTime.of(2026, 6, 1, 9, 0))
                .status(RecordStatus.VALID).build();

        when(vaccinationRecordService.getVaccinationRecords(eq(1L), eq(1L), any()))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/vaccination-records/citizens/1/vaccination-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(99))
                .andExpect(jsonPath("$.result[0].status").value("VALID"))
                .andExpect(jsonPath("$.result[1].id").value(100))
                .andExpect(jsonPath("$.result[1].vaccineName").value("Vắc xin Y"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CITIZEN"})
    void getVaccinationRecords_returns200_withEmptyList() throws Exception {
        when(vaccinationRecordService.getVaccinationRecords(eq(1L), eq(1L), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/vaccination-records/citizens/1/vaccination-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(0));
    }

    @Test
    @WithMockUser(username = "1", roles = {"CITIZEN"})
    void getVaccinationRecords_returns403_whenAccessDenied() throws Exception {
        when(vaccinationRecordService.getVaccinationRecords(eq(2L), eq(1L), any()))
                .thenThrow(new AppException(ErrorCode.ACCESS_DENIED));

        mockMvc.perform(get("/vaccination-records/citizens/2/vaccination-records"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1006))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }
}
