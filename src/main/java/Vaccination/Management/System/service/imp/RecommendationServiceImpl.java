package Vaccination.Management.System.service.imp;

import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;
import Vaccination.Management.System.model.entity.VaccinationRecord;
import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import Vaccination.Management.System.model.enums.RecordStatus;
import Vaccination.Management.System.repository.VaccinationRecordRepository;
import Vaccination.Management.System.repository.VaccineDoseScheduleRepository;
import Vaccination.Management.System.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccineDoseScheduleRepository vaccineDoseScheduleRepository;

    @Override
    public List<RecommendationResponse> getRecommendations(Long citizenId) {
        List<VaccinationRecord> validRecords = vaccinationRecordRepository
                .findByCitizenId(citizenId)
                .stream()
                .filter(r -> r.getStatus() == RecordStatus.VALID)
                .toList();

        if (validRecords.isEmpty()) return List.of();

        // Keep only the highest dose record per vaccine
        Map<Long, VaccinationRecord> latestByVaccine = new LinkedHashMap<>();
        for (VaccinationRecord record : validRecords) {
            Long vaccineId = record.getVaccine().getId();
            VaccinationRecord current = latestByVaccine.get(vaccineId);
            if (current == null
                    || record.getDoseSchedule().getDoseNumber() > current.getDoseSchedule().getDoseNumber()) {
                latestByVaccine.put(vaccineId, record);
            }
        }

        List<RecommendationResponse> result = new ArrayList<>();
        for (VaccinationRecord last : latestByVaccine.values()) {
            int currentDose = last.getDoseSchedule().getDoseNumber();
            Long vaccineId = last.getVaccine().getId();

            Optional<VaccineDoseSchedule> nextSchedule = vaccineDoseScheduleRepository
                    .findByVaccineIdAndDoseNumber(vaccineId, currentDose + 1);

            if (nextSchedule.isEmpty()) {
                result.add(RecommendationResponse.builder()
                        .vaccineId(vaccineId)
                        .vaccineName(last.getVaccine().getName())
                        .currentDoseNumber(currentDose)
                        .nextDoseNumber(null)
                        .earliestDate(null)
                        .status("SERIES_COMPLETED")
                        .build());
                continue;
            }

            Integer daysAfterPrevious = nextSchedule.get().getDaysAfterPrevious();
            LocalDate administeredDate = last.getAdministeredAt().toLocalDate();
            LocalDate earliestDate = (daysAfterPrevious != null)
                    ? administeredDate.plusDays(daysAfterPrevious)
                    : LocalDate.now();

            String status = LocalDate.now().isBefore(earliestDate) ? "INTERVAL_NOT_MET" : "PENDING";

            result.add(RecommendationResponse.builder()
                    .vaccineId(vaccineId)
                    .vaccineName(last.getVaccine().getName())
                    .currentDoseNumber(currentDose)
                    .nextDoseNumber(currentDose + 1)
                    .earliestDate(earliestDate)
                    .status(status)
                    .build());
        }

        return result;
    }
}
