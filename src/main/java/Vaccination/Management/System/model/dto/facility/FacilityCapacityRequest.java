package Vaccination.Management.System.model.dto.facility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FacilityCapacityRequest {

    @NotNull
    @Positive
    private Integer maxSlotsPerDay;

    @NotNull
    private LocalDate effectiveFrom;
}
