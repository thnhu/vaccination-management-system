package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.citizen.CitizenProfileResponse;
import Vaccination.Management.System.model.dto.citizen.UpdateCitizenProfileRequest;
import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.entity.CitizenProfile;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.model.enums.AdministrativeUnitType;
import Vaccination.Management.System.model.enums.Gender;
import Vaccination.Management.System.model.enums.UserRole;
import Vaccination.Management.System.repository.AdministrativeUnitRepository;
import Vaccination.Management.System.repository.CitizenProfileRepository;
import Vaccination.Management.System.service.imp.CitizenServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenServiceTest {

    @Mock private CitizenProfileRepository citizenProfileRepository;
    @Mock private AdministrativeUnitRepository administrativeUnitRepository;

    @InjectMocks private CitizenServiceImp citizenService;

    // ── getMyProfile ──────────────────────────────────────────────────────────

    @Test
    void getMyProfile_success() {
        CitizenProfile profile = buildProfile(1L, Gender.MALE, "123 Đường ABC", "01", "Hà Nội", "001", "Phường Ba Đình");
        when(citizenProfileRepository.findByUser_Id(1L)).thenReturn(Optional.of(profile));

        CitizenProfileResponse response = citizenService.getMyProfile(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(response.getPhone()).isEqualTo("0901234567");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getIdCardNumber()).isEqualTo("001234567890");
        assertThat(response.getGender()).isEqualTo(Gender.MALE);
        assertThat(response.getAddress()).isEqualTo("123 Đường ABC");
        assertThat(response.getProvinceCode()).isEqualTo("01");
        assertThat(response.getProvinceName()).isEqualTo("Hà Nội");
        assertThat(response.getWardCode()).isEqualTo("001");
        assertThat(response.getWardName()).isEqualTo("Phường Ba Đình");
    }

    @Test
    void getMyProfile_throwsException_whenProfileNotFound() {
        when(citizenProfileRepository.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.getMyProfile(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CITIZEN_PROFILE_NOT_FOUND));
    }

    // ── updateMyProfile ───────────────────────────────────────────────────────

    @Test
    void updateMyProfile_success_genderOnly() {
        CitizenProfile profile = buildProfile(2L, Gender.MALE, "Địa chỉ cũ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(2L)).thenReturn(Optional.of(profile));
        when(citizenProfileRepository.save(any(CitizenProfile.class))).thenReturn(profile);

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setGender(Gender.FEMALE);

        CitizenProfileResponse response = citizenService.updateMyProfile(2L, req);

        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        verify(citizenProfileRepository).save(any(CitizenProfile.class));
    }

    @Test
    void updateMyProfile_success_addressOnly() {
        CitizenProfile profile = buildProfile(3L, Gender.FEMALE, "Địa chỉ cũ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(3L)).thenReturn(Optional.of(profile));
        when(citizenProfileRepository.save(any(CitizenProfile.class))).thenReturn(profile);

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setAddress("Địa chỉ mới");

        CitizenProfileResponse response = citizenService.updateMyProfile(3L, req);

        assertThat(response.getAddress()).isEqualTo("Địa chỉ mới");
    }

    @Test
    void updateMyProfile_success_withProvinceChange_clearsWard() {
        CitizenProfile profile = buildProfile(4L, Gender.MALE, "Địa chỉ", "01", "Hà Nội", "001", "Phường Ba Đình");
        AdministrativeUnit newProvince = buildProvince("79", "TP.HCM");
        when(citizenProfileRepository.findByUser_Id(4L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("79")).thenReturn(Optional.of(newProvince));
        when(citizenProfileRepository.save(any(CitizenProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setProvinceCode("79");

        CitizenProfileResponse response = citizenService.updateMyProfile(4L, req);

        assertThat(response.getProvinceCode()).isEqualTo("79");
        assertThat(response.getProvinceName()).isEqualTo("TP.HCM");
        assertThat(response.getWardCode()).isNull();
        assertThat(response.getWardName()).isNull();
    }

    @Test
    void updateMyProfile_success_withProvinceAndWard() {
        CitizenProfile profile = buildProfile(5L, Gender.FEMALE, "Địa chỉ", "01", "Hà Nội", null, null);
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        AdministrativeUnit ward = buildWard("001", "Phường Ba Đình", province);
        when(citizenProfileRepository.findByUser_Id(5L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("01")).thenReturn(Optional.of(province));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(ward));
        when(citizenProfileRepository.save(any(CitizenProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setProvinceCode("01");
        req.setWardCode("001");

        CitizenProfileResponse response = citizenService.updateMyProfile(5L, req);

        assertThat(response.getProvinceCode()).isEqualTo("01");
        assertThat(response.getWardCode()).isEqualTo("001");
        assertThat(response.getWardName()).isEqualTo("Phường Ba Đình");
    }

    @Test
    void updateMyProfile_success_wardChangeWithExistingProvince() {
        AdministrativeUnit province = buildProvince("01", "Hà Nội");
        CitizenProfile profile = buildProfile(6L, Gender.MALE, "Địa chỉ", "01", "Hà Nội", null, null);
        AdministrativeUnit ward = buildWard("002", "Phường Hoàn Kiếm", province);
        when(citizenProfileRepository.findByUser_Id(6L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("002")).thenReturn(Optional.of(ward));
        when(citizenProfileRepository.save(any(CitizenProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setWardCode("002");

        CitizenProfileResponse response = citizenService.updateMyProfile(6L, req);

        assertThat(response.getWardCode()).isEqualTo("002");
        assertThat(response.getWardName()).isEqualTo("Phường Hoàn Kiếm");
    }

    @Test
    void updateMyProfile_throwsException_whenProfileNotFound() {
        when(citizenProfileRepository.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.updateMyProfile(99L, new UpdateCitizenProfileRequest()))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CITIZEN_PROFILE_NOT_FOUND));
    }

    @Test
    void updateMyProfile_throwsException_whenProvinceNotFound() {
        CitizenProfile profile = buildProfile(7L, Gender.MALE, "Địa chỉ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(7L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("99")).thenReturn(Optional.empty());

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setProvinceCode("99");

        assertThatThrownBy(() -> citizenService.updateMyProfile(7L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void updateMyProfile_throwsException_whenWardNotFound() {
        CitizenProfile profile = buildProfile(8L, Gender.FEMALE, "Địa chỉ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(8L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("999")).thenReturn(Optional.empty());

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setWardCode("999");

        assertThatThrownBy(() -> citizenService.updateMyProfile(8L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
    }

    @Test
    void updateMyProfile_throwsException_whenWardProvinceMismatch() {
        AdministrativeUnit otherProvince = buildProvince("79", "TP.HCM");
        AdministrativeUnit mismatchedWard = buildWard("001", "Phường Ba Đình", otherProvince);
        CitizenProfile profile = buildProfile(9L, Gender.MALE, "Địa chỉ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(9L)).thenReturn(Optional.of(profile));
        when(administrativeUnitRepository.findById("001")).thenReturn(Optional.of(mismatchedWard));

        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        req.setWardCode("001");

        assertThatThrownBy(() -> citizenService.updateMyProfile(9L, req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH));
    }

    // ── toResponse edge cases ─────────────────────────────────────────────────

    @Test
    void getMyProfile_withoutWard_wardFieldsNull() {
        CitizenProfile profile = buildProfile(10L, Gender.OTHER, "Địa chỉ", "01", "Hà Nội", null, null);
        when(citizenProfileRepository.findByUser_Id(10L)).thenReturn(Optional.of(profile));

        CitizenProfileResponse response = citizenService.getMyProfile(10L);

        assertThat(response.getWardCode()).isNull();
        assertThat(response.getWardName()).isNull();
        assertThat(response.getProvinceCode()).isEqualTo("01");
    }

    @Test
    void getMyProfile_withoutProvince_provinceFieldsNull() {
        CitizenProfile profile = buildProfile(11L, Gender.MALE, "Địa chỉ", null, null, null, null);
        when(citizenProfileRepository.findByUser_Id(11L)).thenReturn(Optional.of(profile));

        CitizenProfileResponse response = citizenService.getMyProfile(11L);

        assertThat(response.getProvinceCode()).isNull();
        assertThat(response.getProvinceName()).isNull();
        assertThat(response.getWardCode()).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CitizenProfile buildProfile(Long userId, Gender gender, String address,
                                        String provinceCode, String provinceName,
                                        String wardCode, String wardName) {
        User user = User.builder()
                .id(userId)
                .phone("0901234567")
                .email("test@example.com")
                .fullName("Nguyễn Văn A")
                .role(UserRole.CITIZEN)
                .build();

        AdministrativeUnit province = provinceCode != null ? buildProvince(provinceCode, provinceName) : null;
        AdministrativeUnit ward = wardCode != null ? buildWard(wardCode, wardName, province) : null;

        return CitizenProfile.builder()
                .id(userId)
                .user(user)
                .idCardNumber("001234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(gender)
                .address(address)
                .province(province)
                .ward(ward)
                .build();
    }

    private AdministrativeUnit buildProvince(String code, String name) {
        return AdministrativeUnit.builder()
                .code(code).name(name).type(AdministrativeUnitType.PROVINCE).build();
    }

    private AdministrativeUnit buildWard(String code, String name, AdministrativeUnit parent) {
        return AdministrativeUnit.builder()
                .code(code).name(name).type(AdministrativeUnitType.WARD).parent(parent).build();
    }
}
