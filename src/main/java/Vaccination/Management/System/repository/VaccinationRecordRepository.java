package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccinationRecord;
import Vaccination.Management.System.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {

    List<VaccinationRecord> findByCitizenId(Long citizenId);

    Optional<VaccinationRecord> findFirstByCitizenIdAndVaccineIdAndStatusOrderByAdministeredAtDesc(
            Long citizenId, Long vaccineId, RecordStatus status);
}
