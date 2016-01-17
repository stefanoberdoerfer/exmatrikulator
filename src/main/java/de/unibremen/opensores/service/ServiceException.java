package de.unibremen.opensores.service;

/**
 * Custom exception for the service layer.
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
