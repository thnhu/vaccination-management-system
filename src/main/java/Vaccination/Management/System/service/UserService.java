package Vaccination.Management.System.service;

import Vaccination.Management.System.exception.AppException;
import Vaccination.Management.System.exception.ErrorCode;
import Vaccination.Management.System.model.dto.user.UserMeResponse;
import Vaccination.Management.System.model.entity.Facility;
import Vaccination.Management.System.model.entity.User;
import Vaccination.Management.System.repository.MedicalStaffProfileRepository;
import Vaccination.Management.System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MedicalStaffProfileRepository medicalStaffProfileRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserMeResponse.UserMeResponseBuilder out = UserMeResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt());

        medicalStaffProfileRepository.findByUserId(userId).ifPresent(p -> {
            Facility f = p.getFacility();
            out.staffCode(p.getStaffCode())
                    .specialization(p.getSpecialization())
                    .facilityId(f.getId())
                    .facilityName(f.getName())
                    .facilityAddress(f.getAddress())
                    .facilityPhone(f.getPhone())
                    .facilityProvince(f.getProvince() != null ? f.getProvince().getName() : null);
        });
        return out.build();
    }
}
