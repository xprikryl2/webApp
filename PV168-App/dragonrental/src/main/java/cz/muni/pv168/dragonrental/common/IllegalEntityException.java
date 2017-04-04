package cz.muni.pv168.dragonrental.common;

/**
 * This exception is thrown when an illegal entity is used in an operation.
 *
 * @author Zuzana Wolfová
 */
public class IllegalEntityException extends RuntimeException {

    public IllegalEntityException() {
    }

    public IllegalEntityException(String message) {
        super(message);
    }

    public IllegalEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalEntityException(Throwable cause) {
        super(cause);
    }
}
