package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.vaccinationrecord.RecordVaccinationRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;
import Vaccination.Management.System.service.VaccinationRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vaccination-records")
@RequiredArgsConstructor
public class VaccinationRecordController {

    private final VaccinationRecordService vaccinationRecordService;

    @PostMapping
    public ResponseEntity<ApiResponse<VaccinationRecordResponse>> recordVaccination(
            @Valid @RequestBody RecordVaccinationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long staffId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        vaccinationRecordService.recordVaccination(request, staffId)));
    }

    @GetMapping("/citizens/{citizenId}/vaccination-records")
    public ResponseEntity<ApiResponse<List<VaccinationRecordResponse>>> getVaccinationRecords(
            @PathVariable Long citizenId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long requesterId = Long.parseLong(userDetails.getUsername());
        String requesterRole = userDetails.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(ApiResponse.success(
                vaccinationRecordService.getVaccinationRecords(citizenId, requesterId, requesterRole)));
    }
}