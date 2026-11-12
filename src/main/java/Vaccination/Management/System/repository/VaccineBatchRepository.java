package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.VaccineBatch;
import Vaccination.Management.System.model.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccineBatchRepository extends JpaRepository<VaccineBatch, Long> {

    List<VaccineBatch> findByFacilityId(Long facilityId);

    boolean existsByBatchNumberAndFacilityId(String batchNumber, Long facilityId);

    // --- Public catalogue aggregates ---
    // A batch is usable when it is ACTIVE, still has doses and is not past expiry on `today`.
    // The expiry check is deliberate: batch status is not flipped to EXPIRED automatically (C15),
    // so status alone would advertise expired stock. Only active facilities/vaccines count.

    /** Per vaccine: price range and number of facilities with usable stock. */
    @Query("""
            SELECT b.vaccine.id AS keyId, MIN(b.price) AS minPrice, MAX(b.price) AS maxPrice,
                   COUNT(DISTINCT b.facility.id) AS facilityCount
            FROM VaccineBatch b
            WHERE b.status = :status AND b.remaining > 0 AND b.expiryDate >= :today
              AND b.facility.active = true
            GROUP BY b.vaccine.id
            """)
    List<StockPriceRow> summarizeUsableStockByVaccine(@Param("status") BatchStatus status,
                                                      @Param("today") LocalDate today);

    /** For one vaccine: the facilities with usable stock and the price range at each. */
    @Query("""
            SELECT b.facility.id AS keyId, MIN(b.price) AS minPrice, MAX(b.price) AS maxPrice,
                   COUNT(DISTINCT b.facility.id) AS facilityCount
            FROM VaccineBatch b
            WHERE b.vaccine.id = :vaccineId
              AND b.status = :status AND b.remaining > 0 AND b.expiryDate >= :today
              AND b.facility.active = true
            GROUP BY b.facility.id
            """)
    List<StockPriceRow> usableStockByFacilityForVaccine(@Param("vaccineId") Long vaccineId,
                                                        @Param("status") BatchStatus status,
                                                        @Param("today") LocalDate today);

    /** For one facility: the active vaccines with usable stock and the price range of each. */
    @Query("""
            SELECT b.vaccine.id AS keyId, MIN(b.price) AS minPrice, MAX(b.price) AS maxPrice,
                   COUNT(DISTINCT b.facility.id) AS facilityCount
            FROM VaccineBatch b
            WHERE b.facility.id = :facilityId
              AND b.status = :status AND b.remaining > 0 AND b.expiryDate >= :today
              AND b.vaccine.active = true
            GROUP BY b.vaccine.id
            """)
    List<StockPriceRow> usableStockByVaccineForFacility(@Param("facilityId") Long facilityId,
                                                        @Param("status") BatchStatus status,
                                                        @Param("today") LocalDate today);
}
