package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.RecordStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dose_schedule_id", nullable = false, updatable = false)
    private VaccineDoseSchedule doseSchedule;

    @Column(name = "administered_at", updatable = false)
    private LocalDateTime administeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    @Builder.Default
    private RecordStatus status = RecordStatus.VALID;

    @Nationalized
    @Column(name = "correction_reason", length = 255, updatable = false)
    private String correctionReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaces_record_id", updatable = false)
    private VaccinationRecord replacesRecord;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
