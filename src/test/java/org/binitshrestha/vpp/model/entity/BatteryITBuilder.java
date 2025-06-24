package org.binitshrestha.vpp.model.entity;


public class BatteryITBuilder {
    private String name = "Duracell";
    private String postcode = "44600";
    private Double wattCapacity = 150.00;

    public Battery build(){
        Battery b = new Battery();
        b.setName(name);
        b.setPostcode(postcode);
        b.setWattCapacity(wattCapacity);

        return b;
    }

    public BatteryITBuilder withName(String name){
        this.name = name;
        return this;
    }

    public BatteryITBuilder withWattCapacity(Double wattCapacity){
        this.wattCapacity = wattCapacity;
        return this;
    }
    public BatteryITBuilder withPostcode(String postcode){
        this.postcode = postcode;
        return this;
    }
}

