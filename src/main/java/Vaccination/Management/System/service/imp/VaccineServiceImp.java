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
                .countryOfOrigin(vaccine.getCountryOfOrigin())
                .requiredDoses(vaccine.getRequiredDoses())
                .category(vaccine.getCategory())
                .active(vaccine.isActive())
                .build();
    }

    private VaccineResponse toResponse(Vaccine vaccine) {
        return VaccineResponse.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .scientificName(vaccine.getScientificName())
                .manufacturer(vaccine.getManufacturer())
                .requiredDoses(vaccine.getRequiredDoses())
                .daysBetweenDoses(vaccine.getDaysBetweenDoses())
                .category(vaccine.getCategory())
                .active(vaccine.isActive())
                .createdAt(vaccine.getCreatedAt())
                .updatedAt(vaccine.getUpdatedAt())
                .build();
    }
}