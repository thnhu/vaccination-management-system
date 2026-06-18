package Vaccination.Management.System.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Success
    SUCCESS(1000, "Success", HttpStatus.OK),

    // Auth
    USER_EXISTED(1001, "Phone number already in use", HttpStatus.CONFLICT),
    EMAIL_EXISTED(1007, "Email already in use", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(1003, "Invalid phone or password", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(1005, "Phone must be 10-15 digits", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(1006, "Access denied", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1008, "Unauthorized", HttpStatus.UNAUTHORIZED),

    // Vaccine
    VACCINE_NOT_FOUND(2001, "Vaccine not found", HttpStatus.NOT_FOUND),
    VACCINE_EXISTED(2002, "Vaccine name already exists", HttpStatus.CONFLICT),
    VACCINE_INACTIVE(2003, "Vaccine is currently inactive", HttpStatus.BAD_REQUEST),
    VACCINE_ALREADY_INACTIVE(2004, "Vaccine is already inactive", HttpStatus.BAD_REQUEST),

    // Administrative Unit
    ADMINISTRATIVE_UNIT_NOT_FOUND(3000, "Administrative unit not found", HttpStatus.NOT_FOUND),

    // Facility
    FACILITY_NOT_FOUND(3001, "Facility not found", HttpStatus.NOT_FOUND),
    FACILITY_INACTIVE(3002, "Facility is currently unavailable", HttpStatus.BAD_REQUEST),
    FACILITY_CAPACITY_NOT_FOUND(3003, "No capacity configuration found for this facility on the selected date", HttpStatus.BAD_REQUEST),
    FACILITY_WARD_PROVINCE_MISMATCH(3004, "Ward does not belong to the specified province", HttpStatus.BAD_REQUEST),

    // Appointment
    APPOINTMENT_NOT_FOUND(4001, "Appointment not found", HttpStatus.NOT_FOUND),
    APPOINTMENT_SLOT_FULL(4002, "No available slots on this date", HttpStatus.BAD_REQUEST),
    APPOINTMENT_DUPLICATE(4003, "Pending appointment already exists for this vaccine", HttpStatus.CONFLICT),
    APPOINTMENT_DOSE_INTERVAL(4004, "Dose interval not met", HttpStatus.BAD_REQUEST),
    APPOINTMENT_INVALID_STATUS(4005, "Action not allowed for appointment in current status", HttpStatus.BAD_REQUEST),
    CITIZEN_ROLE_REQUIRED(4006, "The specified user does not have CITIZEN role", HttpStatus.BAD_REQUEST),
    VACCINATION_SERIES_COMPLETED(4007, "Citizen has already completed the full vaccination series for this vaccine", HttpStatus.BAD_REQUEST),

    // Citizen
    CITIZEN_PROFILE_NOT_FOUND(6001, "Citizen profile not found", HttpStatus.NOT_FOUND),

    // Vaccination Record
    RECORD_NOT_FOUND(5001, "Vaccination record not found", HttpStatus.NOT_FOUND),
    BATCH_NOT_FOUND(5002, "Vaccine batch not found", HttpStatus.NOT_FOUND),
    BATCH_UNAVAILABLE(5003, "Batch is not available", HttpStatus.BAD_REQUEST),
    BATCH_DEPLETED(5004, "Batch has no remaining doses", HttpStatus.BAD_REQUEST),
    RECORD_APPOINTMENT_MISMATCH(5005, "Record fields do not match the linked appointment", HttpStatus.BAD_REQUEST),
    RECORD_ALREADY_VERIFIED(5006, "Vaccination record has already been verified", HttpStatus.BAD_REQUEST),

    // Fallback
    UNCATEGORIZED(9999, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}