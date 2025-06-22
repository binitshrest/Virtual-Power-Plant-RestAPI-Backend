package org.binitshrestha.vpp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryBatchCreateRequest {
    private List<BatteryCreateRequest> batteries;
}
