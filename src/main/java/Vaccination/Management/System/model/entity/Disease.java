package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @Column(nullable = false, unique = true, length = 200)
    private String name;
}