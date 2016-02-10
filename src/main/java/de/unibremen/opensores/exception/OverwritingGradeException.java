package de.unibremen.opensores.exception;

/**
 * Custom exception for service.
 *
 * @author Kevin Scheck
 */
public class OverwritingGradeException extends Exception {
    public OverwritingGradeException() {
        super("GRADE_ALREADY_EXISTS");
    }
}
