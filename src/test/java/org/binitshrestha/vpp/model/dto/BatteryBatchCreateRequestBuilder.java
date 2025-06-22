package org.binitshrestha.vpp.model.dto;

import java.util.List;

public class BatteryBatchCreateRequestBuilder {
    private List<BatteryCreateRequest> batteries;

    public BatteryBatchCreateRequest build(){
        BatteryBatchCreateRequest batteryBatchCreateRequest = new BatteryBatchCreateRequest();
        batteryBatchCreateRequest.setBatteries(batteries);
        return batteryBatchCreateRequest;
    }

    public BatteryBatchCreateRequestBuilder withBatteries(List<BatteryCreateRequest> batteries){
        this.batteries = batteries;
        return this;
    }
}
