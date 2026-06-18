package Vaccination.Management.System.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vaccine_disease")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineDisease {

    @EmbeddedId
    private VaccineDiseaseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vaccineId")
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("diseaseId")
    @JoinColumn(name = "disease_id")
    private Disease disease;
}
