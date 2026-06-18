package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.AdministrativeUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, String> {
}
