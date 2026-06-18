package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccineDoseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaccineDoseScheduleRepository extends JpaRepository<VaccineDoseSchedule, Long> {

    Optional<VaccineDoseSchedule> findByVaccineIdAndDoseNumber(Long vaccineId, Integer doseNumber);
}
