package cz.muni.pv168.dragonrental.common;

/**
 * This exception is thrown when an invalid object (with respect to data) is used.
 *
 * @author Zuzana Wolfová
 */
public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}