package Vaccination.Management.System.model.dto.facility;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FacilityCapacityResponse {
    private Long id;
    private Long facilityId;
    private Integer maxSlotsPerDay;
    private LocalDate effectiveFrom;
}
