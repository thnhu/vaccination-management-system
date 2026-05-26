package Vaccination.Management.System.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLoginRequest {

    @NotBlank(message = "INVALID_PHONE")
    private String phone;

    @NotBlank(message = "INVALID_PASSWORD")
    private String password;
}
