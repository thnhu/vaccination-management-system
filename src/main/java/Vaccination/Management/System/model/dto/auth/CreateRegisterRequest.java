package Vaccination.Management.System.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
}
