package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.MedicalStaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalStaffProfileRepository extends JpaRepository<MedicalStaffProfile, Long> {

    Optional<MedicalStaffProfile> findByUserId(Long userId);
}
