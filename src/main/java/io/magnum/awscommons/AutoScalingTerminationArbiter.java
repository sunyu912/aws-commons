package io.magnum.awscommons;

import io.magnum.awscommons.retry.ExceptionArbiter;

import com.amazonaws.AmazonServiceException;

/**
 * ExceptionArbiter which defers to a parameter arbiter except in the following
 * case(s):
 * 
 * @author Yu Sun
 */
public class AutoScalingTerminationArbiter implements ExceptionArbiter {

    static final String INSTANCE_NOT_FOUND_ERROR_CODE = "ValidationError";
    static final String INSTANCE_NOT_FOUND_MESSAGE_PREFIX = "Instance Id not found - No managed instance found for instance ID";
    
    private final ExceptionArbiter arbiter;
    
    public AutoScalingTerminationArbiter(ExceptionArbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Override
    public boolean isRetryable(Throwable e) {
        return arbiter.isRetryable(e) && ! isInstanceIDNotFoundException(e);
    }
    
    private boolean isInstanceIDNotFoundException(Throwable e) {
        if(e instanceof AmazonServiceException) {
            AmazonServiceException amznEx = (AmazonServiceException) e;
            
            if(INSTANCE_NOT_FOUND_ERROR_CODE.equals(amznEx.getErrorCode()) && e.getMessage() != null 
                    && e.getMessage().startsWith(INSTANCE_NOT_FOUND_MESSAGE_PREFIX)) {
                return true;
            }
        }
        
        return false;
    }
}
