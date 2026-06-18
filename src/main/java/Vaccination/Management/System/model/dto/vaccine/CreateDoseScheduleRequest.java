package Vaccination.Management.System.model.dto.vaccine;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDoseScheduleRequest {

    @NotNull
    @Min(1)
    private Integer doseNumber;

    private Integer daysAfterPrevious;
}
