package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(name = "scientific_name", length = 100)
    private String scientificName;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Builder.Default
    @Column(name = "required_doses", nullable = false)
    private Integer requiredDoses = 1;

    @Column(name = "days_between_doses")
    private Integer daysBetweenDoses;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private VaccineCategory category;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;
}
