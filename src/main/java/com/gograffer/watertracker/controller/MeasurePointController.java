package com.gograffer.watertracker.controller;

import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.service.LocationService;
import com.gograffer.watertracker.service.MeasurePointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/measure-points")
public class MeasurePointController {

    @Autowired
    private MeasurePointService measurePointService;
    
    @Autowired
    private LocationService locationService;

    /**
     * Display all measure points for a location
     */
    @GetMapping("/location/{locationId}")
    public String viewMeasurePointsByLocation(@PathVariable("locationId") Long locationId,
                                             Model model,
                                             HttpSession session) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        // Get location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            return "redirect:/dashboard";
        }

        // Get measure points for location
        List<MeasurePoint> measurePoints = measurePointService.findByLocation(location);

        model.addAttribute("location", location);
        model.addAttribute("measurePoints", measurePoints);
        return "measurepoints";
    }

    /**
     * Add a new measure point to a location
     */
    @PostMapping("/add")
    public String addMeasurePoint(@RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam("locationId") Long locationId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            redirectAttributes.addFlashAttribute("error", "Morate biti prijavljeni da biste dodali mjerno mjesto.");
            return "redirect:/login";
        }

        // Get location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            redirectAttributes.addFlashAttribute("error", "Lokacija nije pronađena.");
            return "redirect:/dashboard";
        }

        // Create and save measure point
        MeasurePoint measurePoint = new MeasurePoint();
        measurePoint.setName(name);
        measurePoint.setDescription(description);
        measurePoint.setLocation(location);

        measurePointService.saveMeasurePoint(measurePoint);
        redirectAttributes.addFlashAttribute("success", "Mjerno mjesto uspješno dodano!");
        return "redirect:/dashboard?locationId=" + locationId;
    }

    /**
     * Delete a measure point
     */
    @GetMapping("/delete/{id}")
    public String deleteMeasurePoint(@PathVariable("id") Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            redirectAttributes.addFlashAttribute("error", "Morate biti prijavljeni da biste obrisali mjerno mjesto.");
            return "redirect:/login";
        }

        // Get measure point
        MeasurePoint measurePoint = measurePointService.findById(id).orElse(null);
        if (measurePoint == null) {
            redirectAttributes.addFlashAttribute("error", "Mjerno mjesto nije pronađeno.");
            return "redirect:/dashboard";
        }

        Long locationId = measurePoint.getLocation().getId();

        // Check if this is the last measure point for the location
        List<MeasurePoint> locationMeasurePoints = measurePointService.findByLocationId(locationId);
        if (locationMeasurePoints.size() <= 1) {
            redirectAttributes.addFlashAttribute("error", "Ne možete obrisati zadnje mjerno mjesto za lokaciju.");
            return "redirect:/dashboard?locationId=" + locationId;
        }

        // Deactivate measure point instead of deleting it
        measurePointService.deactivateMeasurePoint(id);
        redirectAttributes.addFlashAttribute("success", "Mjerno mjesto uspješno obrisano!");
        return "redirect:/dashboard?locationId=" + locationId;
    }

    @PostMapping("/update")
    public String updateMeasurePoint(@RequestParam("id") Long id,
                                    @RequestParam("name") String name,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam("locationId") Long locationId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Find the measure point
        MeasurePoint measurePoint = measurePointService.findById(id).orElse(null);
        if (measurePoint == null) {
            redirectAttributes.addFlashAttribute("error", "Mjerno mjesto nije pronađeno.");
            return "redirect:/dashboard";
        }

        // Find the location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            redirectAttributes.addFlashAttribute("error", "Lokacija nije pronađena.");
            return "redirect:/dashboard";
        }

        // Check if the location belongs to the user
        if (!location.getUser().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "Nemate pristup ovoj lokaciji.");
            return "redirect:/dashboard";
        }

        // Update the measure point
        measurePoint.setName(name);
        measurePoint.setDescription(description);
        measurePoint.setLocation(location);
        measurePointService.saveMeasurePoint(measurePoint);

        redirectAttributes.addFlashAttribute("success", "Mjerno mjesto uspješno ažurirano.");
        return "redirect:/dashboard?locationId=" + locationId;
    }
    
    @GetMapping("/api/{locationId}")
    @ResponseBody
    public List<MeasurePoint> getMeasurePointsByLocation(@PathVariable("locationId") Long locationId, HttpSession session) {
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // Find the location
        Location location = locationService.findById(locationId).orElse(null);
        if (location == null) {
            return new ArrayList<>();
        }
        
        // Check if the location belongs to the user
        if (!location.getUser().getId().equals(userId)) {
            return new ArrayList<>();
        }
        
        // Return the measure points for this location
        return measurePointService.findByLocationId(locationId);
    }
}
