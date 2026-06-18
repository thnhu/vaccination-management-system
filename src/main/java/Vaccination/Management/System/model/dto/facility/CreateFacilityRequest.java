package Vaccination.Management.System.model.dto.facility;

import Vaccination.Management.System.model.enums.FacilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFacilityRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    private FacilityType facilityType;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotBlank
    private String provinceCode;

    private String wardCode;

    @Size(max = 15)
    private String phone;

    @NotNull
    @Positive
    private Integer maxSlotsPerDay;

    @NotNull
    private LocalDate capacityEffectiveFrom;
}
