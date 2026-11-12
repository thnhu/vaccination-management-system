package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "vaccine")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private VaccineCategory category;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    // --- Public catalogue content (all optional; shown on the public vaccine page) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @Nationalized
    @Column(length = 1000)
    private String description;

    /** Youngest eligible age in months (inclusive); null = no lower bound stated. */
    @Column(name = "min_age_months")
    private Integer minAgeMonths;

    /** Oldest eligible age in months (inclusive); null = no upper bound stated. */
    @Column(name = "max_age_months")
    private Integer maxAgeMonths;

    /** Route of administration, e.g. "Tiêm bắp". */
    @Nationalized
    @Column(length = 100)
    private String route;

    @Nationalized
    @Column(length = 1000)
    private String contraindications;

    @Nationalized
    @Column(length = 1000)
    private String precautions;

    @Nationalized
    @Column(name = "side_effects", length = 1000)
    private String sideEffects;

    @Nationalized
    @Column(length = 200)
    private String storage;

    /** Co-administration and drug interactions. */
    @Nationalized
    @Column(length = 2000)
    private String interactions;

    /**
     * Human-readable regimen by age group, incl. boosters (e.g. "2–6 months: 2 doses ≥ 2 months apart…").
     * Display only — booking rules still come from vaccine_dose_schedule.
     */
    @Nationalized
    @Column(name = "schedule_notes", length = 2000)
    private String scheduleNotes;

    @Builder.Default
    @OneToMany(mappedBy = "vaccine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("doseNumber ASC")
    private List<VaccineDoseSchedule> doseSchedules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "vaccine", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VaccineDisease> vaccineDiseases = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
}
