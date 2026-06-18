package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccinationReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationReactionRepository extends JpaRepository<VaccinationReaction, Long> {

    List<VaccinationReaction> findByRecordId(Long recordId);
}
