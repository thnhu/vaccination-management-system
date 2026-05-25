package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineServiceImp implements VaccineService {

    private final VaccineRepository vaccineRepository;

    @Override
    public List<VaccineSummary> getAllVaccines() {
        return vaccineRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public VaccineResponse getVaccineById(Long id) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        return toResponse(vaccine);
    }

    // --- Mappers ---

    private VaccineSummary toSummary(Vaccine vaccine) {
        return VaccineSummary.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .category(vaccine.getCategory())
                .totalDoses(vaccine.getDoseSchedules().size())
                .active(vaccine.isActive())
                .build();
    }

    private VaccineResponse toResponse(Vaccine vaccine) {
        List<VaccineResponse.DoseScheduleInfo> schedules = vaccine.getDoseSchedules().stream()
                .map(s -> VaccineResponse.DoseScheduleInfo.builder()
                        .doseNumber(s.getDoseNumber())
                        .daysAfterPrevious(s.getDaysAfterPrevious())
                        .build())
                .toList();

        Set<VaccineResponse.DiseaseInfo> diseases = vaccine.getVaccineDiseases().stream()
                .map(vd -> VaccineResponse.DiseaseInfo.builder()
                        .id(vd.getDisease().getId())
                        .name(vd.getDisease().getName())
                        .build())
                .collect(Collectors.toSet());

        return VaccineResponse.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .category(vaccine.getCategory())
                .active(vaccine.isActive())
                .totalDoses(vaccine.getDoseSchedules().size())
                .doseSchedules(schedules)
                .diseases(diseases)
                .createdAt(vaccine.getCreatedAt())
                .updatedAt(vaccine.getUpdatedAt())
                .build();
    }
}
