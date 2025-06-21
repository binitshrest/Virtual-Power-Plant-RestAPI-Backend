package org.binitshrestha.vpp.repository;

import org.binitshrestha.vpp.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Long> {

    // Find battery by name (for uniqueness check)
    Optional<Battery> findByName(String name);

    // Find batteries within postcode range
    @Query("SELECT b FROM Battery b WHERE b.postcode BETWEEN :startPostcode AND :endPostcode ORDER BY b.name ASC")
    List<Battery> findBatteriesInPostcodeRange(@Param("startPostcode") String startPostcode,
                                               @Param("endPostcode") String endPostcode);

    // Find batteries within postcode range with watt capacity filters
    @Query("SELECT b FROM Battery b WHERE b.postcode BETWEEN :startPostcode AND :endPostcode " +
            "AND (:minWattCapacity IS NULL OR b.wattCapacity >= :minWattCapacity) " +
            "AND (:maxWattCapacity IS NULL OR b.wattCapacity <= :maxWattCapacity) " +
            "ORDER BY b.name ASC")
    List<Battery> findBatteriesInPostcodeRangeWithCapacityFilter(
            @Param("startPostcode") String startPostcode,
            @Param("endPostcode") String endPostcode,
            @Param("minWattCapacity") Double minWattCapacity,
            @Param("maxWattCapacity") Double maxWattCapacity);

    // Find batteries by postcode
    List<Battery> findByPostcodeOrderByNameAsc(String postcode);

    // Find batteries with watt capacity greater than specified value
    List<Battery> findByWattCapacityGreaterThanOrderByWattCapacityDesc(Double wattCapacity);

    // Find batteries by status
    List<Battery> findByStatus(Battery.BatteryStatus status);

    // Custom query for statistics within postcode range
    @Query("SELECT COUNT(b), SUM(b.wattCapacity), AVG(b.wattCapacity), MIN(b.wattCapacity), MAX(b.wattCapacity) " +
            "FROM Battery b WHERE b.postcode BETWEEN :startPostcode AND :endPostcode")
    Object[] getBatteryStatisticsInPostcodeRange(@Param("startPostcode") String startPostcode,
                                                 @Param("endPostcode") String endPostcode);

    // Custom query for statistics within postcode range with capacity filters
    @Query("SELECT COUNT(b), SUM(b.wattCapacity), AVG(b.wattCapacity), MIN(b.wattCapacity), MAX(b.wattCapacity) " +
            "FROM Battery b WHERE b.postcode BETWEEN :startPostcode AND :endPostcode " +
            "AND (:minWattCapacity IS NULL OR b.wattCapacity >= :minWattCapacity) " +
            "AND (:maxWattCapacity IS NULL OR b.wattCapacity <= :maxWattCapacity)")
    Object[] getBatteryStatisticsInPostcodeRangeWithCapacityFilter(
            @Param("startPostcode") String startPostcode,
            @Param("endPostcode") String endPostcode,
            @Param("minWattCapacity") Double minWattCapacity,
            @Param("maxWattCapacity") Double maxWattCapacity);

    // Check if battery name exists (for concurrent registration handling)
    boolean existsByName(String name);
}