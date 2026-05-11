package Vaccination.Management.System.model.dto.appointment;

import Vaccination.Management.System.model.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentSummary {
    private Long id;
    private String vaccineName;
    private String facilityName;
    private LocalDate preferredDate;
    private AppointmentStatus status;
}