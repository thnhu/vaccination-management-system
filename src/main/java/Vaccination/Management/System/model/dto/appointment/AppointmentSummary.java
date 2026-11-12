package Vaccination.Management.System.model.dto.appointment;

import Vaccination.Management.System.model.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import Vaccination.Management.System.model.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class AppointmentSummary {
    private Long id;
    private String vaccineName;
    private String facilityName;
    private LocalDate preferredDate;
    private AppointmentStatus status;

    // Only filled for the staff "today" list (identity check before vaccinating); absent elsewhere.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long citizenId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String citizenName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String citizenPhone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate citizenDateOfBirth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Gender citizenGender;
}