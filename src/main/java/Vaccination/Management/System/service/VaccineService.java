package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.vaccine.CreateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.UpdateVaccineRequest;
import Vaccination.Management.System.model.dto.vaccine.VaccineResponse;
import Vaccination.Management.System.model.dto.vaccine.VaccineSummary;

import java.util.List;

public interface VaccineService {
    List<VaccineSummary> getAllVaccines();
    VaccineResponse getVaccineById(Long id);
    VaccineResponse createVaccine(CreateVaccineRequest request);
    VaccineResponse updateVaccine(Long id, UpdateVaccineRequest request);
    VaccineResponse deactivateVaccine(Long id);
}