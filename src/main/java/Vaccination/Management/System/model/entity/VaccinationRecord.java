package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.DataSource;
import Vaccination.Management.System.model.enums.ReactionLevel;
import Vaccination.Management.System.model.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaccination_record")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", updatable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false, updatable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false, updatable = false)
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false, updatable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", updatable = false)
    private VaccineBatch batch;

    @Column(name = "dose_number", nullable = false, updatable = false)
    private Integer doseNumber;

    @Column(name = "administered_at", updatable = false)
    private LocalDateTime administeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by", updatable = false)
    private User administeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_level", length = 10)
    @Builder.Default
    private ReactionLevel reactionLevel = ReactionLevel.NONE;

    @Column(name = "reaction_note", length = 500)
    private String reactionNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    @Builder.Default
    private RecordStatus status = RecordStatus.VALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invalidated_by")
    private User invalidatedBy;

    @Column(name = "invalidated_reason", length = 255)
    private String invalidatedReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_by_record_id")
    private VaccinationRecord correctedByRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = true, length = 20, updatable = false)
    @Builder.Default
    private DataSource dataSource = DataSource.SYSTEM;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
