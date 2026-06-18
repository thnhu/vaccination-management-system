package Vaccination.Management.System.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VaccineDiseaseId implements Serializable {

    @Column(name = "vaccine_id")
    private Long vaccineId;

    @Column(name = "disease_id")
    private Long diseaseId;
}
