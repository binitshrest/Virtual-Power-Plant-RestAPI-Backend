package org.binitshrestha.vpp.model.dto;

import org.binitshrestha.vpp.model.BatteryCreateRequest;

public class BatteryCreateRequestBuilder {
    private String name = "Duracell";
    private String postcode = "44600";
    private Double wattCapacity = 150.00;

    public BatteryCreateRequest build(){
        BatteryCreateRequest bcr = new BatteryCreateRequest();
        bcr.setName(name);
        bcr.setPostcode(postcode);
        bcr.setWattCapacity(wattCapacity);

        return bcr;
    }

    public BatteryCreateRequestBuilder withName(String name){
        this.name = name;
        return this;
    }


}
