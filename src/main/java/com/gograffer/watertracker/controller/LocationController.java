package com.gograffer.watertracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gograffer.watertracker.model.AppUser;
import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.service.LocationService;
import com.gograffer.watertracker.service.MeasurePointService;
import com.gograffer.watertracker.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;
    private final UserService userService;
    private final MeasurePointService measurePointService;

    @Autowired
    public LocationController(LocationService locationService, UserService userService, MeasurePointService measurePointService) {
        this.locationService = locationService;
        this.userService = userService;
        this.measurePointService = measurePointService;
    }

    @PostMapping("/add")
    public String addLocation(@ModelAttribute("location") Location location,
                             @RequestParam("defaultMeasurePointName") String defaultMeasurePointName,
                             @RequestParam(value = "defaultMeasurePointDescription", required = false) String defaultMeasurePointDescription,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get user
        AppUser user = userService.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        
        // Set user for the location
        location.setUser(user);
        
        // Save location
        Location savedLocation = locationService.saveLocation(location);
        
        // Create and save default measure point
        MeasurePoint defaultMeasurePoint = new MeasurePoint();
        defaultMeasurePoint.setName(defaultMeasurePointName);
        defaultMeasurePoint.setDescription(defaultMeasurePointDescription);
        defaultMeasurePoint.setLocation(savedLocation);
        measurePointService.saveMeasurePoint(defaultMeasurePoint);
        
        redirectAttributes.addFlashAttribute("success", "Lokacija i mjerno mjesto uspješno dodani!");
        return "redirect:/dashboard";
    }
}
