package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_staff_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalStaffProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_code", nullable = false, unique = true, length = 20)
    private String staffCode;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", referencedColumnName = "id", nullable = false)
    private Facility facility;
}
