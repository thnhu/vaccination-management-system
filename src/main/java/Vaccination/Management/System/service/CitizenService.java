package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.citizen.CitizenProfileResponse;
import Vaccination.Management.System.model.dto.citizen.UpdateCitizenProfileRequest;

public interface CitizenService {
    CitizenProfileResponse getMyProfile(Long userId);
    CitizenProfileResponse updateMyProfile(Long userId, UpdateCitizenProfileRequest request);
}
