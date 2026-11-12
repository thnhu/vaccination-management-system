package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.catalog.FacilityVaccineResponse;
import Vaccination.Management.System.model.dto.catalog.VaccineAvailabilityResponse;
import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.Vaccine;
import Vaccination.Management.System.model.enums.BatchStatus;
import Vaccination.Management.System.model.enums.VaccineCategory;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.repository.VaccineBatchRepository;
import Vaccination.Management.System.repository.VaccineRepository;
import Vaccination.Management.System.service.imp.PublicCatalogServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static Vaccination.Management.System.service.VaccineServiceTest.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCatalogServiceTest {

    @Mock private VaccineRepository vaccineRepository;
    @Mock private FacilityRepository facilityRepository;
    @Mock private VaccineBatchRepository vaccineBatchRepository;

    @InjectMocks private PublicCatalogServiceImp service;

    private static final AdministrativeUnit HN = AdministrativeUnit.builder().code("01").name("Hà Nội").build();
    private static final AdministrativeUnit HCM = AdministrativeUnit.builder().code("79").name("Hồ Chí Minh").build();

    @Test
    void availability_listsFacilitiesWithUsableStock_sortedByProvinceThenName_withoutBatchData() {
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(vaccine(1L, true)));
        when(vaccineBatchRepository.usableStockByFacilityForVaccine(eq(1L), eq(BatchStatus.ACTIVE), any()))
                .thenReturn(List.of(row(10L, "300000", "320000", 1L), row(20L, "310000", "310000", 1L)));
        when(facilityRepository.findAllById(any())).thenReturn(List.of(
                facility(20L, "B", HN, true), facility(10L, "A", HCM, true)));

        List<VaccineAvailabilityResponse> res = service.getVaccineAvailability(1L);

        assertThat(res).extracting(VaccineAvailabilityResponse::getFacilityName).containsExactly("B", "A");
        assertThat(res.get(1).getMinPrice()).isEqualByComparingTo("300000");
        assertThat(res.get(1).getMaxPrice()).isEqualByComparingTo("320000");
        assertThat(res.get(0).getOpeningHours()).isEqualTo("7:30–17:00");
    }

    @Test
    void availability_usesTodayForTheExpiryCutoff() {
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(vaccine(1L, true)));
        ArgumentCaptor<LocalDate> today = ArgumentCaptor.forClass(LocalDate.class);
        when(vaccineBatchRepository.usableStockByFacilityForVaccine(eq(1L), eq(BatchStatus.ACTIVE), today.capture()))
                .thenReturn(List.of());

        assertThat(service.getVaccineAvailability(1L)).isEmpty();
        assertThat(today.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void availability_inactiveVaccine_isEmpty_withoutQueryingStock() {
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(vaccine(1L, false)));

        assertThat(service.getVaccineAvailability(1L)).isEmpty();
        verify(vaccineBatchRepository, never()).usableStockByFacilityForVaccine(any(), any(), any());
    }

    @Test
    void availability_unknownVaccine_throws404() {
        when(vaccineRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVaccineAvailability(9L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VACCINE_NOT_FOUND));
    }

    @Test
    void facilityVaccines_listsActiveVaccinesWithStock_sortedByName() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility(10L, "A", HN, true)));
        when(vaccineBatchRepository.usableStockByVaccineForFacility(eq(10L), eq(BatchStatus.ACTIVE), any()))
                .thenReturn(List.of(row(1L, "100", "100", 1L), row(2L, "200", "250", 1L)));
        Vaccine zeta = vaccine(1L, true);
        zeta.setName("Zeta");
        Vaccine alpha = vaccine(2L, true);
        alpha.setName("Alpha");
        when(vaccineRepository.findAllById(any())).thenReturn(List.of(zeta, alpha));

        List<FacilityVaccineResponse> res = service.getFacilityVaccines(10L);

        assertThat(res).extracting(FacilityVaccineResponse::getVaccineName).containsExactly("Alpha", "Zeta");
        assertThat(res.get(0).getMaxPrice()).isEqualByComparingTo("250");
    }

    @Test
    void facilityVaccines_inactiveFacility_isEmpty() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility(10L, "A", HN, false)));

        assertThat(service.getFacilityVaccines(10L)).isEmpty();
        verify(vaccineBatchRepository, never()).usableStockByVaccineForFacility(any(), any(), any());
    }

    @Test
    void facilityVaccines_unknownFacility_throws404() {
        when(facilityRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFacilityVaccines(9L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    private static Vaccine vaccine(Long id, boolean active) {
        return Vaccine.builder().id(id).name("V" + id).category(VaccineCategory.ADULT).active(active).build();
    }

    private static Facility facility(Long id, String name, AdministrativeUnit province, boolean active) {
        return Facility.builder().id(id).name(name).address("addr").province(province)
                .phone("0900").openingHours("7:30–17:00").active(active).build();
    }
}
