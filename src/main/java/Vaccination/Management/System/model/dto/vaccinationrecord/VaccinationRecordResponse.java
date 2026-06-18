package Vaccination.Management.System.model.dto.vaccinationrecord;

import Vaccination.Management.System.model.enums.RecordStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private RecordStatus status;
    private String correctionReason;
    private Long replacesRecordId;
    private LocalDateTime createdAt;
    private List<VaccinationReactionResponse> reactions;
}
