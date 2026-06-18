package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "manufacturer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Nationalized
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 100)
    @Nationalized
    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Size(max = 200)
    @Nationalized
    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
