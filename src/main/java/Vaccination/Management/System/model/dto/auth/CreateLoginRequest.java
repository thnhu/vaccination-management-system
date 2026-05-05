package Vaccination.Management.System.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLoginRequest {

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
}
