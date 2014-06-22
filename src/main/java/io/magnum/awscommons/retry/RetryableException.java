package io.magnum.awscommons.retry;

/**
 * The exception indicating that an operation has failed, but it can be
 * retry.
 */
@SuppressWarnings("serial")
public class RetryableException extends Exception {
    public RetryableException() { super(); }
    public RetryableException(String message, Throwable cause) { super(message, cause); }
    public RetryableException(String message) { super(message); }
    public RetryableException(Throwable cause) { super(cause); }
}