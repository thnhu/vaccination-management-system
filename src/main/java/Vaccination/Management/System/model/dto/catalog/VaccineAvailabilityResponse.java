package Vaccination.Management.System.model.dto.catalog;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * One facility where a vaccine can be given today. Public: deliberately carries no batch numbers
 * or stock counts — only that it is available and at what price range.
 */
@Data
@Builder
public class VaccineAvailabilityResponse {
    private Long facilityId;
    private String facilityName;
    private String address;
    private String provinceCode;
    private String provinceName;
    private String phone;
    private String openingHours;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
