package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vaccine_dose_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vaccine_id", "dose_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineDoseSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    // NULL for dose 1 — no previous dose exists
    @Column(name = "days_after_previous")
    private Integer daysAfterPrevious;
}
