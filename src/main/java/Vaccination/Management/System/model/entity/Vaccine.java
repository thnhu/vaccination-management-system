package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private VaccineCategory category;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

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
