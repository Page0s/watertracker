package com.gograffer.watertracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.model.WaterConsumption;

@Repository
public interface WaterConsumptionRepository extends JpaRepository<WaterConsumption, Long> {
    // MeasurePoint-based methods
    List<WaterConsumption> findByMeasurePoint(MeasurePoint measurePoint);
    List<WaterConsumption> findByMeasurePointId(Long measurePointId);
    List<WaterConsumption> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT wc FROM WaterConsumption wc WHERE wc.measurePoint.location.user.id = :userId")
    List<WaterConsumption> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT wc FROM WaterConsumption wc WHERE wc.measurePoint.location.id = :locationId AND wc.date BETWEEN :startDate AND :endDate")
    List<WaterConsumption> findByLocationIdAndDateBetween(@Param("locationId") Long locationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // MeasurePoint-based sorting methods
    List<WaterConsumption> findByMeasurePointOrderByDateDesc(MeasurePoint measurePoint);
    List<WaterConsumption> findByMeasurePointOrderByDateAsc(MeasurePoint measurePoint);
    
    List<WaterConsumption> findByMeasurePointIdOrderByDateDesc(Long measurePointId);
    List<WaterConsumption> findByMeasurePointIdOrderByDateAsc(Long measurePointId);
    
    // Methods to find by measure point's location
    @Query("SELECT wc FROM WaterConsumption wc WHERE wc.measurePoint.location.id = :locationId")
    List<WaterConsumption> findByMeasurePointLocationId(@Param("locationId") Long locationId);
    
    @Query("SELECT wc FROM WaterConsumption wc WHERE wc.measurePoint.location.id = :locationId ORDER BY wc.date DESC")
    List<WaterConsumption> findByMeasurePointLocationIdOrderByDateDesc(@Param("locationId") Long locationId);
    
    @Query("SELECT wc FROM WaterConsumption wc WHERE wc.measurePoint.location.id = :locationId ORDER BY wc.date ASC")
    List<WaterConsumption> findByMeasurePointLocationIdOrderByDateAsc(@Param("locationId") Long locationId);
    
    // New method to find previous readings for a measure point
    List<WaterConsumption> findByMeasurePointIdAndDateBeforeOrderByDateDesc(Long measurePointId, LocalDate date);
}
