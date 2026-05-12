package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.entity.Disease;
import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class VaccineResponse {
    private Long id;
    private String name;
    private String scientificName;
    private String manufacturer;
    private String countryOfOrigin;
    private Integer requiredDoses;
    private Integer daysBetweenDoses;
    private VaccineCategory category;
    private BigDecimal price;
    private boolean active;
    private Set<Disease> diseases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}