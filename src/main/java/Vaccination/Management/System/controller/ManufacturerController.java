package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.repository.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only list for the admin vaccine form (pick a manufacturer by name instead of typing an id). FACILITY_ADMIN only. */
@RestController
@RequestMapping("/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    public record ManufacturerResponse(Long id, String name, String countryOfOrigin) {}

    private final ManufacturerRepository manufacturerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManufacturerResponse>>> getManufacturers() {
        List<ManufacturerResponse> list = manufacturerRepository.findByActiveTrueOrderByName().stream()
                .map(m -> new ManufacturerResponse(m.getId(), m.getName(), m.getCountryOfOrigin()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
