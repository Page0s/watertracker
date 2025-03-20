package com.gograffer.watertracker.controller;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.service.LocationService;
import com.gograffer.watertracker.service.MeasurePointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/measure-points")
public class MeasurePointRestController {

    @Autowired
    private MeasurePointService measurePointService;

    @Autowired
    private LocationService locationService;

    @GetMapping("/{locationId}")
    public List<MeasurePoint> getMeasurePointsByLocation(@PathVariable("locationId") Long locationId) {
        return measurePointService.findByLocationId(locationId);
    }
    
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getMeasurePointInfo(@PathVariable("id") Long id) {
        MeasurePoint measurePoint = measurePointService.findById(id).orElse(null);
        if (measurePoint == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Create a simple map with the needed information
        Map<String, Object> info = new HashMap<>();
        info.put("id", measurePoint.getId());
        info.put("name", measurePoint.getName());
        info.put("description", measurePoint.getDescription());
        info.put("locationId", measurePoint.getLocation().getId());
        info.put("locationName", measurePoint.getLocation().getName());
        
        return ResponseEntity.ok(info);
    }

    @PostMapping
    public ResponseEntity<?> addMeasurePoint(@RequestBody Map<String, Object> payload, HttpSession session) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Morate biti prijavljeni da biste dodali mjerno mjesto.");
        }
        
        // Get location ID from payload
        Long locationId = Long.valueOf(payload.get("locationId").toString());
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        
        // Get location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            return ResponseEntity.badRequest().body("Lokacija nije pronađena.");
        }
        
        // Check if location belongs to user
        if (!location.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("Nemate pristup ovoj lokaciji.");
        }
        
        // Create and save measure point
        MeasurePoint measurePoint = new MeasurePoint();
        measurePoint.setName(name);
        measurePoint.setDescription(description);
        measurePoint.setLocation(location);
        
        MeasurePoint savedMeasurePoint = measurePointService.saveMeasurePoint(measurePoint);
        return ResponseEntity.ok(savedMeasurePoint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeasurePoint(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        MeasurePoint existingMeasurePoint = measurePointService.findById(id).orElse(null);
        if (existingMeasurePoint != null) {
            // Dohvati podatke iz payload-a
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");
            
            // Dohvati ID lokacije iz payload-a
            Map<String, Object> locationMap = (Map<String, Object>) payload.get("location");
            Long locationId = Long.valueOf(locationMap.get("id").toString());
            
            // Dohvati lokaciju
            Location location = locationService.findById(locationId).orElse(null);
            if (location == null) {
                return ResponseEntity.badRequest().body("Lokacija nije pronađena.");
            }
            
            // Ažuriraj mjerno mjesto
            existingMeasurePoint.setName(name);
            existingMeasurePoint.setDescription(description);
            existingMeasurePoint.setLocation(location);
            
            MeasurePoint updatedMeasurePoint = measurePointService.saveMeasurePoint(existingMeasurePoint);
            return ResponseEntity.ok(updatedMeasurePoint);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteMeasurePoint(@PathVariable("id") Long id) {
        measurePointService.deactivateMeasurePoint(id);
    }
}
