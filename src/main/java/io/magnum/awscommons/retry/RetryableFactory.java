package io.magnum.awscommons.retry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

/**
 * Dynamically creates interface implementations whose methods are all wrapped
 * in retry logic. 
 */
public class RetryableFactory {

    public static <T, D extends T> T create(Class<T> interfaze, D delegate, RetryHelper retryHelper) {
        return create(interfaze, delegate, retryHelper, AlwaysAbortArbiter.INSTANCE);
    }
    
    public static <T, D extends T> T create(Class<T> interfaze, D delegate, RetryHelper retryHelper,
            ExceptionArbiter defaultArbiter) {
        return create(interfaze, delegate, retryHelper, defaultArbiter, Collections.<Method, ExceptionArbiter>emptyMap());
    }
    
    @SuppressWarnings("unchecked")
    public static <T, D extends T> T create(Class<T> interfaze, D delegate, RetryHelper retryHelper,
            ExceptionArbiter defaultArbiter, Map<Method, ExceptionArbiter> methodSpecificArbiter) {
        
        Class<?>[] interfaces = new Class<?>[] { interfaze };
        InvocationHandler handler = new RetryingInvocationHandler(delegate, retryHelper, defaultArbiter, methodSpecificArbiter);
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), interfaces, handler);
    }
}
