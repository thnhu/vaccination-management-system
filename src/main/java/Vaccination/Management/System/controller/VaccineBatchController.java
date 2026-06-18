package Vaccination.Management.System.controller;

import Vaccination.Management.System.common.ApiResponse;
import Vaccination.Management.System.model.dto.vaccinebatch.VaccineBatchResponse;
import Vaccination.Management.System.service.VaccineBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class VaccineBatchController {

    private final VaccineBatchService vaccineBatchService;

    @PatchMapping("/{id}/recall")
    public ResponseEntity<ApiResponse<VaccineBatchResponse>> recallBatch(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long staffId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(vaccineBatchService.recallBatch(id, staffId)));
    }
}
