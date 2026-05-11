package Vaccination.Management.System.model.dto.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAppointmentRequest {

    @NotNull(message = "VACCINE_NOT_FOUND")
    private Long vaccineId;

    @NotNull(message = "FACILITY_NOT_FOUND")
    private Long facilityId;

    @NotNull
    @FutureOrPresent
    private LocalDate preferredDate;
}
