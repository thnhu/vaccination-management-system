package Vaccination.Management.System.model.entity;

import Vaccination.Management.System.model.enums.AdministrativeUnitType;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import lombok.*;

@Entity
@Table(name = "administrative_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrativeUnit {

    @Id
    @Column(length = 10)
    private String code;

    @Nationalized
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AdministrativeUnitType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code")
    private AdministrativeUnit parent;
}