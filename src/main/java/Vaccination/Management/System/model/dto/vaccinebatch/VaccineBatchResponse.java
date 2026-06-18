package Vaccination.Management.System.model.dto.vaccinebatch;

import Vaccination.Management.System.model.enums.BatchStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class VaccineBatchResponse {
    private Long id;
    private Long vaccineId;
    private String vaccineName;
    private Long facilityId;
    private String facilityName;
    private String batchNumber;
    private Integer quantity;
    private Integer remaining;
    private BatchStatus status;
    private LocalDate manufacturedDate;
    private LocalDate expiryDate;
    private LocalDate receivedDate;
    private String manufacturerName;
    private String supplierName;
    private BigDecimal price;
    private LocalDateTime importedAt;
    private LocalDateTime recalledAt;
}
