package Vaccination.Management.System.model.dto.advisor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdvisorChatRequest {
    @NotBlank(message = "Message must not be blank")
    private String message;

    private String sessionId;
}
