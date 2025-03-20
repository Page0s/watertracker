package com.gograffer.watertracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.AppUser;
import com.gograffer.watertracker.repository.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    public List<Location> findByUser(AppUser user) {
        return locationRepository.findByUser(user);
    }

    public List<Location> findByUserId(Long userId) {
        return locationRepository.findByUserId(userId);
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public void deleteById(Long id) {
        locationRepository.deleteById(id);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }
}
