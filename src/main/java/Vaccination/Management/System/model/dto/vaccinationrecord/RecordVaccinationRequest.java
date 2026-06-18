package Vaccination.Management.System.model.dto.vaccinationrecord;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordVaccinationRequest {

    @NotNull(message = "APPOINTMENT_NOT_FOUND")
    private Long appointmentId;

    @NotNull(message = "BATCH_NOT_FOUND")
    private Long batchId;
}