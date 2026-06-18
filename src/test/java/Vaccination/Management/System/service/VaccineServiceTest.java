package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;
import Vaccination.Management.System.model.entity.Disease;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.entity.VaccineDisease;
import Vaccination.Management.System.model.entity.VaccineDiseaseId;
import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import Vaccination.Management.System.model.enums.VaccineCategory;
import Vaccination.Management.System.model.dto.vaccine.AssignDiseaseRequest;
import Vaccination.Management.System.model.dto.vaccine.CreateDoseScheduleRequest;
import Vaccination.Management.System.model.dto.vaccine.CreateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.UpdateVaccineRequest;
import Vaccination.Management.System.model.entity.VaccineDiseaseId;
import Vaccination.Management.System.repository.DiseaseRepository;
import Vaccination.Management.System.repository.VaccineDiseaseRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.imp.VaccineServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaccineServiceTest {

    @Mock private VaccineRepository vaccineRepository;
    @Mock private VaccineDoseScheduleRepository vaccineDoseScheduleRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private VaccineDiseaseRepository vaccineDiseaseRepository;

    @InjectMocks
    private VaccineServiceImp vaccineService;

    // getAllVaccines

    @Test
    void getAllVaccines_returnsSummaryList() {
        Vaccine vaccine = buildVaccine(1L, "Vắc xin A", VaccineCategory.CHILD, true, 2);
        when(vaccineRepository.findAll()).thenReturn(List.of(vaccine));

        List<VaccineSummary> result = vaccineService.getAllVaccines();

        assertThat(result).hasSize(1);
        VaccineSummary summary = result.get(0);
        assertThat(summary.getId()).isEqualTo(1L);
        assertThat(summary.getName()).isEqualTo("Vắc xin A");
        assertThat(summary.getCategory()).isEqualTo(VaccineCategory.CHILD);
        assertThat(summary.getTotalDoses()).isEqualTo(2);
        assertThat(summary.isActive()).isTrue();
    }

    @Test
    void getAllVaccines_returnsEmptyList_whenNoVaccines() {
        when(vaccineRepository.findAll()).thenReturn(Collections.emptyList());

        List<VaccineSummary> result = vaccineService.getAllVaccines();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllVaccines_inactiveVaccineIncluded() {
        Vaccine inactive = buildVaccine(2L, "Vắc xin B", VaccineCategory.ADULT, false, 1);
        when(vaccineRepository.findAll()).thenReturn(List.of(inactive));

        List<VaccineSummary> result = vaccineService.getAllVaccines();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isFalse();
    }

    @Test
    void getAllVaccines_totalDosesReflectsDoseScheduleCount() {
        Vaccine vaccine = buildVaccine(3L, "Vắc xin C", VaccineCategory.ELDERLY, true, 3);
        when(vaccineRepository.findAll()).thenReturn(List.of(vaccine));

        List<VaccineSummary> result = vaccineService.getAllVaccines();

        assertThat(result.get(0).getTotalDoses()).isEqualTo(3);
    }

    // getVaccineById

    @Test
    void getVaccineById_returnsDetailedResponse() {
        Disease disease = Disease.builder().id(10L).name("Cúm mùa").build();
        VaccineDisease vd = VaccineDisease.builder()
                .id(new VaccineDiseaseId(1L, 10L))
                .disease(disease)
                .build();

        VaccineDoseSchedule dose1 = VaccineDoseSchedule.builder()
                .doseNumber(1).daysAfterPrevious(null).build();
        VaccineDoseSchedule dose2 = VaccineDoseSchedule.builder()
                .doseNumber(2).daysAfterPrevious(30).build();

        Vaccine vaccine = Vaccine.builder()
                .id(1L)
                .name("Vắc xin A")
                .category(VaccineCategory.CHILD)
                .active(true)
                .doseSchedules(List.of(dose1, dose2))
                .vaccineDiseases(Set.of(vd))
                .build();

        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(vaccine));

        VaccineResponse response = vaccineService.getVaccineById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Vắc xin A");
        assertThat(response.getCategory()).isEqualTo(VaccineCategory.CHILD);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getTotalDoses()).isEqualTo(2);

        assertThat(response.getDoseSchedules()).hasSize(2);
        assertThat(response.getDoseSchedules().get(0).getDoseNumber()).isEqualTo(1);
        assertThat(response.getDoseSchedules().get(0).getDaysAfterPrevious()).isNull();
        assertThat(response.getDoseSchedules().get(1).getDoseNumber()).isEqualTo(2);
        assertThat(response.getDoseSchedules().get(1).getDaysAfterPrevious()).isEqualTo(30);

        assertThat(response.getDiseases()).hasSize(1);
        VaccineResponse.DiseaseInfo diseaseInfo = response.getDiseases().iterator().next();
        assertThat(diseaseInfo.getId()).isEqualTo(10L);
        assertThat(diseaseInfo.getName()).isEqualTo("Cúm mùa");
    }

    @Test
    void getVaccineById_throwsAppException_whenNotFound() {
        when(vaccineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.getVaccineById(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VACCINE_NOT_FOUND);
                });
    }

    @Test
    void getVaccineById_noDosesAndNoDiseases_returnEmptyCollections() {
        Vaccine vaccine = Vaccine.builder()
                .id(5L)
                .name("Vắc xin mới")
                .category(VaccineCategory.ALL_AGES)
                .active(true)
                .build();

        when(vaccineRepository.findById(5L)).thenReturn(Optional.of(vaccine));

        VaccineResponse response = vaccineService.getVaccineById(5L);

        assertThat(response.getDoseSchedules()).isEmpty();
        assertThat(response.getDiseases()).isEmpty();
        assertThat(response.getTotalDoses()).isZero();
    }

    // createVaccine

    @Test
    void createVaccine_success() {
        CreateVaccineRequest req = new CreateVaccineRequest();
        req.setName("Vaccine Mới");
        req.setCategory(VaccineCategory.CHILD);

        when(vaccineRepository.existsByName("Vaccine Mới")).thenReturn(false);
        Vaccine saved = buildVaccine(10L, "Vaccine Mới", VaccineCategory.CHILD, true, 0);
        when(vaccineRepository.save(any(Vaccine.class))).thenReturn(saved);

        VaccineResponse response = vaccineService.createVaccine(req);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Vaccine Mới");
        assertThat(response.getCategory()).isEqualTo(VaccineCategory.CHILD);
        verify(vaccineRepository).save(any(Vaccine.class));
    }

    @Test
    void createVaccine_throwsException_whenDuplicateName() {
        CreateVaccineRequest req = new CreateVaccineRequest();
        req.setName("Vaccine Trùng");
        req.setCategory(VaccineCategory.ADULT);

        when(vaccineRepository.existsByName("Vaccine Trùng")).thenReturn(true);

        assertThatThrownBy(() -> vaccineService.createVaccine(req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_EXISTED));
    }

    // updateVaccine

    @Test
    void updateVaccine_success_withAllFields() {
        Vaccine existing = buildVaccine(1L, "Tên Cũ", VaccineCategory.CHILD, true, 0);
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vaccineRepository.existsByNameAndIdNot("Tên Mới", 1L)).thenReturn(false);
        Vaccine saved = buildVaccine(1L, "Tên Mới", VaccineCategory.ADULT, true, 0);
        when(vaccineRepository.save(any(Vaccine.class))).thenReturn(saved);

        UpdateVaccineRequest req = new UpdateVaccineRequest();
        req.setName("Tên Mới");
        req.setCategory(VaccineCategory.ADULT);

        VaccineResponse response = vaccineService.updateVaccine(1L, req);

        assertThat(response.getName()).isEqualTo("Tên Mới");
        assertThat(response.getCategory()).isEqualTo(VaccineCategory.ADULT);
        verify(vaccineRepository).save(any(Vaccine.class));
    }

    @Test
    void updateVaccine_success_withNullFields_doesNotOverwrite() {
        Vaccine existing = buildVaccine(2L, "Tên Giữ Nguyên", VaccineCategory.ELDERLY, true, 0);
        when(vaccineRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(vaccineRepository.save(any(Vaccine.class))).thenReturn(existing);

        UpdateVaccineRequest req = new UpdateVaccineRequest();

        VaccineResponse response = vaccineService.updateVaccine(2L, req);

        assertThat(response.getName()).isEqualTo("Tên Giữ Nguyên");
        assertThat(response.getCategory()).isEqualTo(VaccineCategory.ELDERLY);
    }

    @Test
    void updateVaccine_throwsException_whenNotFound() {
        when(vaccineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.updateVaccine(99L, new UpdateVaccineRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void updateVaccine_throwsException_whenDuplicateName() {
        Vaccine existing = buildVaccine(3L, "Tên Cũ", VaccineCategory.CHILD, true, 0);
        when(vaccineRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(vaccineRepository.existsByNameAndIdNot("Tên Trùng", 3L)).thenReturn(true);

        UpdateVaccineRequest req = new UpdateVaccineRequest();
        req.setName("Tên Trùng");

        assertThatThrownBy(() -> vaccineService.updateVaccine(3L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_EXISTED));
    }

    // deactivateVaccine

    @Test
    void deactivateVaccine_success() {
        Vaccine active = buildVaccine(4L, "Vaccine Active", VaccineCategory.CHILD, true, 1);
        when(vaccineRepository.findById(4L)).thenReturn(Optional.of(active));
        Vaccine deactivated = buildVaccine(4L, "Vaccine Active", VaccineCategory.CHILD, false, 1);
        when(vaccineRepository.save(any(Vaccine.class))).thenReturn(deactivated);

        VaccineResponse response = vaccineService.deactivateVaccine(4L);

        assertThat(response.isActive()).isFalse();
        verify(vaccineRepository).save(any(Vaccine.class));
    }

    @Test
    void deactivateVaccine_throwsException_whenNotFound() {
        when(vaccineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.deactivateVaccine(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void deactivateVaccine_throwsException_whenAlreadyInactive() {
        Vaccine inactive = buildVaccine(5L, "Vaccine Inactive", VaccineCategory.ADULT, false, 0);
        when(vaccineRepository.findById(5L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> vaccineService.deactivateVaccine(5L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_ALREADY_INACTIVE));
    }

    // addDoseSchedule

    @Test
    void addDoseSchedule_success() {
        Vaccine vaccine = buildVaccine(6L, "Vaccine Dose", VaccineCategory.CHILD, true, 1);
        when(vaccineRepository.findById(6L)).thenReturn(Optional.of(vaccine));
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(6L, 2)).thenReturn(Optional.empty());
        Vaccine fresh = buildVaccine(6L, "Vaccine Dose", VaccineCategory.CHILD, true, 2);
        when(vaccineRepository.findById(6L)).thenReturn(Optional.of(vaccine)).thenReturn(Optional.of(fresh));

        CreateDoseScheduleRequest req = new CreateDoseScheduleRequest();
        req.setDoseNumber(2);
        req.setDaysAfterPrevious(30);

        VaccineResponse response = vaccineService.addDoseSchedule(6L, req);

        assertThat(response.getTotalDoses()).isEqualTo(2);
        verify(vaccineDoseScheduleRepository).save(any(VaccineDoseSchedule.class));
    }

    @Test
    void addDoseSchedule_throwsException_whenVaccineNotFound() {
        when(vaccineRepository.findById(99L)).thenReturn(Optional.empty());

        CreateDoseScheduleRequest req = new CreateDoseScheduleRequest();
        req.setDoseNumber(1);

        assertThatThrownBy(() -> vaccineService.addDoseSchedule(99L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void addDoseSchedule_throwsException_whenDuplicateDoseNumber() {
        Vaccine vaccine = buildVaccine(7L, "Vaccine X", VaccineCategory.CHILD, true, 1);
        when(vaccineRepository.findById(7L)).thenReturn(Optional.of(vaccine));
        VaccineDoseSchedule existing = VaccineDoseSchedule.builder().doseNumber(1).build();
        when(vaccineDoseScheduleRepository.findByVaccineIdAndDoseNumber(7L, 1))
                .thenReturn(Optional.of(existing));

        CreateDoseScheduleRequest req = new CreateDoseScheduleRequest();
        req.setDoseNumber(1);

        assertThatThrownBy(() -> vaccineService.addDoseSchedule(7L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DOSE_SCHEDULE_DUPLICATE));
    }

    // assignDisease

    @Test
    void assignDisease_success() {
        Vaccine vaccine = buildVaccine(8L, "Vaccine Y", VaccineCategory.ALL_AGES, true, 0);
        Disease disease = Disease.builder().id(20L).name("Cúm mùa").build();
        VaccineDiseaseId compositeId = new VaccineDiseaseId(8L, 20L);

        when(vaccineRepository.findById(8L)).thenReturn(Optional.of(vaccine));
        when(diseaseRepository.findById(20L)).thenReturn(Optional.of(disease));
        when(vaccineDiseaseRepository.existsById(compositeId)).thenReturn(false);

        Vaccine fresh = buildVaccine(8L, "Vaccine Y", VaccineCategory.ALL_AGES, true, 0);
        VaccineDisease vd = VaccineDisease.builder()
                .id(compositeId).vaccine(fresh).disease(disease).build();
        fresh.setVaccineDiseases(Set.of(vd));
        when(vaccineRepository.findById(8L)).thenReturn(Optional.of(vaccine)).thenReturn(Optional.of(fresh));

        AssignDiseaseRequest req = new AssignDiseaseRequest();
        req.setDiseaseId(20L);

        VaccineResponse response = vaccineService.assignDisease(8L, req);

        assertThat(response.getDiseases()).hasSize(1);
        assertThat(response.getDiseases().iterator().next().getId()).isEqualTo(20L);
        verify(vaccineDiseaseRepository).save(any(VaccineDisease.class));
    }

    @Test
    void assignDisease_throwsException_whenVaccineNotFound() {
        when(vaccineRepository.findById(99L)).thenReturn(Optional.empty());

        AssignDiseaseRequest req = new AssignDiseaseRequest();
        req.setDiseaseId(1L);

        assertThatThrownBy(() -> vaccineService.assignDisease(99L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void assignDisease_throwsException_whenDiseaseNotFound() {
        Vaccine vaccine = buildVaccine(9L, "Vaccine Z", VaccineCategory.CHILD, true, 0);
        when(vaccineRepository.findById(9L)).thenReturn(Optional.of(vaccine));
        when(diseaseRepository.findById(99L)).thenReturn(Optional.empty());

        AssignDiseaseRequest req = new AssignDiseaseRequest();
        req.setDiseaseId(99L);

        assertThatThrownBy(() -> vaccineService.assignDisease(9L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DISEASE_NOT_FOUND));
    }

    @Test
    void assignDisease_throwsException_whenAlreadyAssigned() {
        Vaccine vaccine = buildVaccine(10L, "Vaccine W", VaccineCategory.ADULT, true, 0);
        Disease disease = Disease.builder().id(30L).name("Sởi").build();
        VaccineDiseaseId compositeId = new VaccineDiseaseId(10L, 30L);

        when(vaccineRepository.findById(10L)).thenReturn(Optional.of(vaccine));
        when(diseaseRepository.findById(30L)).thenReturn(Optional.of(disease));
        when(vaccineDiseaseRepository.existsById(compositeId)).thenReturn(true);

        AssignDiseaseRequest req = new AssignDiseaseRequest();
        req.setDiseaseId(30L);

        assertThatThrownBy(() -> vaccineService.assignDisease(10L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.VACCINE_DISEASE_ALREADY_ASSIGNED));
    }

    // helpers

    private Vaccine buildVaccine(Long id, String name, VaccineCategory category,
                                  boolean active, int doseCount) {
        List<VaccineDoseSchedule> schedules = new java.util.ArrayList<>();
        for (int i = 1; i <= doseCount; i++) {
            schedules.add(VaccineDoseSchedule.builder()
                    .doseNumber(i)
                    .daysAfterPrevious(i == 1 ? null : 30)
                    .build());
        }
        return Vaccine.builder()
                .id(id)
                .name(name)
                .category(category)
                .active(active)
                .doseSchedules(schedules)
                .build();
    }
}
