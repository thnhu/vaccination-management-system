package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.catalog.FacilityVaccineResponse;
import Vaccination.Management.System.model.dto.catalog.VaccineAvailabilityResponse;

import java.util.List;

/** Read-only, public answers to "where can I get vaccine X?" and "what does facility Y have?". */
public interface PublicCatalogService {
    List<VaccineAvailabilityResponse> getVaccineAvailability(Long vaccineId);
    List<FacilityVaccineResponse> getFacilityVaccines(Long facilityId);
}
