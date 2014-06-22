package io.magnum.awscommons;

import io.magnum.awscommons.retry.AwsExceptionArbiter;
import io.magnum.awscommons.retry.ExceptionArbiter;
import io.magnum.awscommons.retry.RetryHelper;
import io.magnum.awscommons.retry.RetryableFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;


/**
 * A factory for producing {@link GeneralAwsHelper} implementations which have
 * configurable retry logic.
 */
public class RetryingGeneralAwsHelperFactory {

    private final static ExceptionArbiter DEFAULT_ARBITER = new AwsExceptionArbiter();
    
    private final static Map<Method, ExceptionArbiter> METHOD_SPECIFIC_ARBITER;
    static {
        try {
            Method method = GeneralAwsHelper.class.getMethod("terminateViaAutoScaling", String.class);
            ExceptionArbiter arbiter = new AutoScalingTerminationArbiter(DEFAULT_ARBITER);
            
            METHOD_SPECIFIC_ARBITER = Collections.singletonMap(method, arbiter);
        } catch(NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
    
    public static GeneralAwsHelper newInstance(AwsContext context, RetryHelper retryHelper) {
        return newInstance(new DefaultGeneralAwsHelper(context), retryHelper);
    }

    public static GeneralAwsHelper newInstance(GeneralAwsHelper awsHelper, RetryHelper retryHelper) {
        retryHelper = (retryHelper != null ? retryHelper : RetryHelper.RETRY_FOREVER);
        return RetryableFactory.create(GeneralAwsHelper.class, awsHelper, retryHelper, DEFAULT_ARBITER, METHOD_SPECIFIC_ARBITER);
    }    
}
