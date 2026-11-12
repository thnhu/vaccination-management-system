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
    private Integer totalDoses;
    private List<DoseScheduleInfo> doseSchedules;
    private Set<DiseaseInfo> diseases;

    // --- Public catalogue content (null when not set) ---
    private Long manufacturerId;
    private String manufacturerName;
    private String countryOfOrigin;
    private String description;
    private Integer minAgeMonths;
    private Integer maxAgeMonths;
    private String route;
    private String contraindications;
    private String precautions;
    private String sideEffects;
    private String storage;

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
