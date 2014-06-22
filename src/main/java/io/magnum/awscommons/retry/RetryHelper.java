package io.magnum.awscommons.retry;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that provides a framework for retrying tasks.
 * 
 * @author Yu Sun
 */
public class RetryHelper {
    
    private final static Logger logger = LoggerFactory.getLogger(RetryHelper.class);

    /**
     * An instance of this class that will retry tasks until they succeed.
     */
    public final static RetryHelper RETRY_FOREVER = new RetryHelper.Builder().build();

    /**
     * An instance of this class that will try a task a maximum of one time.
     */
    public final static RetryHelper RUN_ONCE = new RetryHelper.Builder().withMaxTries(1).build();
    
    /**
     * The maximum amount of tolerable drift for the purposes of calculating
     * temporal offsets between "wall clock" time and nanosecond time.
     */
    private final static long MAX_DRIFT_NANOS = TimeUnit.MILLISECONDS.toNanos(1)/10L;

    /**
     * The timeout after which to give up retrying a task; if <code>null</code>,
     * there is no timeout.
     */
    private final Long timeout;

    /**
     * The unit in which the timeout is expressed.
     */
    private final TimeUnit timeoutUnit;

    /**
     * The deadline after which to give up retrying a task; if <code>null</code>,
     * there is no deadline.
     */
    private final Long deadlineNanos;

    /**
     * The maximum number of attempts to make, including the initial attempt;
     * if <code>null</code>, the number of attempts is unlimited.
     */
    private final Integer maxTries;

    /**
     * The amount of time to wait in between retries if retries are
     * necessary and possible; if <code>null</code>, there is no
     * delay between retries.
     */
    private final Long retryInterval;

    /**
     * The unit in which the retry interval is expressed.
     */
    private final TimeUnit retryUnit;

    
    public final static class Builder {
        private Long timeout;
        private TimeUnit timeoutUnit;
        private Long deadlineNanos;
        private Integer maxTries;
        private Long retryInterval;
        private TimeUnit retryUnit;

        public Builder() {
        }

        public RetryHelper build() {
            return new RetryHelper(timeout, timeoutUnit, deadlineNanos, maxTries, retryInterval, retryUnit);
        }

        public Builder withTimeout(final long timeout, final TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        public Builder withDeadlineUtcMillis(final long deadlineUtcMillis) {
            this.deadlineNanos = utcMillisToSystemNanos(deadlineUtcMillis);
            return this;
        }

        public Builder withMaxTries(final int maxTries) {
            this.maxTries = maxTries;
            return this;
        }

        public Builder withRetryInterval(final long retryInterval, final TimeUnit retryUnit) {
            this.retryInterval = retryInterval;
            this.retryUnit = retryUnit;
            return this;
        }
    }

    private RetryHelper(final Long timeout, final TimeUnit timeoutUnit,
            final Long deadlineNanos,
            final Integer maxTries,
            final Long retryInterval, final TimeUnit retryUnit) {
        
        if ( (timeout != null && timeoutUnit == null) || (timeout == null && timeoutUnit != null)) {
            throw new IllegalArgumentException(
                    "timeout and timeoutUnit must be either both specified or both null: timeout="
                    + timeout + ", timeoutUnit=" + timeoutUnit);
        }
        if ( (retryInterval != null && retryUnit == null) || (retryInterval == null && retryUnit != null)) {
            throw new IllegalArgumentException(
                    "retryInterval and retryUnit must be either both specified or both null: retryInterval="
                    + retryInterval + ", retryUnit=" + retryUnit);
        }
        if (timeout != null && timeout < 0) {
            throw new IllegalArgumentException("timeout must be null or >= 0: " + timeout);
        }
        if (maxTries != null && maxTries < 1) {
            throw new IllegalArgumentException("maxTries must be null or >= 1: " + maxTries);
        }
        if (retryInterval != null  && retryInterval < 0) {
            throw new IllegalArgumentException("retryInterval must be null or >= 0: " + retryInterval);
        }

        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.deadlineNanos = deadlineNanos;
        this.maxTries = maxTries;
        this.retryInterval = retryInterval;
        this.retryUnit = retryUnit;
    }
    
    private final static long utcMillisToSystemNanos(final long utcMillis) {
        long systemNanos;
        long systemUtcMillis;
        do {
            systemNanos = System.nanoTime();
            systemUtcMillis = System.currentTimeMillis();
        } while (System.nanoTime() - systemNanos > MAX_DRIFT_NANOS);
        return systemNanos + TimeUnit.MILLISECONDS.toNanos(utcMillis - systemUtcMillis);
    }

    public <T> T runRetryable(final Retryable<T> task) throws AbortException {
        return runRetryable(task, AlwaysAbortArbiter.INSTANCE);
    }
    
    public <T> T runRetryable(final Retryable<T> task, ExceptionArbiter arbiter)
            throws AbortException {
        return run(task, arbiter);
    }

