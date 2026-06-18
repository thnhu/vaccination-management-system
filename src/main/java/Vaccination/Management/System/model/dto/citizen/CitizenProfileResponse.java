package Vaccination.Management.System.model.dto.citizen;

import Vaccination.Management.System.model.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CitizenProfileResponse {
    private Long userId;
    private String phone;
    private String email;
    private String fullName;
    private String idCardNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String provinceCode;
    private String provinceName;
    private String wardCode;
    private String wardName;
}
