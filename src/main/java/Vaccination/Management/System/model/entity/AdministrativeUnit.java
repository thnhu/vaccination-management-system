package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String type; // PROVINCE | WARD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code")
    private AdministrativeUnit parent;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;
}