package de.unibremen.opensores.exception;

/**
 * Custom exception for service.
 *
 * @author Kevin Scheck
 */
public class ServiceException extends Exception {
    /**
     * Constructor for a ServiceException with a message.
     * @param message The mesage of the exception, can't be null nor empty.
     */
    public ServiceException(final String message) {
        super(message);
    }
}
