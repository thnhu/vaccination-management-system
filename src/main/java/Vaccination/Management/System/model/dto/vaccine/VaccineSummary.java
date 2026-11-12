package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VaccineSummary {
    private Long id;
    private String name;
    private VaccineCategory category;
    private int totalDoses;
    private boolean active;

    // --- Public catalogue fields (null when not set) ---
    private List<VaccineResponse.DiseaseInfo> diseases;
    private String manufacturerName;
    private String countryOfOrigin;
    private Integer minAgeMonths;
    private Integer maxAgeMonths;

    /**
     * Price range and number of active facilities over batches that can actually be used today
     * (ACTIVE, remaining &gt; 0, not past expiry). Null prices / 0 facilities = not in stock anywhere.
     */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private long availableFacilityCount;
}
