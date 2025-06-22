package org.binitshrestha.vpp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostcodeRangeResponse {
    private List<String> batteryNames;
    private BatteryStatistics statistics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatteryStatistics {
        private int totalBatteries;
        private Double totalWattCapacity;
        private Double averageWattCapacity;
        private Double minWattCapacity;
        private Double maxWattCapacity;
    }
}
