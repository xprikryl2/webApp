package cz.muni.pv168.dragonrental.common;

/**
 * Exception thrown when database fails.
 *
 * @author Zuzana Wolfová
 */
public class ServiceFailureException extends RuntimeException {

    public ServiceFailureException() {
    }

    public ServiceFailureException(String message) {
        super(message);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
