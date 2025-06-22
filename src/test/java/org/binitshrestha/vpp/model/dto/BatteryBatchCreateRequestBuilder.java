package org.binitshrestha.vpp.model.dto;

import org.binitshrestha.vpp.model.BatteryBatchCreateRequest;
import org.binitshrestha.vpp.model.BatteryCreateRequest;

import java.util.List;

public class BatteryBatchCreateRequestBuilder {
    private List<BatteryCreateRequest> batteries;

    public BatteryBatchCreateRequest build(){
        BatteryBatchCreateRequest batteryBatchCreateRequest = new BatteryBatchCreateRequest();
        batteryBatchCreateRequest.setBatteries(batteries);
        return batteryBatchCreateRequest;
    }
}
