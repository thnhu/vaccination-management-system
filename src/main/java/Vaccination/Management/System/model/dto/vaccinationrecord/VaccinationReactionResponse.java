package Vaccination.Management.System.model.dto.vaccinationrecord;

import Vaccination.Management.System.model.enums.ReactionLevel;
import Vaccination.Management.System.model.enums.ReportSource;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VaccinationReactionResponse {
    private Long id;
    private String symptom;
    private ReactionLevel severity;
    private LocalDateTime onsetAt;
    private LocalDateTime resolvedAt;
    private ReportSource reportSource;
    private LocalDateTime reportedAt;
}
