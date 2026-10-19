package us.dot.its.jpo.ode.api.services.firmware;

/**
 * An OBU firmware publish or delete request that conflicts with the current
 * catalog or storage state. Reported as 409 so the admin can refresh and retry.
 */
public class ObuFirmwareConflictException extends RuntimeException {
    public ObuFirmwareConflictException(String message) {
        super(message);
    }
}
