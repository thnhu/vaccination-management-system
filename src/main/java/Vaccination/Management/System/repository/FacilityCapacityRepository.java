package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.FacilityCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FacilityCapacityRepository extends JpaRepository<FacilityCapacity, Long> {

    @Query("""
            SELECT fc FROM FacilityCapacity fc
            WHERE fc.facility.id = :facilityId
              AND fc.effectiveFrom <= :date
            ORDER BY fc.effectiveFrom DESC
            LIMIT 1
            """)
    Optional<FacilityCapacity> findActiveCapacity(@Param("facilityId") Long facilityId,
                                                   @Param("date") LocalDate date);
}
