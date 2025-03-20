package com.gograffer.watertracker.controller;

import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.service.MeasurePointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MeasurePointApiController {

    @Autowired
    private MeasurePointService measurePointService;

    /**
     * REST API endpoint to get measure points for a location
     */
    @GetMapping("/locations/{locationId}/measurepoints")
    public ResponseEntity<List<MeasurePoint>> getMeasurePointsForLocation(
            @PathVariable("locationId") Long locationId,
            HttpSession session) {

        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        // Get measure points for location
        List<MeasurePoint> measurePoints = measurePointService.findByLocationId(locationId);
        return ResponseEntity.ok(measurePoints);
    }
}
