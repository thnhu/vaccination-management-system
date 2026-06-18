package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface AvailableSlotService {
    List<AvailableSlotResponse> getAvailableSlots(Long facilityId, LocalDate startDate, LocalDate endDate);
}
