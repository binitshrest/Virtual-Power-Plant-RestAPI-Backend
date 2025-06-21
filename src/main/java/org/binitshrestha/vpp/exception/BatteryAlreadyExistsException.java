package org.binitshrestha.vpp.exception;

public class BatteryAlreadyExistsException extends RuntimeException{
    public BatteryAlreadyExistsException (String message){
        super(message);
    }
    public BatteryAlreadyExistsException (String message, Throwable cause){
        super(message, cause);
    }
}
