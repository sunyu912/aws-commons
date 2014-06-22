package io.magnum.awscommons;

/**
 * An enumeration of possible instance states in an elastic load
 * balancer.
 * 
 * @author Yu Sun
 */
public enum ElasticLoadBalancingInstanceState {
    InService,
    OutOfService,
    Unknown;

    /**
     * Returns the state that matches the specified string in a case-insensitive
     * manner.
     */
    public final static ElasticLoadBalancingInstanceState parse(final String string) {
        for (final ElasticLoadBalancingInstanceState state : values()) {
            if (state.name().equalsIgnoreCase(string)) {
                return state;
            }
        }
        return null;
    }
}