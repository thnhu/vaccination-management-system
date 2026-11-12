package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.enums.AdministrativeUnitType;
import Vaccination.Management.System.repository.AdministrativeUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public lookup used by the sign-up form (a visitor has no token yet). */
@RestController
@RequestMapping("/administrative-units")
@RequiredArgsConstructor
public class AdministrativeUnitController {

    public record ProvinceResponse(String code, String name) {}

    private final AdministrativeUnitRepository administrativeUnitRepository;

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> getProvinces() {
        List<ProvinceResponse> provinces = administrativeUnitRepository
                .findByTypeOrderByName(AdministrativeUnitType.PROVINCE)
                .stream()
                .map(u -> new ProvinceResponse(u.getCode(), u.getName()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(provinces));
    }
}