    /**
     * Calculates and returns the fewest number of nanoseconds that may elapse
     * before a timeout occurs.     
     */
    protected Long shortestTimeoutNanos() {
        // Calculate a timeout using the rules discussed in the constructor.
        final long nowNanos = System.nanoTime();
        if (timeout != null) {
            if (deadlineNanos == null) {
                // Only a timeout is specified, use it.
                return timeoutUnit.toNanos(timeout);
            } else {
                // Both are specified, use whichever one happens soonest
                long timeoutDeadlineNanos = nowNanos + timeoutUnit.toNanos(timeout);
                timeoutDeadlineNanos = Math.min(timeoutDeadlineNanos, deadlineNanos);
                return timeoutDeadlineNanos - nowNanos;
            }
        } else if (deadlineNanos != null) {
            // Only a deadline is specified, use it.
            return deadlineNanos - nowNanos;
        } // else Neither is specified, there is no timeout of any kind
        return null;
    }

    /**
     * Runs the specified task with appropriate retry logic.
     */
    protected <T> T run(final Retryable<T> task, final ExceptionArbiter arbiter)
    		throws AbortException {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }

        // Convenience conversions
        final long startNanos = System.nanoTime();
        final Long deadlineNanos;
        if (timeout == null) {
            // No deadline, run till we succeed or die trying
            deadlineNanos = null;
        } else {
            // Deadline imposed, honor it.
            deadlineNanos = startNanos + shortestTimeoutNanos();
        }
        final Long retryIntervalNanos;
        if (retryInterval == null) {
            retryIntervalNanos = null;
        } else {
            retryIntervalNanos = TimeUnit.MILLISECONDS.toNanos(retryInterval);
        }

        int numTries = 0;
        while ((maxTries == null || numTries < maxTries) && (deadlineNanos == null || System.nanoTime() < deadlineNanos)) {
            numTries++;
            if (numTries > 1) {
                logger.info("attempt #" + numTries + " of " + (maxTries == null ? "unlimited" : maxTries) + " for task: " + task.getDescription());
            }
            try {
                return task.call();
            } catch (RetryableException e) {                
                logger.info("task failed but indicates that retry is acceptable: " + task.getDescription() + ": " + e.getMessage());
            } catch (AbortException e) {
                throw e;
            } catch (Exception e) {
                if (arbiter.isRetryable(e)) {
                    logger.info("task failed but exception class " + e.getClass().getName() + " was considered retryable: "+ task.getDescription(), e);
                } else {
                    throw new AbortException("unhandled exception raised in task, "
                        + "assuming that retry is unacceptable: "
                        + task.getDescription(), e);
                }
            }

            // We haven't returned and we haven't thrown an exception.
            // Sleep and retry later.
            // We do this outside of our try/catch above so that we can
            // isolate any interrupted exception here.
            if ((maxTries == null || (numTries < maxTries)) && retryInterval != null) {
                final Date nextRetryAt = new Date(System.currentTimeMillis() + retryUnit.toMillis(retryInterval));
                logger.info("will retry in " + retryInterval + " " + retryUnit.toString().toLowerCase()
                        + " (at approximately " + nextRetryAt + ")");
            }

            if ((deadlineNanos != null) && (System.nanoTime() + retryIntervalNanos >= deadlineNanos)) {
                // We will run out of time if we try to sleep again
                final long elapsedNanos = System.nanoTime() - startNanos;
                final String message = "task did not succeed after " + numTries
                        + " attempts in " + elapsedNanos + "ns, and timeout will "
                        + "elapse before the next attempt can be made; giving up: "
                        + task.getDescription();
                throw new AbortException(message, new TimeoutException(message));
            }

            if (retryInterval != null && retryInterval > 0) {
                final long wakeAtNanos = System.nanoTime() + retryUnit.toNanos(retryInterval);
                try {
                    long nanosRemaining = wakeAtNanos - System.nanoTime();
                    while (nanosRemaining > 0) {
                        // There's no simple way to sleep for x nanos; instead
                        // we have to sleep for x millis and y nanos.  Compute
                        // x and y here.
                        final long sleepMillis = TimeUnit.NANOSECONDS.toMillis(nanosRemaining); // truncates fractional milliseconds
                        final int sleepNanos = (int) (nanosRemaining - TimeUnit.MILLISECONDS.toNanos(sleepMillis)); // the fractional millisecond
                        Thread.sleep(sleepMillis, sleepNanos);
                        nanosRemaining = wakeAtNanos - System.nanoTime();
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted(); // clear flag because we aren't bubbling up the raw InterruptedException
                    throw new AbortException("interrupted while waiting to retry task " + task.getDescription(), e);
                }
            }
        }
        final long endNanos = System.nanoTime();
        final long elapsedNanos = endNanos - startNanos;
        final String message = "task did not succeed after " + numTries
                + " attempts in " + elapsedNanos + "ns; giving up: "
                + task.getDescription();
        throw new AbortException(message, new TimeoutException(message));
    }

    
    public Long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public Long getDeadlineNanos() {
        return deadlineNanos;
    }

    public Integer getMaxTries() {
        return maxTries;
    }

    public Long getRetryInterval() {
        return retryInterval;
    }

    public TimeUnit getRetryUnit() {
        return retryUnit;
    }
}
