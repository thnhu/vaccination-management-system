package Vaccination.Management.System.controller;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import Vaccination.Management.System.security.CustomUserDetailsService;
import Vaccination.Management.System.security.JwtUtil;
import Vaccination.Management.System.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String VALID_REQUEST_BODY =
            "{\"vaccineId\":10,\"facilityId\":20,\"preferredDate\":\"2027-12-31\"}";

    // POST /appointments

    @Test
    @WithMockUser(username = "1")
    void createAppointment_returns201WithResponse() throws Exception {
        AppointmentResponse response = buildResponse(100L, AppointmentStatus.SCHEDULED);

        when(appointmentService.createAppointment(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(100))
                .andExpect(jsonPath("$.result.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.result.facilityId").value(20))
                .andExpect(jsonPath("$.result.vaccineId").value(10));
    }

    @Test
    @WithMockUser(username = "1")
    void createAppointment_returns409_whenDuplicateAppointment() throws Exception {
        when(appointmentService.createAppointment(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_DUPLICATE));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(4003))
                .andExpect(jsonPath("$.message").value("Scheduled appointment already exists for this vaccine"));
    }

    @Test
    @WithMockUser(username = "1")
    void createAppointment_returns400_whenSlotFull() throws Exception {
        when(appointmentService.createAppointment(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_SLOT_FULL));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4002));
    }

    @Test
    @WithMockUser(username = "1")
    void createAppointment_returns400_whenDoseIntervalNotMet() throws Exception {
        when(appointmentService.createAppointment(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_DOSE_INTERVAL));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4004));
    }

    @Test
    @WithMockUser(username = "1")
    void createAppointment_returns400_whenVaccineSeriesCompleted() throws Exception {
        when(appointmentService.createAppointment(any(), anyLong()))
                .thenThrow(new AppException(ErrorCode.VACCINATION_SERIES_COMPLETED));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4007));
    }

    // GET /appointments/my

    @Test
    @WithMockUser(username = "1")
    void getMyAppointments_returns200WithList() throws Exception {
        AppointmentSummary s1 = AppointmentSummary.builder()
                .id(1L).vaccineName("Vắc xin A").facilityName("Cơ sở B")
                .preferredDate(LocalDate.of(2026, 6, 1)).status(AppointmentStatus.SCHEDULED).build();
        AppointmentSummary s2 = AppointmentSummary.builder()
                .id(2L).vaccineName("Vắc xin C").facilityName("Cơ sở D")
                .preferredDate(LocalDate.of(2026, 7, 1)).status(AppointmentStatus.VACCINATED).build();

        when(appointmentService.getMyAppointments(1L)).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/appointments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].vaccineName").value("Vắc xin A"))
                .andExpect(jsonPath("$.result[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.result[1].status").value("VACCINATED"));
    }

    @Test
    @WithMockUser(username = "1")
    void getMyAppointments_returns200WithEmptyList_whenNone() throws Exception {
        when(appointmentService.getMyAppointments(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/appointments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(0));
    }

    // PATCH /appointments/{id}/cancel

    @Test
    @WithMockUser(username = "1")
    void cancelAppointment_returns200() throws Exception {
        AppointmentResponse response = buildResponse(6L, AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointment(eq(6L), eq(1L), eq("Bận"))).thenReturn(response);

        mockMvc.perform(patch("/appointments/6/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Bận\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "1")
    void cancelAppointment_returns400_whenInvalidStatus() throws Exception {
        when(appointmentService.cancelAppointment(anyLong(), anyLong(), any()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS));

        mockMvc.perform(patch("/appointments/8/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"lý do\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4005));
    }

    // PATCH /appointments/{id}/no-show

    @Test
    @WithMockUser(username = "2")
    void markNoShow_returns200() throws Exception {
        AppointmentResponse response = buildResponse(11L, AppointmentStatus.NO_SHOW);
        when(appointmentService.markNoShow(11L, 2L)).thenReturn(response);

        mockMvc.perform(patch("/appointments/11/no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.status").value("NO_SHOW"));
    }

    @Test
    @WithMockUser(username = "2")
    void markNoShow_returns400_whenNotConfirmed() throws Exception {
        when(appointmentService.markNoShow(anyLong(), anyLong()))
                .thenThrow(new AppException(ErrorCode.APPOINTMENT_INVALID_STATUS));

        mockMvc.perform(patch("/appointments/11/no-show"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4005));
    }

    // helpers

    private AppointmentResponse buildResponse(Long id, AppointmentStatus status) {
        return AppointmentResponse.builder()
                .id(id)
                .citizenId(1L)
                .citizenName("Nguyễn Văn A")
                .vaccineId(10L)
                .vaccineName("Vắc xin X")
                .facilityId(20L)
                .facilityName("Cơ sở Y")
                .preferredDate(LocalDate.of(2026, 6, 1))
                .status(status)
                .createdAt(LocalDateTime.of(2026, 5, 26, 8, 0))
                .build();
    }
}
