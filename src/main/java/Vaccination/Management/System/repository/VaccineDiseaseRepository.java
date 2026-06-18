package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccineDisease;
import Vaccination.Management.System.model.entity.VaccineDiseaseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineDiseaseRepository extends JpaRepository<VaccineDisease, VaccineDiseaseId> {
}
