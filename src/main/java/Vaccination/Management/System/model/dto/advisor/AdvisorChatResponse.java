package Vaccination.Management.System.model.dto.advisor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorChatResponse {
    private String response;
    private String sessionId;
}
