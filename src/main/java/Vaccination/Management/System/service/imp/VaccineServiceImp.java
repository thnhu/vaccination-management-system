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
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.entity.VaccineDisease;
import Vaccination.Management.System.model.entity.VaccineDiseaseId;
import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import Vaccination.Management.System.repository.DiseaseRepository;
import Vaccination.Management.System.repository.VaccineDiseaseRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.VaccineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineServiceImp implements VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccineDoseScheduleRepository vaccineDoseScheduleRepository;
    private final DiseaseRepository diseaseRepository;
    private final VaccineDiseaseRepository vaccineDiseaseRepository;

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
