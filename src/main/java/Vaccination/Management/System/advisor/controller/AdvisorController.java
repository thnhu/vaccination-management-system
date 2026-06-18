package Vaccination.Management.System.advisor.controller;

import Vaccination.Management.System.advisor.service.CitizenAdvisorService;
import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatRequest;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatResponse;
import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;
import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;
import Vaccination.Management.System.service.AvailableSlotService;
import Vaccination.Management.System.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/advisor")
@RequiredArgsConstructor
public class AdvisorController {

    private final CitizenAdvisorService citizenAdvisorService;
    private final RecommendationService recommendationService;
    private final AvailableSlotService availableSlotService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AdvisorChatResponse>> chat(
            @Valid @RequestBody AdvisorChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long citizenId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                citizenAdvisorService.chat(citizenId, request)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long citizenId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getRecommendations(citizenId)));
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @RequestParam Long facilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ApiResponse.success(
                availableSlotService.getAvailableSlots(facilityId, startDate, endDate)));
    }
}
