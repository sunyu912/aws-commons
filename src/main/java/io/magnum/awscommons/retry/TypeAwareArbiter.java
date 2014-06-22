package io.magnum.awscommons.retry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Determines retryability of an exception
 * based upon a list of known retryable types.
 */
public class TypeAwareArbiter implements ExceptionArbiter {

    private final Collection<Class<? extends Exception>> retryableTypes;
    
    public TypeAwareArbiter(Collection<Class<? extends Exception>> retryableTypes) {
        this.retryableTypes = new ArrayList<Class<? extends Exception>>(retryableTypes);
    }
    
    @Override
    public boolean isRetryable(Throwable e) {        
        // See if we can treat the exception as retryable
        for (Class<?> type : retryableTypes) {
            if (type.isAssignableFrom(e.getClass())) {
                // We can consider this as a retryable exception
                return true;
            }
        }

        return false;
    }

}
