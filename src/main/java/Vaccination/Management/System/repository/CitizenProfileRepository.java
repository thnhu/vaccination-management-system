package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.CitizenProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitizenProfileRepository extends JpaRepository<CitizenProfile, Long> {
    Optional<CitizenProfile> findByUser_Id(Long userId);
}
