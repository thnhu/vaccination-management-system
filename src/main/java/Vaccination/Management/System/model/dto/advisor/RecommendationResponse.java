package Vaccination.Management.System.model.dto.advisor;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecommendationResponse {
    private Long vaccineId;
    private String vaccineName;
    private Integer currentDoseNumber;
    private Integer nextDoseNumber;
    private LocalDate earliestDate;
    // PENDING | INTERVAL_NOT_MET | SERIES_COMPLETED
    private String status;
}
