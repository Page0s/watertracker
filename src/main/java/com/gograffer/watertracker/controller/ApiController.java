package com.gograffer.watertracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.service.MeasurePointService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MeasurePointService measurePointService;

    @Autowired
    public ApiController(MeasurePointService measurePointService) {
        this.measurePointService = measurePointService;
    }

    @GetMapping("/measure-points")
    public ResponseEntity<List<MeasurePoint>> getMeasurePointsByLocation(@RequestParam Long locationId, HttpSession session) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        
        // Get measure points for the location
        List<MeasurePoint> measurePoints = measurePointService.findByLocationId(locationId);
        return ResponseEntity.ok(measurePoints);
    }
}
