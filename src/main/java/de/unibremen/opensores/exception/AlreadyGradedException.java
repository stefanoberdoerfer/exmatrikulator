package de.unibremen.opensores.exception;

/**
 * Custom exception for service.
 *
 * @author Kevin Scheck
 */
public class AlreadyGradedException extends Exception {
    /**
     * Constructor for a ServiceException with a message.
     */
    public AlreadyGradedException() {
        super("GRADE_ALREADY_EXISTS");
    }
}
