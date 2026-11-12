package Vaccination.Management.System.model.dto.auth;

import Vaccination.Management.System.model.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRegisterRequest {

    @NotBlank(message = "INVALID_PHONE")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "INVALID_PHONE")
    private String phone;

    @NotBlank(message = "INVALID_PASSWORD")
    @Size(min = 8, message = "INVALID_PASSWORD")
    private String password;

    @NotBlank(message = "UNCATEGORIZED")
    private String fullName;

    private String email;

    // Identity fields: needed so staff can verify the person (name + date of birth) before vaccinating
    // and contact them afterwards. They create the citizen_profile together with the user.
    @NotNull(message = "INVALID_DATE_OF_BIRTH")
    @Past(message = "INVALID_DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @NotNull(message = "INVALID_GENDER")
    private Gender gender;

    @NotBlank(message = "INVALID_ID_CARD")
    @Pattern(regexp = "^[0-9]{12}$", message = "INVALID_ID_CARD")
    private String idCardNumber;

    @NotBlank(message = "INVALID_PROVINCE")
    private String provinceCode;
}
