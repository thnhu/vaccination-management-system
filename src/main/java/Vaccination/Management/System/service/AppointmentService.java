package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(CreateAppointmentRequest request, Long citizenId);
    List<AppointmentSummary> getMyAppointments(Long citizenId);
    AppointmentResponse confirmAppointment(Long appointmentId, Long staffId);
    AppointmentResponse cancelAppointment(Long appointmentId, Long userId, String reason);
    AppointmentResponse rejectAppointment(Long appointmentId, Long staffId, String reason);
    AppointmentResponse markNoShow(Long appointmentId, Long staffId);
}
