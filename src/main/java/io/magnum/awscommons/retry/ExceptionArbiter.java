package io.magnum.awscommons.retry;

/**
 * Determine whether a throwable is retryable
 */
public interface ExceptionArbiter {
    
    /**
     * Returns true if and only if the throwable indicates a retryable failure.
     */
    boolean isRetryable(Throwable t);
}
