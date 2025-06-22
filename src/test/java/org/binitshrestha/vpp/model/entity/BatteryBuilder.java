package org.binitshrestha.vpp.model.entity;

public class BatteryBuilder {
    private Long id = 1L;
    private String name = "Duracell";
    private String postcode = "44600";
    private Double wattCapacity = 150.00;

    public Battery build(){
        Battery b = new Battery();
        b.setId(id);
        b.setName(name);
        b.setPostcode(postcode);
        b.setWattCapacity(wattCapacity);

        return b;
    }

    public BatteryBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public BatteryBuilder withName(String name){
        this.name = name;
        return this;
    }

    public BatteryBuilder withWattCapacity(Double wattCapacity){
        this.wattCapacity = wattCapacity;
        return this;
    }
    public BatteryBuilder withPostcode(String postcode){
        this.postcode = postcode;
        return this;
    }
}
