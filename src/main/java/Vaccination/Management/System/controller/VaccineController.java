package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.vaccine.CreateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.UpdateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.service.VaccineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vaccines")
@RequiredArgsConstructor
public class VaccineController {

    private final VaccineService vaccineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VaccineSummary>>> getAllVaccines() {
        return ResponseEntity.ok(ApiResponse.success(vaccineService.getAllVaccines()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VaccineResponse>> getVaccineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(vaccineService.getVaccineById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VaccineResponse>> createVaccine(
            @Valid @RequestBody CreateVaccineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vaccineService.createVaccine(request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VaccineResponse>> updateVaccine(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVaccineRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vaccineService.updateVaccine(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<VaccineResponse>> deactivateVaccine(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(vaccineService.deactivateVaccine(id)));
    }
}
