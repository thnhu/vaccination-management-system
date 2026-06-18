package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.AppointmentStatusHistory;
import Vaccination.Management.System.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {

    List<AppointmentStatusHistory> findByAppointmentIdOrderByChangedAtAsc(Long appointmentId);

    Optional<AppointmentStatusHistory> findFirstByAppointmentIdAndToStatus(Long appointmentId, AppointmentStatus toStatus);
}
