package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.citizen.CitizenProfileResponse;
import Vaccination.Management.System.model.dto.citizen.UpdateCitizenProfileRequest;
import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.entity.CitizenProfile;
import Vaccination.Management.System.repository.AdministrativeUnitRepository;
import Vaccination.Management.System.repository.CitizenProfileRepository;
import Vaccination.Management.System.service.CitizenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CitizenServiceImp implements CitizenService {

    private final CitizenProfileRepository citizenProfileRepository;
    private final AdministrativeUnitRepository administrativeUnitRepository;

    @Override
    @Transactional(readOnly = true)
    public CitizenProfileResponse getMyProfile(Long userId) {
        CitizenProfile profile = citizenProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    @Override
    @Transactional
    public CitizenProfileResponse updateMyProfile(Long userId, UpdateCitizenProfileRequest request) {
        CitizenProfile profile = citizenProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CITIZEN_PROFILE_NOT_FOUND));

        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }

        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        AdministrativeUnit effectiveProvince = profile.getProvince();

        if (request.getProvinceCode() != null) {
            effectiveProvince = administrativeUnitRepository.findById(request.getProvinceCode())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
            profile.setProvince(effectiveProvince);
            if (request.getWardCode() == null) {
                profile.setWard(null);
            }
        }

        if (request.getWardCode() != null) {
            AdministrativeUnit ward = administrativeUnitRepository.findById(request.getWardCode())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMINISTRATIVE_UNIT_NOT_FOUND));
            if (ward.getParent() == null || !ward.getParent().getCode().equals(effectiveProvince.getCode())) {
                throw new AppException(ErrorCode.FACILITY_WARD_PROVINCE_MISMATCH);
            }
            profile.setWard(ward);
        }

        profile = citizenProfileRepository.save(profile);
        return toResponse(profile);
    }

    private CitizenProfileResponse toResponse(CitizenProfile p) {
        return CitizenProfileResponse.builder()
                .userId(p.getUser().getId())
                .phone(p.getUser().getPhone())
                .email(p.getUser().getEmail())
                .fullName(p.getUser().getFullName())
                .idCardNumber(p.getIdCardNumber())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .address(p.getAddress())
                .provinceCode(p.getProvince() != null ? p.getProvince().getCode() : null)
                .provinceName(p.getProvince() != null ? p.getProvince().getName() : null)
                .wardCode(p.getWard() != null ? p.getWard().getCode() : null)
                .wardName(p.getWard() != null ? p.getWard().getName() : null)
                .build();
    }
}
