package Vaccination.Management.System.model.dto.vaccinationrecord;

import Vaccination.Management.System.model.enums.DataSource;
import Vaccination.Management.System.model.enums.RecordStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VaccinationRecordResponse {
    private Long id;
    private Long citizenId;
    private String citizenName;
    private Long vaccineId;
    private String vaccineName;
    private Long facilityId;
    private String facilityName;
    private String batchNumber;
    private Integer doseNumber;
    private LocalDateTime administeredAt;
    private String administeredByName;
    private RecordStatus status;
    private Long replacesRecordId;
    private DataSource dataSource;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
