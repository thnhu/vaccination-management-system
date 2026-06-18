package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.facility.CreateFacilityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityResponse;
import Vaccination.Management.System.model.dto.facility.FacilityResponse;
import Vaccination.Management.System.model.dto.facility.UpdateFacilityRequest;
import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.FacilityCapacity;
import Vaccination.Management.System.model.enums.AdministrativeUnitType;
import Vaccination.Management.System.model.enums.FacilityType;
import Vaccination.Management.System.repository.AdministrativeUnitRepository;
import Vaccination.Management.System.repository.FacilityCapacityRepository;
import Vaccination.Management.System.repository.FacilityRepository;
import Vaccination.Management.System.service.imp.FacilityServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock private FacilityRepository facilityRepository;
    @Mock private FacilityCapacityRepository facilityCapacityRepository;
    @Mock private AdministrativeUnitRepository administrativeUnitRepository;

    @InjectMocks private FacilityServiceImp facilityService;

    // ── getAllFacilities ───────────────────────────────────────────────────────

    @Test
    void getAllFacilities_returnsEmptyList_whenNoFacilities() {
        when(facilityRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(facilityService.getAllFacilities()).isEmpty();
    }

    @Test
    void getAllFacilities_returnsListWithCorrectFields() {
        when(facilityRepository.findAll()).thenReturn(List.of(
                buildFacility(1L, "Bệnh viện A", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null)
        ));

        List<FacilityResponse> result = facilityService.getAllFacilities();

        assertThat(result).hasSize(1);
        FacilityResponse r = result.get(0);
        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getName()).isEqualTo("Bệnh viện A");
        assertThat(r.getFacilityType()).isEqualTo(FacilityType.VACCINATION_CENTER);
        assertThat(r.getProvinceCode()).isEqualTo("01");
        assertThat(r.getProvinceName()).isEqualTo("Hà Nội");
        assertThat(r.getWardCode()).isNull();
        assertThat(r.getWardName()).isNull();
        assertThat(r.isActive()).isTrue();
    }

    @Test
    void getAllFacilities_facilityWithWard_includesWardFields() {
        when(facilityRepository.findAll()).thenReturn(List.of(
                buildFacility(2L, "Trạm y tế", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", "001", "Phường Ba Đình")
        ));

        FacilityResponse r = facilityService.getAllFacilities().get(0);

        assertThat(r.getWardCode()).isEqualTo("001");
        assertThat(r.getWardName()).isEqualTo("Phường Ba Đình");
    }

    // ── getFacilityById ───────────────────────────────────────────────────────

    @Test
    void getFacilityById_returnsCorrectResponse_whenFound() {
        Facility facility = buildFacility(5L, "Phòng khám B", FacilityType.VACCINATION_CENTER, "79", "TP.HCM", null, null);
        facility.setPhone("0901234567");
        when(facilityRepository.findById(5L)).thenReturn(Optional.of(facility));

        FacilityResponse result = facilityService.getFacilityById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Phòng khám B");
        assertThat(result.getFacilityType()).isEqualTo(FacilityType.VACCINATION_CENTER);
        assertThat(result.getProvinceCode()).isEqualTo("79");
        assertThat(result.getPhone()).isEqualTo("0901234567");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getFacilityById_throwsAppException_whenNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getFacilityById(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    // ── createFacility ────────────────────────────────────────────────────────

    @Test
    void createFacility_withoutWard_savesFacilityAndCapacity() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        Facility saved = buildFacility(10L, "Bệnh viện mới", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.save(any(Facility.class))).thenReturn(saved);

        FacilityResponse result = facilityService.createFacility(
                buildRequest("Bệnh viện mới", FacilityType.VACCINATION_CENTER, "01", null, 50, LocalDate.of(2026, 6, 1)));

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getWardCode()).isNull();
        verify(facilityRepository).save(any(Facility.class));
        verify(facilityCapacityRepository).save(any(FacilityCapacity.class));
    }

    @Test
    void createFacility_withMatchingWard_savesFacilityAndCapacity() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        AdministrativeUnit ward = buildWard("001", "Phường Ba Đình", province);
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(ward));
        Facility saved = buildFacility(11L, "Trạm y tế", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", "001", "Phường Ba Đình");
        when(facilityRepository.save(any(Facility.class))).thenReturn(saved);

        FacilityResponse result = facilityService.createFacility(
                buildRequest("Trạm y tế", FacilityType.VACCINATION_CENTER, "01", "001", 20, LocalDate.of(2026, 6, 1)));

        assertThat(result.getWardCode()).isEqualTo("001");
        assertThat(result.getWardName()).isEqualTo("Phường Ba Đình");
        verify(facilityCapacityRepository).save(any(FacilityCapacity.class));
    }

    @Test
    void createFacility_capacityHasCorrectValuesFromRequest() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        Facility saved = buildFacility(12L, "Bệnh viện X", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.save(any(Facility.class))).thenReturn(saved);

        facilityService.createFacility(
                buildRequest("Bệnh viện X", FacilityType.VACCINATION_CENTER, "01", null, 100, LocalDate.of(2026, 7, 1)));

        ArgumentCaptor<FacilityCapacity> captor = ArgumentCaptor.forClass(FacilityCapacity.class);
        verify(facilityCapacityRepository).save(captor.capture());
        FacilityCapacity capacity = captor.getValue();
        assertThat(capacity.getMaxSlotsPerDay()).isEqualTo(100);
        assertThat(capacity.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(capacity.getFacility()).isEqualTo(saved);
    }

    @Test
    void createFacility_throwsException_whenProvinceNotFound() {
        when(administrativeUnitRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.createFacility(
                buildRequest("X", FacilityType.VACCINATION_CENTER, "99", null, 10, LocalDate.now())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void createFacility_throwsException_whenWardNotFound() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        when(administrativeUnitRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.createFacility(
                buildRequest("X", FacilityType.VACCINATION_CENTER, "01", "999", 10, LocalDate.now())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void createFacility_throwsException_whenWardProvinceMismatch() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        AdministrativeUnit otherProvince = buildProvince("79", "TP.HCM");
        AdministrativeUnit ward = buildWard("001", "Phường Ba Đình", otherProvince);
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> facilityService.createFacility(
                buildRequest("X", FacilityType.VACCINATION_CENTER, "01", "001", 10, LocalDate.now())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH));
    }

    // ── updateFacility ────────────────────────────────────────────────────────

    @Test
    void updateFacility_success_nameAndPhone() {
        Facility existing = buildFacility(20L, "Tên Cũ", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(facilityRepository.save(any(Facility.class))).thenReturn(existing);

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setName("Tên Mới");
        req.setPhone("0900000000");

        FacilityResponse result = facilityService.updateFacility(20L, req);

        assertThat(result.getName()).isEqualTo("Tên Mới");
        assertThat(result.getPhone()).isEqualTo("0900000000");
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void updateFacility_success_withNullFields_doesNotChange() {
        Facility existing = buildFacility(21L, "Bệnh viện B", FacilityType.VACCINATION_CENTER, "79", "TP.HCM", null, null);
        when(facilityRepository.findById(21L)).thenReturn(Optional.of(existing));
        when(facilityRepository.save(any(Facility.class))).thenReturn(existing);

        FacilityResponse result = facilityService.updateFacility(21L, new UpdateFacilityRequest());

        assertThat(result.getName()).isEqualTo("Bệnh viện B");
        assertThat(result.getFacilityType()).isEqualTo(FacilityType.VACCINATION_CENTER);
    }

    @Test
    void updateFacility_throwsException_whenNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.updateFacility(99L, new UpdateFacilityRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Test
    void updateFacility_withProvinceChange_success() {
        Facility existing = buildFacility(22L, "Trạm Y Tế", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        AdministrativeUnit newProvince = buildProvince("79", "TP.HCM");
        when(facilityRepository.findById(22L)).thenReturn(Optional.of(existing));
        when(administrativeUnitRepository.findById("79")).thenReturn(Optional.of(newProvince));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setProvinceCode("79");

        FacilityResponse result = facilityService.updateFacility(22L, req);

        assertThat(result.getProvinceCode()).isEqualTo("79");
        assertThat(result.getProvinceName()).isEqualTo("TP.HCM");
    }

    @Test
    void updateFacility_withProvinceNotFound_throwsException() {
        Facility existing = buildFacility(23L, "Phòng khám", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(23L)).thenReturn(Optional.of(existing));
        when(administrativeUnitRepository.findById("99")).thenReturn(Optional.empty());

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setProvinceCode("99");

        assertThatThrownBy(() -> facilityService.updateFacility(23L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void updateFacility_withWardChange_success() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        Facility existing = buildFacility(24L, "Bệnh viện C", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        AdministrativeUnit ward = buildWard("001", "Phường Ba Đình", province);
        when(facilityRepository.findById(24L)).thenReturn(Optional.of(existing));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(ward));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setWardCode("001");

        FacilityResponse result = facilityService.updateFacility(24L, req);

        assertThat(result.getWardCode()).isEqualTo("001");
        assertThat(result.getWardName()).isEqualTo("Phường Ba Đình");
    }

    @Test
    void updateFacility_withWardNotFound_throwsException() {
        Facility existing = buildFacility(25L, "Bệnh viện D", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(25L)).thenReturn(Optional.of(existing));
        when(administrativeUnitRepository.findById("999")).thenReturn(Optional.empty());

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setWardCode("999");

        assertThatThrownBy(() -> facilityService.updateFacility(25L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void updateFacility_withWardProvinceMismatch_throwsException() {
        AdministrativeUnit otherProvince = buildProvince("79", "TP.HCM");
        AdministrativeUnit mismatchedWard = buildWard("001", "Phường Ba Đình", otherProvince);
        Facility existing = buildFacility(26L, "Bệnh viện E", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(26L)).thenReturn(Optional.of(existing));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(mismatchedWard));

        UpdateFacilityRequest req = new UpdateFacilityRequest();
        req.setWardCode("001");

        assertThatThrownBy(() -> facilityService.updateFacility(26L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH));
    }

    // ── deactivateFacility ────────────────────────────────────────────────────

    @Test
    void deactivateFacility_success() {
        Facility active = buildFacility(30L, "Cơ sở Active", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(30L)).thenReturn(Optional.of(active));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> {
            Facility f = inv.getArgument(0);
            f.setActive(false);
            return f;
        });

        FacilityResponse result = facilityService.deactivateFacility(30L);

        assertThat(result.isActive()).isFalse();
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void deactivateFacility_throwsException_whenNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.deactivateFacility(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Test
    void deactivateFacility_throwsException_whenAlreadyInactive() {
        Facility inactive = buildFacility(31L, "Cơ sở Inactive", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        inactive.setActive(false);
        when(facilityRepository.findById(31L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> facilityService.deactivateFacility(31L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_ALREADY_INACTIVE));
    }

    // ── createCapacity ────────────────────────────────────────────────────────

    @Test
    void createCapacity_success() {
        Facility facility = buildFacility(40L, "Bệnh viện F", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(40L)).thenReturn(Optional.of(facility));

        FacilityCapacity saved = FacilityCapacity.builder()
                .id(1L).facility(facility).maxSlotsPerDay(80).effectiveFrom(LocalDate.of(2026, 7, 1)).build();
        when(facilityCapacityRepository.save(any(FacilityCapacity.class))).thenReturn(saved);

        FacilityCapacityRequest req = new FacilityCapacityRequest();
        req.setMaxSlotsPerDay(80);
        req.setEffectiveFrom(LocalDate.of(2026, 7, 1));

        FacilityCapacityResponse result = facilityService.createCapacity(40L, req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFacilityId()).isEqualTo(40L);
        assertThat(result.getMaxSlotsPerDay()).isEqualTo(80);
        assertThat(result.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        verify(facilityCapacityRepository).save(any(FacilityCapacity.class));
    }

    @Test
    void createCapacity_throwsException_whenFacilityNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        FacilityCapacityRequest req = new FacilityCapacityRequest();
        req.setMaxSlotsPerDay(50);
        req.setEffectiveFrom(LocalDate.now());

        assertThatThrownBy(() -> facilityService.createCapacity(99L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    // ── getCapacity ───────────────────────────────────────────────────────────

    @Test
    void getCapacity_success() {
        Facility facility = buildFacility(50L, "Bệnh viện G", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        FacilityCapacity capacity = FacilityCapacity.builder()
                .id(5L).facility(facility).maxSlotsPerDay(60).effectiveFrom(LocalDate.of(2026, 6, 1)).build();
        when(facilityRepository.findById(50L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(50L), any(LocalDate.class)))
                .thenReturn(Optional.of(capacity));

        FacilityCapacityResponse result = facilityService.getCapacity(50L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getFacilityId()).isEqualTo(50L);
        assertThat(result.getMaxSlotsPerDay()).isEqualTo(60);
    }

    @Test
    void getCapacity_throwsException_whenFacilityNotFound() {
        when(facilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getCapacity(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_NOT_FOUND));
    }

    @Test
    void getCapacity_throwsException_whenCapacityNotFound() {
        Facility facility = buildFacility(51L, "Bệnh viện H", FacilityType.VACCINATION_CENTER, "01", "Hà Nội", null, null);
        when(facilityRepository.findById(51L)).thenReturn(Optional.of(facility));
        when(facilityCapacityRepository.findActiveCapacity(eq(51L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getCapacity(51L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_CAPACITY_NOT_FOUND));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AdministrativeUnit buildProvince(String code, String name) {
        return AdministrativeUnit.builder()
                .code(code).name(name).type(AdministrativeUnitType.PROVINCE).build();
    }

    private AdministrativeUnit buildWard(String code, String name, AdministrativeUnit parent) {
        return AdministrativeUnit.builder()
                .code(code).name(name).type(AdministrativeUnitType.WARD).parent(parent).build();
    }

    private Facility buildFacility(Long id, String name, FacilityType type,
                                    String provinceCode, String provinceName,
                                    String wardCode, String wardName) {
        AdministrativeUnit province = buildProvince(provinceCode, provinceName);
        AdministrativeUnit ward = wardCode != null
                ? buildWard(wardCode, wardName, province)
                : null;
        return Facility.builder()
                .id(id).name(name).facilityType(type)
                .address("123 Đường ABC")
                .province(province).ward(ward)
                .active(true).build();
    }

    private CreateFacilityRequest buildRequest(String name, FacilityType type,
                                                String provinceCode, String wardCode,
                                                int maxSlots, LocalDate effectiveFrom) {
        CreateFacilityRequest req = new CreateFacilityRequest();
        req.setName(name);
        req.setFacilityType(type);
        req.setAddress("123 Đường ABC");
        req.setProvinceCode(provinceCode);
        req.setWardCode(wardCode);
        req.setMaxSlotsPerDay(maxSlots);
        req.setCapacityEffectiveFrom(effectiveFrom);
        return req;
    }
}
