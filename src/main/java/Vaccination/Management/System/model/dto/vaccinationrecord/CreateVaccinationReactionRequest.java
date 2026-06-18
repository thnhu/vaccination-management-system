package Vaccination.Management.System.model.dto.vaccinationrecord;

import Vaccination.Management.System.model.enums.ReactionLevel;
import Vaccination.Management.System.model.enums.ReportSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateVaccinationReactionRequest {

    @NotBlank(message = "Symptom is required")
    private String symptom;

    @NotNull(message = "Severity is required")
    private ReactionLevel severity;

    @NotNull(message = "Onset time is required")
    private LocalDateTime onsetAt;

    private LocalDateTime resolvedAt;

    @NotNull(message = "Report source is required")
    private ReportSource reportSource;
}
