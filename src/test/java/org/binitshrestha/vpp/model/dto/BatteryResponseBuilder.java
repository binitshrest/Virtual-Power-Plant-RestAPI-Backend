package org.binitshrestha.vpp.model.dto;

public class BatteryResponseBuilder {
    private Long id = 1L;
    private String name = "Test Battery";
    private String postcode = "12345";
    private Double wattCapacity = 500.0;
    private String status = "Active";

    public BatteryResponse build() {
        BatteryResponse br = new BatteryResponse();
        br.setId(id);
        br.setName(name);
        br.setPostcode(postcode);
        br.setWattCapacity(wattCapacity);
        br.setStatus(status);

        return br;
    }
    public BatteryResponseBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public BatteryResponseBuilder withPostcode(String postcode){
        this.postcode = postcode;
        return this;
    }

    public BatteryResponseBuilder withName(String name) {
        this.name = name;
        return this;
    }

}