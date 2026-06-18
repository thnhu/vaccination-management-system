package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "disease")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Nationalized
    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Size(max = 200)
    @Nationalized
    @Column(name = "scientific_name", length = 200)
    private String scientificName;

    @Size(max = 10)
    @Column(name = "icd10_code", unique = true, length = 10)
    private String icd10Code;

    @Size(max = 500)
    @Nationalized
    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}