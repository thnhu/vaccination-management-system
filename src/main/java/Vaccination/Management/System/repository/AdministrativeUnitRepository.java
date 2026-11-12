package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.AdministrativeUnit;
import Vaccination.Management.System.model.enums.AdministrativeUnitType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, String> {
    List<AdministrativeUnit> findByTypeOrderByName(AdministrativeUnitType type);
}
