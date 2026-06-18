package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVaccineRequest {
    @Size(max = 200)
    private String name;

    private VaccineCategory category;
}
