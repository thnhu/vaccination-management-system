package Vaccination.Management.System.model.dto.vaccine;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDiseaseRequest {

    @NotNull
    private Long diseaseId;
}
