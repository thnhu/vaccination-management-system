package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.Appointment;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.facility.id = :facilityId
              AND a.preferredDate = :date
              AND a.status IN ('PENDING', 'CONFIRMED')
            """)
    long countBookedSlots(@Param("facilityId") Long facilityId,
                          @Param("date") LocalDate date);

    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.citizen.id = :citizenId
              AND a.vaccine.id = :vaccineId
              AND a.status IN ('PENDING', 'CONFIRMED')
            """)
    boolean existsPendingOrConfirmed(@Param("citizenId") Long citizenId,
                                     @Param("vaccineId") Long vaccineId);

    List<Appointment> findByCitizenId(Long citizenId);
}