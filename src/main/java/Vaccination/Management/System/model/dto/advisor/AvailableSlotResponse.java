package Vaccination.Management.System.model.dto.advisor;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AvailableSlotResponse {
    private Long facilityId;
    private String facilityName;
    private LocalDate date;
    private int maxSlots;
    private long bookedSlots;
    private long availableSlots;
}
