package io.magnum.awscommons.retry;

/**
 * Treats every exception as non-retryable.
 */
public class AlwaysAbortArbiter implements ExceptionArbiter {

    public static final AlwaysAbortArbiter INSTANCE = new AlwaysAbortArbiter();
    
    private AlwaysAbortArbiter() {}
        
    @Override
    public boolean isRetryable(Throwable e) {
        return false;
    }

}