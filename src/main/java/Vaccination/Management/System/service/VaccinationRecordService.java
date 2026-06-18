package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.vaccinationrecord.RecordVaccinationRequest;
import Vaccination.Management.System.model.dto.vaccinationrecord.VaccinationRecordResponse;

import java.util.List;

public interface VaccinationRecordService {
    VaccinationRecordResponse recordVaccination(RecordVaccinationRequest request, Long staffId);
    List<VaccinationRecordResponse> getVaccinationRecords(Long citizenId, Long requesterId, String requesterRole);
    VaccinationRecordResponse invalidateRecord(Long recordId, Long staffId, String reason);
}
