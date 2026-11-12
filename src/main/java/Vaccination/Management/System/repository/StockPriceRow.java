package Vaccination.Management.System.repository;

import java.math.BigDecimal;

/** Aggregate over usable batches, grouped by vaccine or by facility (see VaccineBatchRepository). */
public interface StockPriceRow {
    /** The grouping key: a vaccine id or a facility id, depending on the query. */
    Long getKeyId();
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
    Long getFacilityCount();
}
