package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VaccineSummary {
    private Long id;
    private String name;
    private String countryOfOrigin;
    private Integer requiredDoses;
    private VaccineCategory category;
    private BigDecimal price;
    private boolean active;
}