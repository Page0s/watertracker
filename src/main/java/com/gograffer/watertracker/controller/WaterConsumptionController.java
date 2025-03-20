package com.gograffer.watertracker.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gograffer.watertracker.model.MeasurePoint;
import com.gograffer.watertracker.model.WaterConsumption;
import com.gograffer.watertracker.service.MeasurePointService;
import com.gograffer.watertracker.service.WaterConsumptionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/consumption")
public class WaterConsumptionController {

    private final WaterConsumptionService waterConsumptionService;
    private final MeasurePointService measurePointService;

    @Autowired
    public WaterConsumptionController(WaterConsumptionService waterConsumptionService,
                                     MeasurePointService measurePointService) {
        this.waterConsumptionService = waterConsumptionService;
        this.measurePointService = measurePointService;
    }

    @PostMapping("/add")
    public String addConsumption(@RequestParam("measurePointId") Long measurePointId,
                                @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(value = "hotWaterMeterReading", required = true) Double hotWaterMeterReading,
                                @RequestParam(value = "coldWaterMeterReading", required = true) Double coldWaterMeterReading,
                                @RequestParam(value = "unit", required = false, defaultValue = "m³") String unit,
                                @RequestParam(value = "notes", required = false) String notes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get measure point
        MeasurePoint measurePoint = measurePointService.findById(measurePointId).orElse(null);
        if (measurePoint == null) {
            redirectAttributes.addFlashAttribute("error", "Mjerno mjesto nije pronađeno!");
            return "redirect:/dashboard";
        }
        
        // Create water consumption object
        WaterConsumption consumption = new WaterConsumption();
        consumption.setMeasurePoint(measurePoint);
        consumption.setDate(date);
        consumption.setUnit(unit);
        consumption.setNotes(notes);
        
        // Set meter readings
        consumption.setHotWaterMeterReading(hotWaterMeterReading);
        consumption.setColdWaterMeterReading(coldWaterMeterReading);
        
        // Calculate total meter reading
        consumption.setMeterReading(hotWaterMeterReading + coldWaterMeterReading);
        
        // Save consumption (period consumption will be calculated in the service)
        waterConsumptionService.saveWaterConsumption(consumption);
        
        redirectAttributes.addFlashAttribute("success", "Potrošnja vode uspješno dodana!");
        return "redirect:/dashboard";
    }
    
    @PostMapping("/edit")
    public String editConsumption(
            @RequestParam("id") Long id,
            @RequestParam("measurePointId") Long measurePointId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("hotWaterMeterReading") Double hotWaterMeterReading,
            @RequestParam("coldWaterMeterReading") Double coldWaterMeterReading,
            @RequestParam("unit") String unit,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        // Get the current user
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Morate biti prijavljeni za uređivanje potrošnje vode!");
            return "redirect:/login";
        }
        
        // Get the water consumption record
        WaterConsumption consumption = waterConsumptionService.findById(id).orElse(null);
        if (consumption == null) {
            redirectAttributes.addFlashAttribute("error", "Potrošnja vode nije pronađena!");
            return "redirect:/dashboard";
        }
        
        // Verify that the consumption belongs to the current user
        if (!consumption.getMeasurePoint().getLocation().getUser().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "Nemate dozvolu za uređivanje ove potrošnje vode!");
            return "redirect:/dashboard";
        }
        
        // Get the measure point
        MeasurePoint measurePoint = measurePointService.findById(measurePointId).orElse(null);
        if (measurePoint == null) {
            redirectAttributes.addFlashAttribute("error", "Mjerno mjesto nije pronađeno!");
            return "redirect:/dashboard";
        }
        
        // Verify that the measure point belongs to the current user
        if (!measurePoint.getLocation().getUser().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "Nemate dozvolu za korištenje ovog mjernog mjesta!");
            return "redirect:/dashboard";
        }
        
        // Update the consumption record
        consumption.setMeasurePoint(measurePoint);
        consumption.setDate(date);
        consumption.setHotWaterMeterReading(hotWaterMeterReading);
        consumption.setColdWaterMeterReading(coldWaterMeterReading);
        consumption.setUnit(unit);
        consumption.setNotes(notes);
        
        // Save the updated consumption record
        waterConsumptionService.saveWaterConsumption(consumption);
        
        redirectAttributes.addFlashAttribute("success", "Potrošnja vode uspješno ažurirana!");
        return "redirect:/dashboard";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteConsumption(@PathVariable("id") Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get consumption
        WaterConsumption consumption = waterConsumptionService.findById(id).orElse(null);
        if (consumption == null) {
            redirectAttributes.addFlashAttribute("error", "Unos potrošnje nije pronađen!");
            return "redirect:/dashboard";
        }
        
        // Delete consumption
        waterConsumptionService.deleteById(id);
        
        redirectAttributes.addFlashAttribute("success", "Potrošnja vode uspješno obrisana!");
        return "redirect:/dashboard";
    }
}
