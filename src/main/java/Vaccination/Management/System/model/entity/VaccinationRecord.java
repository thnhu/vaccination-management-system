package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.DataSource;
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

    @Column(name = "dose_number", nullable = false, updatable = false)
    private Integer doseNumber;

    @Column(name = "administered_at", updatable = false)
    private LocalDateTime administeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by", updatable = false)
    private User administeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    @Builder.Default
    private RecordStatus status = RecordStatus.VALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invalidated_by")
    private User invalidatedBy;

    @Nationalized
    @Column(name = "invalidated_reason", length = 255)
    private String invalidatedReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaces_record_id", updatable = false)
    private VaccinationRecord replacesRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = true, length = 20, updatable = false)
    @Builder.Default
    private DataSource dataSource = DataSource.SYSTEM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
