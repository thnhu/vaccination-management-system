package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.VaccineCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    @Column(name = "scientific_name", length = 100)
    private String scientificName;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Builder.Default
    @Column(name = "required_doses", nullable = false)
    private Integer requiredDoses = 1;

    @Column(name = "days_between_doses")
    private Integer daysBetweenDoses;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private VaccineCategory category;

    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vaccine_disease",
            joinColumns = @JoinColumn(name = "vaccine_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private Set<Disease> diseases = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

}
