package com.gograffer.watertracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;

@Repository
public interface MeasurePointRepository extends JpaRepository<MeasurePoint, Long> {
    List<MeasurePoint> findByLocation(Location location);
    List<MeasurePoint> findByLocationId(@Param("locationId") Long locationId);
}
