package Vaccination.Management.System.model.dto.appointment;

import Vaccination.Management.System.model.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long citizenId;
    private String citizenName;
    private Long vaccineId;
    private String vaccineName;
    private Long facilityId;
    private String facilityName;
    private LocalDate preferredDate;
    private AppointmentStatus status;
    private Long confirmedById;
    private String confirmedByName;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
