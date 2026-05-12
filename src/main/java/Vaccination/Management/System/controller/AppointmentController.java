package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;
import Vaccination.Management.System.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long citizenId = extractUserId(userDetails);
        AppointmentResponse response = appointmentService.createAppointment(request, citizenId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppointmentSummary>>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long citizenId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getMyAppointments(citizenId)));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long staffId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.confirmAppointment(id, staffId)));
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}