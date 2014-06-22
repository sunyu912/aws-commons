package io.magnum.awscommons.retry;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

/**
 * Treats typical AWS exception types as retryable. 
 */
public class AwsExceptionArbiter implements ExceptionArbiter {

    private final static ExceptionArbiter ARBITER;
    
    static {
        List<Class<? extends Exception>> list = new ArrayList<Class<? extends Exception>>(2);
        list.add(AmazonClientException.class);
        list.add(AmazonServiceException.class);
        ARBITER = new TypeAwareArbiter(list);
    }
    
    @Override
    public boolean isRetryable(Throwable t) {
        return ARBITER.isRetryable(t);
    }

}
