package com.gograffer.watertracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.repository.MeasurePointRepository;

@Service
public class MeasurePointService {

    private final MeasurePointRepository measurePointRepository;

    @Autowired
    public MeasurePointService(MeasurePointRepository measurePointRepository) {
        this.measurePointRepository = measurePointRepository;
    }

    public List<MeasurePoint> findByLocation(Location location) {
        return measurePointRepository.findByLocation(location).stream()
                .filter(MeasurePoint::isActive)
                .toList();
    }

    public List<MeasurePoint> findByLocationId(Long locationId) {
        return measurePointRepository.findByLocationId(locationId).stream()
                .filter(MeasurePoint::isActive)
                .toList();
    }

    public Optional<MeasurePoint> findById(Long id) {
        return measurePointRepository.findById(id);
    }

    public MeasurePoint saveMeasurePoint(MeasurePoint measurePoint) {
        return measurePointRepository.save(measurePoint);
    }

    public void deactivateMeasurePoint(Long id) {
        measurePointRepository.findById(id).ifPresent(measurePoint -> {
            measurePoint.setActive(false);
            measurePointRepository.save(measurePoint);
        });
    }
}
