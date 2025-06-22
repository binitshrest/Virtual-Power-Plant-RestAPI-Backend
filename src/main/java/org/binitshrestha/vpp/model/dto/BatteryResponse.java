package org.binitshrestha.vpp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryResponse {
    private Long id;
    private String name;
    private String postcode;
    private Double wattCapacity;
    private String status;
}