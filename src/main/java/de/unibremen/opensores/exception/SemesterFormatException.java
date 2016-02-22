package de.unibremen.opensores.exception;

/**
 * Exception for mailformated semester strings.
 */
public class SemesterFormatException extends IllegalArgumentException {
    public SemesterFormatException(final String msg) {
        super(msg);
    }
}
