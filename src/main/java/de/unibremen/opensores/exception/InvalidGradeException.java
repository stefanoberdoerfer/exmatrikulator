package de.unibremen.opensores.exception;

/**
 * Custom exception for service.
 *
 * @author Kevin Scheck
 */
public class InvalidGradeException extends Exception {
    /**
     * Constructor for a ServiceException with a message.
     * @param message The mesage of the exception, can't be null nor empty.
     */
    public InvalidGradeException(final String message) {
        super(message);
    }
}
