package org.binitshrestha.vpp.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostcodeRangeRequest {
    @NotBlank(message = "Start postcode is required")
    private String startPostcode;

    @NotBlank(message = "End postcode is required")
    private String endPostcode;

    private Double minWattCapacity;
    private Double maxWattCapacity;
}
