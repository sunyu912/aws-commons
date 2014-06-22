package io.magnum.awscommons;

/**
 * An enumeration of possible instance lifecycle states in an auto-scaling group.
 * 
 * @author Yu Sun
 */
public enum AutoScalingInstanceLifecycleState {
    Pending,
    Quarantined,
    InService,
    Terminating,
    Terminated;
    
    /**
     * Returns the state that matches the specified string in a case-insensitive
     * manner
     */
    public final static AutoScalingInstanceLifecycleState parse(final String string) {
        for (final AutoScalingInstanceLifecycleState state : values()) {
            if (state.name().equalsIgnoreCase(string)) {
                return state;
            }
        }
        return null;
    }
}
