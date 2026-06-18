package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineBatchRepository extends JpaRepository<VaccineBatch, Long> {
}