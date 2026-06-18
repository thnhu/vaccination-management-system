package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.advisor.AvailableSlotResponse;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.FacilityCapacity;
import Vaccination.Management.System.repository.AppointmentRepository;
import Vaccination.Management.System.repository.FacilityCapacityRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.service.AvailableSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvailableSlotServiceImpl implements AvailableSlotService {

    private final FacilityRepository facilityRepository;
    private final FacilityCapacityRepository facilityCapacityRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public List<AvailableSlotResponse> getAvailableSlots(Long facilityId, LocalDate startDate, LocalDate endDate) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        List<AvailableSlotResponse> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            Optional<FacilityCapacity> capacityOpt =
                    facilityCapacityRepository.findActiveCapacity(facilityId, current);

            if (capacityOpt.isPresent()) {
                int maxSlots = capacityOpt.get().getMaxSlotsPerDay();
                long booked = appointmentRepository.countBookedSlots(facilityId, current);
                long available = Math.max(0L, maxSlots - booked);

                result.add(AvailableSlotResponse.builder()
                        .facilityId(facilityId)
                        .facilityName(facility.getName())
                        .date(current)
                        .maxSlots(maxSlots)
                        .bookedSlots(booked)
                        .availableSlots(available)
                        .build());
            }

            current = current.plusDays(1);
        }

        return result;
    }
}
