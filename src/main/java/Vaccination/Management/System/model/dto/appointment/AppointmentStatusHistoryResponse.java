package Vaccination.Management.System.model.dto.appointment;

import Vaccination.Management.System.model.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentStatusHistoryResponse {
    private Long id;
    private AppointmentStatus fromStatus;
    private AppointmentStatus toStatus;
    private String reason;
    private String changedByName;
    private LocalDateTime changedAt;
}
