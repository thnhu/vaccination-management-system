package Vaccination.Management.System.model.dto.vaccine;

import Vaccination.Management.System.model.enums.VaccineCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class VaccineResponse {
    private Long id;
    private String name;
    private VaccineCategory category;
    private boolean active;
    private List<DoseScheduleInfo> doseSchedules;
    private Set<DiseaseInfo> diseases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class DoseScheduleInfo {
        private Integer doseNumber;
        private Integer daysAfterPrevious;
    }

    @Data
    @Builder
    public static class DiseaseInfo {
        private Long id;
        private String name;
    }
}
