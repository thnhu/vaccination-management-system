package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccine.AssignDiseaseRequest;
import Vaccination.Management.System.model.dto.vaccine.CreateDoseScheduleRequest;
import Vaccination.Management.System.model.dto.vaccine.CreateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.UpdateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.model.entity.Disease;
import Vaccination.Management.System.model.entity.Manufacturer;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.entity.VaccineDisease;
import Vaccination.Management.System.model.entity.VaccineDiseaseId;
import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import Vaccination.Management.System.model.enums.BatchStatus;
import Vaccination.Management.System.repository.DiseaseRepository;
import Vaccination.Management.System.repository.ManufacturerRepository;
import Vaccination.Management.System.repository.StockPriceRow;
import Vaccination.Management.System.repository.VaccineBatchRepository;
import Vaccination.Management.System.repository.VaccineDiseaseRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineServiceImp implements VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccineDoseScheduleRepository vaccineDoseScheduleRepository;
    private final DiseaseRepository diseaseRepository;
    private final VaccineDiseaseRepository vaccineDiseaseRepository;
    private final VaccineBatchRepository vaccineBatchRepository;
    private final ManufacturerRepository manufacturerRepository;

    @Override
    public List<VaccineSummary> getAllVaccines() {
        // One aggregate query for price/stock of every vaccine, instead of one per vaccine.
        Map<Long, StockPriceRow> stock = vaccineBatchRepository
                .summarizeUsableStockByVaccine(BatchStatus.ACTIVE, LocalDate.now())
                .stream()
                .collect(Collectors.toMap(StockPriceRow::getKeyId, Function.identity()));

        return vaccineRepository.findAll()
                .stream()
                .map(v -> toSummary(v, stock.get(v.getId())))
                .toList();
    }

    @Override
    public VaccineResponse getVaccineById(Long id) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        return toResponse(vaccine);
    }

    @Override
    @Transactional
    public VaccineResponse createVaccine(CreateVaccineRequest request) {
        if (vaccineRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.VACCINE_EXISTED);
        }
        Vaccine vaccine = Vaccine.builder()
                .name(request.getName())
                .category(request.getCategory())
                .build();
        vaccine = vaccineRepository.save(vaccine);
        return toResponse(vaccine);
    }

    @Override
    @Transactional
    public VaccineResponse updateVaccine(Long id, UpdateVaccineRequest request) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        if (request.getName() != null) {
            if (vaccineRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new AppException(ErrorCode.VACCINE_EXISTED);
            }
            vaccine.setName(request.getName());
        }

        if (request.getCategory() != null) {
            vaccine.setCategory(request.getCategory());
        }

        if (request.getManufacturerId() != null) {
            Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                    .orElseThrow(() -> new AppException(ErrorCode.MANUFACTURER_NOT_FOUND));
            vaccine.setManufacturer(manufacturer);
        }

        // Content fields: null = unchanged, "" = clear.
        if (request.getDescription() != null) vaccine.setDescription(blankToNull(request.getDescription()));
        if (request.getRoute() != null) vaccine.setRoute(blankToNull(request.getRoute()));
        if (request.getContraindications() != null) vaccine.setContraindications(blankToNull(request.getContraindications()));
        if (request.getPrecautions() != null) vaccine.setPrecautions(blankToNull(request.getPrecautions()));
        if (request.getSideEffects() != null) vaccine.setSideEffects(blankToNull(request.getSideEffects()));
        if (request.getStorage() != null) vaccine.setStorage(blankToNull(request.getStorage()));
        if (request.getMinAgeMonths() != null) vaccine.setMinAgeMonths(request.getMinAgeMonths());
        if (request.getMaxAgeMonths() != null) vaccine.setMaxAgeMonths(request.getMaxAgeMonths());

        // Validate the resulting range, so a partial update cannot leave min > max.
        if (vaccine.getMinAgeMonths() != null && vaccine.getMaxAgeMonths() != null
                && vaccine.getMinAgeMonths() > vaccine.getMaxAgeMonths()) {
            throw new AppException(ErrorCode.INVALID_AGE_RANGE);
        }

        vaccine = vaccineRepository.save(vaccine);
        return toResponse(vaccine);
    }

    @Override
    @Transactional
    public VaccineResponse deactivateVaccine(Long id) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        if (!vaccine.isActive()) {
            throw new AppException(ErrorCode.VACCINE_ALREADY_INACTIVE);
        }

        vaccine.setActive(false);
        vaccine = vaccineRepository.save(vaccine);
        return toResponse(vaccine);
    }

    @Override
    @Transactional
    public VaccineResponse addDoseSchedule(Long vaccineId, CreateDoseScheduleRequest request) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        if (vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(vaccineId, request.getDoseNumber()).isPresent()) {
            throw new AppException(ErrorCode.DOSE_SCHEDULE_DUPLICATE);
        }

        VaccineDoseSchedule schedule = VaccineDoseSchedule.builder()
                .vaccine(vaccine)
                .doseNumber(request.getDoseNumber())
                .daysAfterPrevious(request.getDaysAfterPrevious())
                .build();

        vaccineDoseScheduleRepository.save(schedule);

        Vaccine fresh = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        return toResponse(fresh);
    }

    @Override
    @Transactional
    public VaccineResponse assignDisease(Long vaccineId, AssignDiseaseRequest request) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        Disease disease = diseaseRepository.findById(request.getDiseaseId())
                .orElseThrow(() -> new AppException(ErrorCode.DISEASE_NOT_FOUND));

        VaccineDiseaseId compositeId = new VaccineDiseaseId(vaccineId, request.getDiseaseId());
        if (vaccineDiseaseRepository.existsById(compositeId)) {
            throw new AppException(ErrorCode.VACCINE_DISEASE_ALREADY_ASSIGNED);
        }

        VaccineDisease vaccineDisease = VaccineDisease.builder()
                .id(compositeId)
                .vaccine(vaccine)
                .disease(disease)
                .build();

        vaccineDiseaseRepository.save(vaccineDisease);

        Vaccine fresh = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        return toResponse(fresh);
    }

    // --- Mappers ---

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static List<VaccineResponse.DiseaseInfo> diseaseInfos(Vaccine vaccine) {
        return vaccine.getVaccineDiseases().stream()
                .map(vd -> VaccineResponse.DiseaseInfo.builder()
                        .id(vd.getDisease().getId())
                        .name(vd.getDisease().getName())
                        .build())
                .sorted(Comparator.comparing(VaccineResponse.DiseaseInfo::getName))
                .toList();
    }

    private VaccineSummary toSummary(Vaccine vaccine, StockPriceRow stock) {
        Manufacturer m = vaccine.getManufacturer();
        return VaccineSummary.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .category(vaccine.getCategory())
                .totalDoses(vaccine.getDoseSchedules().size())
                .active(vaccine.isActive())
                .diseases(diseaseInfos(vaccine))
                .manufacturerName(m != null ? m.getName() : null)
                .countryOfOrigin(m != null ? m.getCountryOfOrigin() : null)
                .minAgeMonths(vaccine.getMinAgeMonths())
                .maxAgeMonths(vaccine.getMaxAgeMonths())
                .minPrice(stock != null ? stock.getMinPrice() : null)
                .maxPrice(stock != null ? stock.getMaxPrice() : null)
                .availableFacilityCount(stock != null && stock.getFacilityCount() != null ? stock.getFacilityCount() : 0)
                .build();
    }

    private VaccineResponse toResponse(Vaccine vaccine) {
        List<VaccineResponse.DoseScheduleInfo> schedules = vaccine.getDoseSchedules().stream()
                .map(s -> VaccineResponse.DoseScheduleInfo.builder()
                        .doseNumber(s.getDoseNumber())
                        .daysAfterPrevious(s.getDaysAfterPrevious())
                        .build())
                .toList();

        Set<VaccineResponse.DiseaseInfo> diseases = new LinkedHashSet<>(diseaseInfos(vaccine));
        Manufacturer m = vaccine.getManufacturer();

        return VaccineResponse.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .category(vaccine.getCategory())
                .active(vaccine.isActive())
                .totalDoses(vaccine.getDoseSchedules().size())
                .doseSchedules(schedules)
                .diseases(diseases)
                .manufacturerId(m != null ? m.getId() : null)
                .manufacturerName(m != null ? m.getName() : null)
                .countryOfOrigin(m != null ? m.getCountryOfOrigin() : null)
                .description(vaccine.getDescription())
                .minAgeMonths(vaccine.getMinAgeMonths())
                .maxAgeMonths(vaccine.getMaxAgeMonths())
                .route(vaccine.getRoute())
                .contraindications(vaccine.getContraindications())
                .precautions(vaccine.getPrecautions())
                .sideEffects(vaccine.getSideEffects())
                .storage(vaccine.getStorage())
                .createdAt(vaccine.getCreatedAt())
                .updatedAt(vaccine.getUpdatedAt())
                .build();
    }
}
