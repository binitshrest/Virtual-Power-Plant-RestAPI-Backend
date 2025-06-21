package org.binitshrestha.vpp.model;


import lombok.AllArgsConstructor;
import lombok.Data;

// Helper class for postcode statistics
@Data
@AllArgsConstructor
public class PostcodeStatistics {
    private String postcode;
    private int batteryCount;
    private double totalCapacity;
    private double averageCapacity;
}
