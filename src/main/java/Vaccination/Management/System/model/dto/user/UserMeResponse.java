package Vaccination.Management.System.model.dto.user;

import Vaccination.Management.System.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Account of the signed-in user, any role. Work-place and staff fields are only filled for MEDICAL_STAFF
 * (a FACILITY_ADMIN is not bound to a single facility in the data model).
 */
@Data
@Builder
public class UserMeResponse {
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String staffCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String specialization;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long facilityId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String facilityName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String facilityAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String facilityPhone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String facilityProvince;
}
