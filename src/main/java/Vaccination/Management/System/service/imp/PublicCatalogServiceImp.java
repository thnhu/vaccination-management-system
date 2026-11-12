package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.catalog.FacilityVaccineResponse;
import Vaccination.Management.System.model.dto.catalog.VaccineAvailabilityResponse;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.enums.BatchStatus;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.repository.StockPriceRow;
import Vaccination.Management.System.repository.VaccineBatchRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.PublicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCatalogServiceImp implements PublicCatalogService {

    private final VaccineRepository vaccineRepository;
    private final FacilityRepository facilityRepository;
    private final VaccineBatchRepository vaccineBatchRepository;

    @Override
    public List<VaccineAvailabilityResponse> getVaccineAvailability(Long vaccineId) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        // A withdrawn vaccine is not offered anywhere, whatever stock is left.
        if (!vaccine.isActive()) return List.of();

        Map<Long, StockPriceRow> byFacility = vaccineBatchRepository
                .usableStockByFacilityForVaccine(vaccineId, BatchStatus.ACTIVE, LocalDate.now())
                .stream()
                .collect(Collectors.toMap(StockPriceRow::getKeyId, Function.identity()));
        if (byFacility.isEmpty()) return List.of();

        return facilityRepository.findAllById(byFacility.keySet()).stream()
                .filter(Facility::isActive)
                .sorted(Comparator.comparing((Facility f) -> f.getProvince().getName()).thenComparing(Facility::getName))
                .map(f -> {
                    StockPriceRow row = byFacility.get(f.getId());
                    return VaccineAvailabilityResponse.builder()
                            .facilityId(f.getId())
                            .facilityName(f.getName())
                            .address(f.getAddress())
                            .provinceCode(f.getProvince().getCode())
                            .provinceName(f.getProvince().getName())
                            .phone(f.getPhone())
                            .openingHours(f.getOpeningHours())
                            .minPrice(row.getMinPrice())
                            .maxPrice(row.getMaxPrice())
                            .build();
                })
                .toList();
    }

    @Override
    public List<FacilityVaccineResponse> getFacilityVaccines(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));
        if (!facility.isActive()) return List.of();

        Map<Long, StockPriceRow> byVaccine = vaccineBatchRepository
                .usableStockByVaccineForFacility(facilityId, BatchStatus.ACTIVE, LocalDate.now())
                .stream()
                .collect(Collectors.toMap(StockPriceRow::getKeyId, Function.identity()));
        if (byVaccine.isEmpty()) return List.of();

        return vaccineRepository.findAllById(byVaccine.keySet()).stream()
                .filter(Vaccine::isActive)
                .sorted(Comparator.comparing(Vaccine::getName))
                .map(v -> {
                    StockPriceRow row = byVaccine.get(v.getId());
                    return FacilityVaccineResponse.builder()
                            .vaccineId(v.getId())
                            .vaccineName(v.getName())
                            .category(v.getCategory())
                            .totalDoses(v.getDoseSchedules().size())
                            .minPrice(row.getMinPrice())
                            .maxPrice(row.getMaxPrice())
                            .build();
                })
                .toList();
    }
}
