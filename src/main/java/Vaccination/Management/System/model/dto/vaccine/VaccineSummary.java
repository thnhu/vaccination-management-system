package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VaccineSummary {
    private Long id;
    private String name;
    private VaccineCategory category;
    private int totalDoses;
    private boolean active;
}
