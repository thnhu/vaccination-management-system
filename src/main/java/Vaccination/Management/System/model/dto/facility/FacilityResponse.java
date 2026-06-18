package Vaccination.Management.System.model.dto.facility;

import Vaccination.Management.System.model.enums.FacilityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FacilityResponse {
    private Long id;
    private String name;
    private FacilityType facilityType;
    private String address;
    private String provinceCode;
    private String provinceName;
    private String wardCode;
    private String wardName;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
