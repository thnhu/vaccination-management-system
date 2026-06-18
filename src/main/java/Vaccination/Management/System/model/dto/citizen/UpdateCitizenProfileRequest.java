package Vaccination.Management.System.model.dto.citizen;

import Vaccination.Management.System.model.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCitizenProfileRequest {
    private Gender gender;

    @Size(max = 300)
    private String address;

    private String provinceCode;
    private String wardCode;
}
