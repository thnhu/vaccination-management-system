package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vaccination/vaccines")
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
}
