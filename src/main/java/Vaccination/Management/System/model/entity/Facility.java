package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.FacilityType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "facility")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false, length = 20)
    private FacilityType facilityType;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "province_code", nullable = false, length = 10)
    private String provinceCode;

    @Column(name = "district_code", length = 10)
    private String districtCode;

    @Column(length = 15)
    private String phone;

    @Builder.Default
    @Column(name = "max_slots_per_day", nullable = false)
    private Integer maxSlotsPerDay = 50;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Version
    @Column(name = "version")
    private Integer version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
