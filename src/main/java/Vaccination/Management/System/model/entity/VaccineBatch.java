package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vaccine_batch", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"batch_number", "facility_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private Integer quantity;

    // Cached value — authoritative source is COUNT of VALID vaccination_records referencing this batch
    @Column(nullable = false)
    private Integer remaining;

    @Column(name = "manufactured_date")
    private LocalDate manufacturedDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private BatchStatus status = BatchStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "price", precision = 15, scale = 0)
    private BigDecimal price;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "recalled_at", columnDefinition = "DATETIME2")
    private LocalDateTime recalledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recalled_by")
    private User recalledBy;

    @CreatedDate
    @Column(name = "imported_at", updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime importedAt;
}