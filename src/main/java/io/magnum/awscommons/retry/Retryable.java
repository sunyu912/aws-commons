package io.magnum.awscommons.retry;

import java.util.concurrent.Callable;


/**
 * Like {@link Callable}, but this is used in retry cases.
 */
public abstract class Retryable<T> implements Callable<T> {
	
    /**
     * Like {@link Callable#call()}, but with type-narrowing on the kinds of
     * exceptions that can be thrown.
     * <p>
     * Subclasses should override this method to perform useful work.  
     */
    abstract public T call() throws RetryableException, AbortException;

    /**
     * Returns a description of the retryable task for logging
     */
    public String getDescription() {
        return toString();
    }
}