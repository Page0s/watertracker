package com.gograffer.watertracker.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gograffer.watertracker.model.AppUser;
import com.gograffer.watertracker.model.Location;
import com.gograffer.watertracker.model.WaterConsumption;
import com.gograffer.watertracker.service.LocationService;
import com.gograffer.watertracker.service.UserService;
import com.gograffer.watertracker.service.WaterConsumptionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final UserService userService;
    private final LocationService locationService;
    private final WaterConsumptionService waterConsumptionService;

    @Autowired
    public ReportController(UserService userService, 
                           LocationService locationService,
                           WaterConsumptionService waterConsumptionService) {
        this.userService = userService;
        this.locationService = locationService;
        this.waterConsumptionService = waterConsumptionService;
    }

    @GetMapping
    public String showReports(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
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
        
        // Set default month and year if not provided
        LocalDate now = LocalDate.now();
        if (month == null) month = now.getMonthValue();
        if (year == null) year = now.getYear();
        
        // Get user's locations
        List<Location> locations = locationService.findByUser(user);
        model.addAttribute("locations", locations);
        
        // Prepare data for the chart
        if (!locations.isEmpty()) {
            // Get all water consumption records for user's locations
            List<WaterConsumption> allConsumptions = waterConsumptionService.findByUserLocations(locations);
            
            // Filter by selected month and year
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            List<WaterConsumption> filteredConsumptions = allConsumptions.stream()
                    .filter(c -> !c.getDate().isBefore(startDate) && !c.getDate().isAfter(endDate))
                    .collect(Collectors.toList());
            
            // Prepare data for chart
            prepareChartData(filteredConsumptions, locations, model, startDate);
            
            // Add month and year for display
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
            model.addAttribute("selectedMonth", startDate.format(monthFormatter));
            model.addAttribute("selectedYear", year);
            model.addAttribute("currentMonth", month);
            model.addAttribute("currentYear", year);
        }
        
        // Add months and years for dropdown
        model.addAttribute("months", getMonthsList());
        model.addAttribute("years", getYearsList());
        
        return "reports";
    }
    
    private void prepareChartData(List<WaterConsumption> consumptions, List<Location> locations, Model model, LocalDate startDate) {
        // Group consumptions by day and location
        Map<Integer, Map<String, Double>> dailyConsumptionByLocation = new HashMap<>();
        
        // Initialize the map with all days of the month
        int daysInMonth = startDate.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            Map<String, Double> locationMap = new HashMap<>();
            for (Location location : locations) {
                locationMap.put(location.getName(), 0.0);
            }
            dailyConsumptionByLocation.put(day, locationMap);
        }
        
        // Fill in the actual consumption data
        for (WaterConsumption consumption : consumptions) {
            int day = consumption.getDate().getDayOfMonth();
            String locationName = consumption.getMeasurePoint().getLocation().getName();
            
            Map<String, Double> locationMap = dailyConsumptionByLocation.get(day);
            if (locationMap != null) {
                Double currentAmount = locationMap.get(locationName);
                if (currentAmount != null) {
                    locationMap.put(locationName, currentAmount + consumption.getAmount());
                }
            }
        }
        
        // Prepare data for chart.js
        List<Integer> days = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            days.add(day);
        }
        
        List<Map<String, Object>> datasets = new ArrayList<>();
        
        // Define colors for each location
        String[] colors = {
            "rgba(255, 99, 132, 0.7)",    // Red
            "rgba(54, 162, 235, 0.7)",    // Blue
            "rgba(255, 206, 86, 0.7)",    // Yellow
            "rgba(75, 192, 192, 0.7)",    // Teal
            "rgba(153, 102, 255, 0.7)",   // Purple
            "rgba(255, 159, 64, 0.7)",    // Orange
            "rgba(199, 199, 199, 0.7)",   // Gray
            "rgba(83, 102, 255, 0.7)",    // Indigo
            "rgba(255, 99, 255, 0.7)",    // Pink
            "rgba(99, 255, 132, 0.7)"     // Light green
        };
        
        // Create a dataset for each location
        int colorIndex = 0;
        for (Location location : locations) {
            Map<String, Object> dataset = new HashMap<>();
            dataset.put("label", location.getName());
            dataset.put("backgroundColor", colors[colorIndex % colors.length]);
            dataset.put("borderColor", colors[colorIndex % colors.length].replace("0.7", "1.0"));
            
            List<Double> data = new ArrayList<>();
            for (int day = 1; day <= daysInMonth; day++) {
                Map<String, Double> locationMap = dailyConsumptionByLocation.get(day);
                data.add(locationMap.get(location.getName()));
            }
            
            dataset.put("data", data);
            datasets.add(dataset);
            colorIndex++;
        }
        
        // Convert data to JSON strings to ensure proper serialization in Thymeleaf
        model.addAttribute("chartDays", days);
        model.addAttribute("chartDatasets", datasets);
        
        // Calculate total consumption by location for the pie chart
        Map<String, Double> totalByLocation = new HashMap<>();
        for (Location location : locations) {
            totalByLocation.put(location.getName(), 0.0);
        }
        
        for (WaterConsumption consumption : consumptions) {
            String locationName = consumption.getMeasurePoint().getLocation().getName();
            Double currentTotal = totalByLocation.get(locationName);
            if (currentTotal != null) {
                totalByLocation.put(locationName, currentTotal + consumption.getAmount());
            }
        }
        
        List<String> pieLabels = new ArrayList<>(totalByLocation.keySet());
        List<Double> pieData = new ArrayList<>();
        
        // Ensure we only include locations with data
        for (String label : pieLabels) {
            pieData.add(totalByLocation.get(label));
        }
        
        // Remove locations with 0 consumption
        List<String> finalPieLabels = new ArrayList<>();
        List<Double> finalPieData = new ArrayList<>();
        List<String> finalPieColors = new ArrayList<>();
        
        for (int i = 0; i < pieLabels.size(); i++) {
            if (pieData.get(i) > 0) {
                finalPieLabels.add(pieLabels.get(i));
                finalPieData.add(pieData.get(i));
                finalPieColors.add(colors[i % colors.length]);
            }
        }
        
        model.addAttribute("pieLabels", finalPieLabels);
        model.addAttribute("pieData", finalPieData);
        model.addAttribute("pieColors", finalPieColors);
    }
    
    private Map<Integer, String> getMonthsList() {
        Map<Integer, String> months = new HashMap<>();
        months.put(1, "Siječanj");
        months.put(2, "Veljača");
        months.put(3, "Ožujak");
        months.put(4, "Travanj");
        months.put(5, "Svibanj");
        months.put(6, "Lipanj");
        months.put(7, "Srpanj");
        months.put(8, "Kolovoz");
        months.put(9, "Rujan");
        months.put(10, "Listopad");
        months.put(11, "Studeni");
        months.put(12, "Prosinac");
        return months;
    }
    
    private List<Integer> getYearsList() {
        List<Integer> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            years.add(year);
        }
        return years;
    }
}
