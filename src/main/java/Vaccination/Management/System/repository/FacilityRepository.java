package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
}