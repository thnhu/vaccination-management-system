package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.ReactionLevel;
import Vaccination.Management.System.model.enums.ReportSource;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaccination_reaction")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private VaccinationRecord record;

    @Nationalized
    @Column(name = "symptom", nullable = false, length = 100)
    private String symptom;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private ReactionLevel severity;

    @Column(name = "onset_at", nullable = false)
    private LocalDateTime onsetAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreatedBy
    @Column(name = "reported_by", nullable = false, updatable = false)
    private Long reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_source", nullable = false, updatable = false, length = 30)
    private ReportSource reportSource;

    @CreatedDate
    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;
}
