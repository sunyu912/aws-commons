package io.magnum.awscommons;

/**
 * Represents the possible health states of an instance in an auto scaling group.
 * 
 * @author Yu Sun
 */
public enum AutoScalingInstanceHealthStatus {
    Healthy,
    Unhealthy;
    
    /**
     * Returns the status that matches the specified string in a case-insensitive
     * manner.
     */
    public final static AutoScalingInstanceHealthStatus parse(final String string) {
        for (final AutoScalingInstanceHealthStatus status : values()) {
            if (status.name().equalsIgnoreCase(string)) {
                return status;
            }
        }
        return null;
    }
}
