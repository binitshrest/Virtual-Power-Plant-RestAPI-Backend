package org.binitshrestha.vpp.exception;

public class BatteryNotFoundException extends RuntimeException{
    public BatteryNotFoundException(String message){
        super(message);
    }

}
