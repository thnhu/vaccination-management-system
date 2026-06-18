package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.appointment.AppointmentResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentStatusHistoryResponse;
import Vaccination.Management.System.model.dto.appointment.AppointmentSummary;
import Vaccination.Management.System.model.dto.appointment.CreateAppointmentRequest;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(CreateAppointmentRequest request, Long citizenId);
    List<AppointmentSummary> getMyAppointments(Long citizenId);
    List<AppointmentSummary> getTodayAppointments(Long staffId);
    List<AppointmentStatusHistoryResponse> getAppointmentHistory(Long appointmentId);
    AppointmentResponse cancelAppointment(Long appointmentId, Long userId, String reason);
    AppointmentResponse markNoShow(Long appointmentId, Long staffId);
}
