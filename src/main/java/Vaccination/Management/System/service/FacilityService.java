package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.facility.CreateFacilityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityRequest;
import Vaccination.Management.System.model.dto.facility.FacilityCapacityResponse;
import Vaccination.Management.System.model.dto.facility.FacilityResponse;
import Vaccination.Management.System.model.dto.facility.UpdateFacilityRequest;

import java.util.List;

public interface FacilityService {
    FacilityResponse createFacility(CreateFacilityRequest request);
    FacilityResponse getFacilityById(Long id);
    List<FacilityResponse> getAllFacilities();
    FacilityResponse updateFacility(Long id, UpdateFacilityRequest request);
    FacilityResponse deactivateFacility(Long id);
    FacilityCapacityResponse createCapacity(Long facilityId, FacilityCapacityRequest request);
    FacilityCapacityResponse getCapacity(Long facilityId);
}
