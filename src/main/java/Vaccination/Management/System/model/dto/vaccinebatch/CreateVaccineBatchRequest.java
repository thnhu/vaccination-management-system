package Vaccination.Management.System.model.dto.vaccinebatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateVaccineBatchRequest {

    @NotNull(message = "Vaccine ID is required")
    private Long vaccineId;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private LocalDate manufacturedDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    private Long manufacturerId;
    private Long supplierId;
    private BigDecimal price;
}
