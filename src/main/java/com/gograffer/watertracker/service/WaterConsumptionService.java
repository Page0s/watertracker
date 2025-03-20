package com.gograffer.watertracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.model.WaterConsumption;
import com.gograffer.watertracker.repository.WaterConsumptionRepository;

@Service
public class WaterConsumptionService {

    private final WaterConsumptionRepository waterConsumptionRepository;

    @Autowired
    public WaterConsumptionService(WaterConsumptionRepository waterConsumptionRepository) {
        this.waterConsumptionRepository = waterConsumptionRepository;
    }

    public WaterConsumption saveWaterConsumption(WaterConsumption waterConsumption) {
        // If meter readings are provided, calculate period consumption
        if (waterConsumption.getMeterReading() != null || 
            waterConsumption.getHotWaterMeterReading() != null || 
            waterConsumption.getColdWaterMeterReading() != null) {
            
            // Find the previous reading for this measure point
            WaterConsumption previousReading = findPreviousReading(
                waterConsumption.getMeasurePoint().getId(), 
                waterConsumption.getDate()
            );
            
            // Calculate period consumption based on the previous reading
            waterConsumption.calculatePeriodConsumption(previousReading);
        }
        
        return waterConsumptionRepository.save(waterConsumption);
    }

    /**
     * Finds the most recent water consumption reading for a measure point before the given date
     * 
     * @param measurePointId The ID of the measure point
     * @param currentDate The date of the current reading
     * @return The most recent water consumption reading or null if none exists
     */
    public WaterConsumption findPreviousReading(Long measurePointId, LocalDate currentDate) {
        List<WaterConsumption> previousReadings = waterConsumptionRepository
            .findByMeasurePointIdAndDateBeforeOrderByDateDesc(measurePointId, currentDate);
        
        if (previousReadings != null && !previousReadings.isEmpty()) {
            return previousReadings.get(0); // Return the most recent reading
        }
        
        return null;
    }

    // MeasurePoint-based methods
    public List<WaterConsumption> findByMeasurePoint(MeasurePoint measurePoint) {
        return waterConsumptionRepository.findByMeasurePoint(measurePoint);
    }

    public List<WaterConsumption> findByMeasurePointId(Long measurePointId) {
        return waterConsumptionRepository.findByMeasurePointId(measurePointId);
    }

    // Location-based methods (now using measurePoint.location)
    public List<WaterConsumption> findByLocationId(Long locationId) {
        return waterConsumptionRepository.findByMeasurePointLocationId(locationId);
    }

    public List<WaterConsumption> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return waterConsumptionRepository.findByDateBetween(startDate, endDate);
    }

    public List<WaterConsumption> findByUserId(Long userId) {
        return waterConsumptionRepository.findByUserId(userId);
    }

    public List<WaterConsumption> findByLocationIdAndDateBetween(Long locationId, LocalDate startDate, LocalDate endDate) {
        return waterConsumptionRepository.findByLocationIdAndDateBetween(locationId, startDate, endDate);
    }

    public Optional<WaterConsumption> findById(Long id) {
        return waterConsumptionRepository.findById(id);
    }

    public void deleteById(Long id) {
        waterConsumptionRepository.deleteById(id);
    }

    public List<WaterConsumption> findAll() {
        return waterConsumptionRepository.findAll();
    }

    public List<WaterConsumption> findByUserLocations(List<Location> locations) {
        List<WaterConsumption> allConsumptions = new ArrayList<>();
        
        for (Location location : locations) {
            List<WaterConsumption> consumptions = waterConsumptionRepository.findByMeasurePointLocationId(location.getId());
            allConsumptions.addAll(consumptions);
        }
        
        return allConsumptions;
    }
    
    // Location-based sorting methods
    
    public List<WaterConsumption> findByUserLocationsOrderByDateDesc(List<Location> locations) {
        List<WaterConsumption> allConsumptions = new ArrayList<>();
        
        for (Location location : locations) {
            List<WaterConsumption> consumptions = waterConsumptionRepository.findByMeasurePointLocationIdOrderByDateDesc(location.getId());
            allConsumptions.addAll(consumptions);
        }
        
        // Sort the combined list by date descending
        return allConsumptions.stream()
                .sorted(Comparator.comparing(WaterConsumption::getDate).reversed())
                .collect(Collectors.toList());
    }
    
    public List<WaterConsumption> findByUserLocationsOrderByDateAsc(List<Location> locations) {
        List<WaterConsumption> allConsumptions = new ArrayList<>();
        
        for (Location location : locations) {
            List<WaterConsumption> consumptions = waterConsumptionRepository.findByMeasurePointLocationIdOrderByDateAsc(location.getId());
            allConsumptions.addAll(consumptions);
        }
        
        // Sort the combined list by date ascending
        return allConsumptions.stream()
                .sorted(Comparator.comparing(WaterConsumption::getDate))
                .collect(Collectors.toList());
    }
    
    public List<WaterConsumption> findByLocationIdSorted(Long locationId, boolean descending) {
        if (descending) {
            return waterConsumptionRepository.findByMeasurePointLocationIdOrderByDateDesc(locationId);
        } else {
            return waterConsumptionRepository.findByMeasurePointLocationIdOrderByDateAsc(locationId);
        }
    }
    
    public List<WaterConsumption> findByUserIdSorted(Long userId, boolean descending) {
        // Use the existing userId methods but add custom sorting
        List<WaterConsumption> consumptions = waterConsumptionRepository.findByUserId(userId);
        if (descending) {
            return consumptions.stream()
                .sorted(Comparator.comparing(WaterConsumption::getDate).reversed())
                .collect(Collectors.toList());
        } else {
            return consumptions.stream()
                .sorted(Comparator.comparing(WaterConsumption::getDate))
                .collect(Collectors.toList());
        }
    }
    
    // MeasurePoint-based sorting methods
    public List<WaterConsumption> findByMeasurePointSorted(MeasurePoint measurePoint, boolean descending) {
        if (descending) {
            return waterConsumptionRepository.findByMeasurePointOrderByDateDesc(measurePoint);
        } else {
            return waterConsumptionRepository.findByMeasurePointOrderByDateAsc(measurePoint);
        }
    }
    
    public List<WaterConsumption> findByMeasurePointIdSorted(Long measurePointId, boolean descending) {
        if (descending) {
            return waterConsumptionRepository.findByMeasurePointIdOrderByDateDesc(measurePointId);
        } else {
            return waterConsumptionRepository.findByMeasurePointIdOrderByDateAsc(measurePointId);
        }
    }
}
