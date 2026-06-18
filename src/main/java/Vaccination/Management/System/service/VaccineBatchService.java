package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.vaccinebatch.CreateVaccineBatchRequest;
import Vaccination.Management.System.model.dto.vaccinebatch.VaccineBatchResponse;

import java.util.List;

public interface VaccineBatchService {
    VaccineBatchResponse createBatch(Long facilityId, CreateVaccineBatchRequest request, Long staffId);
    List<VaccineBatchResponse> getBatchesByFacility(Long facilityId);
    VaccineBatchResponse recallBatch(Long batchId, Long staffId);
}
