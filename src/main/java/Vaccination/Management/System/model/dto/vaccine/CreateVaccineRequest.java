package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVaccineRequest {
    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    private VaccineCategory category;
}
