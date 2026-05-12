package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    List<VaccinationRecord> findByCitizenId(Long citizenId);

    @Query("""
            SELECT CAST(MAX(vr.administeredAt) AS localdate)
            FROM VaccinationRecord vr
            WHERE vr.citizen.id = :citizenId
              AND vr.vaccine.id = :vaccineId
              AND vr.status = 'VALID'
            """)
    Optional<LocalDate> findLatestDoseDate(@Param("citizenId") Long citizenId,
                                           @Param("vaccineId") Long vaccineId);


}