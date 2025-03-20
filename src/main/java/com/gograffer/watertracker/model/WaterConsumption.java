package com.gograffer.watertracker.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "water_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_point_id")
    private MeasurePoint measurePoint;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    // New fields for meter readings
    private Double meterReading;
    private Double hotWaterMeterReading;
    private Double coldWaterMeterReading;
    
    // Fields for period consumption (difference between readings)
    private Double periodConsumption;
    private Double hotWaterPeriodConsumption;
    private Double coldWaterPeriodConsumption;

    private String unit;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String notes;
    
    // Method to calculate period consumption from previous reading
    public void calculatePeriodConsumption(WaterConsumption previousReading) {
        if (previousReading != null) {
            // Calculate total consumption
            if (this.meterReading != null && previousReading.getMeterReading() != null) {
                this.periodConsumption = this.meterReading - previousReading.getMeterReading();
                // Ensure we don't have negative values due to meter replacement
                if (this.periodConsumption < 0) {
                    this.periodConsumption = this.meterReading;
                }
            }
            
            // Calculate hot water consumption
            if (this.hotWaterMeterReading != null && previousReading.getHotWaterMeterReading() != null) {
                this.hotWaterPeriodConsumption = this.hotWaterMeterReading - previousReading.getHotWaterMeterReading();
                // Ensure we don't have negative values due to meter replacement
                if (this.hotWaterPeriodConsumption < 0) {
                    this.hotWaterPeriodConsumption = this.hotWaterMeterReading;
                }
            }
            
            // Calculate cold water consumption
            if (this.coldWaterMeterReading != null && previousReading.getColdWaterMeterReading() != null) {
                this.coldWaterPeriodConsumption = this.coldWaterMeterReading - previousReading.getColdWaterMeterReading();
                // Ensure we don't have negative values due to meter replacement
                if (this.coldWaterPeriodConsumption < 0) {
                    this.coldWaterPeriodConsumption = this.coldWaterMeterReading;
                }
            }
        } else {
            // If there's no previous reading, the period consumption is the same as the meter reading
            this.periodConsumption = this.meterReading;
            this.hotWaterPeriodConsumption = this.hotWaterMeterReading;
            this.coldWaterPeriodConsumption = this.coldWaterMeterReading;
        }
        
        // Update the amount field to maintain backward compatibility
        this.amount = (this.periodConsumption != null) ? this.periodConsumption : 0.0;
    }
}
