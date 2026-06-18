package Vaccination.Management.System.model.dto.facility;

import Vaccination.Management.System.model.enums.FacilityType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFacilityRequest {

    @Size(max = 200)
    private String name;

    private FacilityType facilityType;

    @Size(max = 255)
    private String address;

    private String provinceCode;

    private String wardCode;

    @Size(max = 15)
    private String phone;
}
