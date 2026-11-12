package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Partial update: a null field is left unchanged. For the free-text content fields an empty
 * string clears the stored value.
 */
@Data
public class UpdateVaccineRequest {
    @Size(max = 200)
    private String name;

    private VaccineCategory category;

    private Long manufacturerId;

    @Size(max = 1000)
    private String description;

    @Min(0) @Max(1500)
    private Integer minAgeMonths;

    @Min(0) @Max(1500)
    private Integer maxAgeMonths;

    @Size(max = 100)
    private String route;

    @Size(max = 1000)
    private String contraindications;

    @Size(max = 1000)
    private String precautions;

    @Size(max = 1000)
    private String sideEffects;

    @Size(max = 200)
    private String storage;
}
