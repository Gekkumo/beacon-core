package beacon.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BeaconException extends RuntimeException {

    protected BeaconException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();

    public abstract String getErrorCode();
}