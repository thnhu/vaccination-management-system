package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {
    List<Vaccine> findAllByActiveTrue();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}