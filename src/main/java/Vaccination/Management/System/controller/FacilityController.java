package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityResponse;
import Vaccination.Management.System.model.dto.facility.FacilityResponse;
import Vaccination.Management.System.model.dto.facility.UpdateFacilityRequest;
import Vaccination.Management.System.model.dto.vaccinebatch.CreateVaccineBatchRequest;
import Vaccination.Management.System.model.dto.vaccinebatch.VaccineBatchResponse;
import Vaccination.Management.System.service.FacilityService;
import Vaccination.Management.System.service.VaccineBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;
    private final VaccineBatchService vaccineBatchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getAllFacilities() {
        return ResponseEntity.ok(ApiResponse.success(facilityService.getAllFacilities()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> getFacilityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(facilityService.getFacilityById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> updateFacility(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacilityRequest request) {

        return ResponseEntity.ok(ApiResponse.success(facilityService.updateFacility(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<FacilityResponse>> deactivateFacility(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(facilityService.deactivateFacility(id)));
    }

    @PostMapping("/{id}/capacity")
    public ResponseEntity<ApiResponse<FacilityCapacityResponse>> createCapacity(
            @PathVariable Long id,
            @Valid @RequestBody FacilityCapacityRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(facilityService.createCapacity(id, request)));
    }

    @GetMapping("/{id}/capacity")
    public ResponseEntity<ApiResponse<FacilityCapacityResponse>> getCapacity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(facilityService.getCapacity(id)));
    }

    @PostMapping("/{id}/batches")
    public ResponseEntity<ApiResponse<VaccineBatchResponse>> createBatch(
            @PathVariable Long id,
            @Valid @RequestBody CreateVaccineBatchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long staffId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vaccineBatchService.createBatch(id, request, staffId)));
    }

    @GetMapping("/{id}/batches")
    public ResponseEntity<ApiResponse<List<VaccineBatchResponse>>> getBatches(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(vaccineBatchService.getBatchesByFacility(id)));
    }
}
