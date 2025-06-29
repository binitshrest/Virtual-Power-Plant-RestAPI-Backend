package org.binitshrestha.vpp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;
    private String message;
    private int totalBatteries;
}