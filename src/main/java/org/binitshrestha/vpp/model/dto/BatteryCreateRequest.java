package org.binitshrestha.vpp.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryCreateRequest {
    @JsonProperty("name")
    @NotBlank(message = "Battery name is required")
    private String name;

    @JsonProperty("postcode")
    @NotBlank(message = "Postcode is required")
    private String postcode;

    @NotNull(message = "Watt capacity is required")
    @Positive(message = "Watt capacity must be positive")
    @JsonProperty("capacity")
    private Double wattCapacity;
}
