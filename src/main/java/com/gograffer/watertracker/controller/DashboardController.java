package com.gograffer.watertracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gograffer.watertracker.model.AppUser;
import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.model.WaterConsumption;
import com.gograffer.watertracker.service.LocationService;
import com.gograffer.watertracker.service.MeasurePointService;
import com.gograffer.watertracker.service.UserService;
import com.gograffer.watertracker.service.WaterConsumptionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final LocationService locationService;
    private final WaterConsumptionService waterConsumptionService;
    private final MeasurePointService measurePointService;

    @Autowired
    public DashboardController(UserService userService, 
                              LocationService locationService,
                              WaterConsumptionService waterConsumptionService,
                              MeasurePointService measurePointService) {
        this.userService = userService;
        this.locationService = locationService;
        this.waterConsumptionService = waterConsumptionService;
        this.measurePointService = measurePointService;
    }

    @GetMapping
    public String dashboard(
            @RequestParam(name = "locationId", required = false) Long locationId,
            @RequestParam(name = "measurePointId", required = false) Long measurePointId,
            @RequestParam(name = "sortDesc", required = false, defaultValue = "true") Boolean sortDesc,
            @RequestParam(name = "showAddConsumptionModal", required = false) Boolean showAddConsumptionModal,
            @RequestParam(name = "showManageMeasurePointsModal", required = false) Boolean showManageMeasurePointsModal,
            HttpSession session, 
            Model model) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get user data
        AppUser user = userService.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        
        // Get user's locations
        List<Location> locations = locationService.findByUser(user);
        model.addAttribute("locations", locations);
        model.addAttribute("hasLocations", !locations.isEmpty());
        
        // Get measure points if a location is selected
        if (locationId != null) {
            List<MeasurePoint> measurePoints = measurePointService.findByLocationId(locationId);
            model.addAttribute("measurePoints", measurePoints);
            model.addAttribute("hasMeasurePoints", !measurePoints.isEmpty());
            
            // Add the selected location to the model
            model.addAttribute("selectedLocationId", locationId);
            
            // Find the selected location name for display
            Location selectedLocation = locations.stream()
                    .filter(loc -> loc.getId().equals(locationId))
                    .findFirst()
                    .orElse(null);
            
            if (selectedLocation != null) {
                model.addAttribute("selectedLocationName", selectedLocation.getName());
                // Add location for measure points
                model.addAttribute("location", selectedLocation);
            }
            
            // If a measure point is selected, add it to the model
            if (measurePointId != null) {
                model.addAttribute("selectedMeasurePointId", measurePointId);
                
                // Find the selected measure point name for display
                MeasurePoint selectedMeasurePoint = measurePoints.stream()
                        .filter(mp -> mp.getId().equals(measurePointId))
                        .findFirst()
                        .orElse(null);
                
                if (selectedMeasurePoint != null) {
                    model.addAttribute("selectedMeasurePointName", selectedMeasurePoint.getName());
                }
            }
            
            // Show add consumption modal if requested
            if (showAddConsumptionModal != null && showAddConsumptionModal) {
                model.addAttribute("showAddConsumptionModal", true);
            }
            
            // Show manage measure points modal if requested
            if (showManageMeasurePointsModal != null && showManageMeasurePointsModal) {
                model.addAttribute("showManageMeasurePointsModal", true);
            }
        }
        
        // Get water consumption data
        double totalConsumption = 0.0;
        List<WaterConsumption> recentConsumptions = null;
        
        if (!locations.isEmpty()) {
            // Get water consumption records based on filter and sort parameters
            if (measurePointId != null) {
                // Filter by specific measure point and sort
                if (sortDesc) {
                    recentConsumptions = waterConsumptionService.findByMeasurePointIdSorted(measurePointId, true);
                } else {
                    recentConsumptions = waterConsumptionService.findByMeasurePointIdSorted(measurePointId, false);
                }
            } else if (locationId != null) {
                // Filter by specific location and sort
                if (sortDesc) {
                    recentConsumptions = waterConsumptionService.findByLocationIdSorted(locationId, true);
                } else {
                    recentConsumptions = waterConsumptionService.findByLocationIdSorted(locationId, false);
                }
            } else {
                // Get all consumption records for all locations and sort
                if (sortDesc) {
                    recentConsumptions = waterConsumptionService.findByUserLocationsOrderByDateDesc(locations);
                } else {
                    recentConsumptions = waterConsumptionService.findByUserLocationsOrderByDateAsc(locations);
                }
            }
            
            // Calculate total consumption
            if (recentConsumptions != null) {
                for (WaterConsumption consumption : recentConsumptions) {
                    totalConsumption += consumption.getAmount();
                }
            }
        }
        
        model.addAttribute("waterConsumptions", recentConsumptions);
        model.addAttribute("hasConsumptions", recentConsumptions != null && !recentConsumptions.isEmpty());
        model.addAttribute("totalConsumption", totalConsumption);
        model.addAttribute("sortDesc", sortDesc);
        
        // Calculate average consumption (if there are records)
        double averageConsumption = 0.0;
        if (recentConsumptions != null && !recentConsumptions.isEmpty()) {
            averageConsumption = totalConsumption / recentConsumptions.size();
        }
        model.addAttribute("averageConsumption", averageConsumption);
        
        // Add new location and measure point objects for the add forms
        model.addAttribute("newLocation", new Location());
        model.addAttribute("newMeasurePoint", new MeasurePoint());
        
        return "dashboard";
    }

    @GetMapping("/add-consumption")
    public String showAddConsumptionModal(
            @RequestParam(name = "locationId", required = true) Long locationId,
            HttpSession session, 
            Model model) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get user data
        AppUser user = userService.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        
        // Get user's locations
        List<Location> locations = locationService.findByUser(user);
        model.addAttribute("locations", locations);
        model.addAttribute("hasLocations", !locations.isEmpty());
        
        // Get measure points for the selected location
        List<MeasurePoint> measurePoints = measurePointService.findByLocationId(locationId);
        model.addAttribute("measurePoints", measurePoints);
        model.addAttribute("hasMeasurePoints", !measurePoints.isEmpty());
        
        // Add the selected location to the model
        model.addAttribute("selectedLocationId", locationId);
        model.addAttribute("showAddConsumptionModal", true);
        
        return "redirect:/dashboard?locationId=" + locationId + "&showAddConsumptionModal=true";
    }
    
    @GetMapping("/manage-measure-points")
    public String showManageMeasurePointsModal(
            @RequestParam(name = "locationId", required = true) Long locationId,
            HttpSession session, 
            Model model) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get user data
        AppUser user = userService.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        
        // Get location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            return "redirect:/dashboard";
        }
        
        // Get measure points for the selected location
        List<MeasurePoint> measurePoints = measurePointService.findByLocationId(locationId);
        
        // Add attributes to model
        model.addAttribute("location", location);
        model.addAttribute("measurePoints", measurePoints);
        model.addAttribute("selectedLocationId", locationId);
        model.addAttribute("showManageMeasurePointsModal", true);
        
        return "redirect:/dashboard?locationId=" + locationId + "&showManageMeasurePointsModal=true";
    }
}
