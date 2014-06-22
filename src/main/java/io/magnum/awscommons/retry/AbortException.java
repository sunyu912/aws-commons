package io.magnum.awscommons.retry;

/**
 * An exception thrown when an operation has failed, which
 * is not retriable.
 */

@SuppressWarnings("serial")
public class AbortException extends Exception {

    public AbortException() {
        super();
    }

    public AbortException(String message) {
        super(message);
    }

    public AbortException(Throwable cause) {
        super(cause);
    }

    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
