package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "facility_capacity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "max_slots_per_day", nullable = false)
    private Integer maxSlotsPerDay;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Version
    @Column(name = "version")
    private Integer version;
}
