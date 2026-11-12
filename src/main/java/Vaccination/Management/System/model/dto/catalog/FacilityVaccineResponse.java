package Vaccination.Management.System.model.dto.catalog;

import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** One vaccine a facility can give today, with its price range there. Public: no batch data. */
@Data
@Builder
public class FacilityVaccineResponse {
    private Long vaccineId;
    private String vaccineName;
    private VaccineCategory category;
    private int totalDoses;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
