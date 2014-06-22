package io.magnum.awscommons.retry;

/**
 * An exception which indicates that an operation was retried but ultimately
 * failed. 
 */
@SuppressWarnings("serial")
public class RetryFailedException extends RuntimeException {

    public RetryFailedException() {
        super();
    }

    public RetryFailedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public RetryFailedException(String arg0) {
        super(arg0);
    }

    public RetryFailedException(Throwable arg0) {
        super(arg0);
    }
}
