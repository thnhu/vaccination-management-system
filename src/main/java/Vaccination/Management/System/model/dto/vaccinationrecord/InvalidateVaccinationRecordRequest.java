package Vaccination.Management.System.model.dto.vaccinationrecord;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvalidateVaccinationRecordRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}
