package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineBatchRepository extends JpaRepository<VaccineBatch, Long> {

    List<VaccineBatch> findByFacilityId(Long facilityId);

    boolean existsByBatchNumberAndFacilityId(String batchNumber, Long facilityId);
}