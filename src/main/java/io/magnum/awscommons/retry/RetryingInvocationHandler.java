package io.magnum.awscommons.retry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An {@link InvocationHandler} implementation which wraps retry logic around
 * each method invocation.
 * 
 * @author Yu Sun
 */
class RetryingInvocationHandler implements InvocationHandler {
    private final Object delegate;
    private final RetryHelper retryHelper;
    private final ExceptionArbiter defaultArbiter;
    private final Map<Method, ExceptionArbiter> methodSpecificArbiter;
    
    public RetryingInvocationHandler(Object delegate, RetryHelper retryHelper, ExceptionArbiter defaultArbiter,
            Map<Method, ExceptionArbiter> methodSpecificArbiter) {
        this.delegate = delegate;
        this.retryHelper = retryHelper;
        this.defaultArbiter = defaultArbiter;
        this.methodSpecificArbiter = methodSpecificArbiter;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        ExceptionArbiter arbiter = methodSpecificArbiter.get(method);
        if(arbiter == null) {
            arbiter = defaultArbiter;
        }
        
        Retryable<?> retryable = toRetryable(delegate, method, args);
        ExceptionArbiter invocationTargetArbiter = toInvocationTargetArbiter(arbiter);
        
        try {
            return retryHelper.runRetryable(retryable, invocationTargetArbiter);
        } catch(AbortException e) {
            throw new RetryFailedException(e.getCause());
        }
    }

    private static ExceptionArbiter toInvocationTargetArbiter(final ExceptionArbiter delegate) {
        return new ExceptionArbiter() {
            
            @Override
            public boolean isRetryable(Throwable e) {
                if(InvocationTargetRuntimeException.class.isInstance(e)) {
                    return delegate.isRetryable(((InvocationTargetRuntimeException) e).getCause());
                }
                
                return delegate.isRetryable(e);
            }
        };
    }
    
    private static Retryable<Object> toRetryable(final Object delegate, final Method method, final Object[] args) {
        return new Retryable<Object>() {
            @Override
            public Object call() throws RetryableException, AbortException {
                try {
                    return method.invoke(delegate, args);
                } catch (InvocationTargetException e) {
                    throw new InvocationTargetRuntimeException(e);
                } catch (Exception e) {
                    throw new AbortException(e);
                }
            }
            
            @Override
            public String getDescription() {
                return "Calling " + delegate.getClass().getCanonicalName() + "#" + method.getName();
            }
        };
    }
    
    @SuppressWarnings("serial")
    private static class InvocationTargetRuntimeException extends RuntimeException {
        
        public InvocationTargetRuntimeException(InvocationTargetException invocationTargetException) {
            super(invocationTargetException.getCause());
        }
    }
}
