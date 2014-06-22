package io.magnum.awscommons;

/**
 * An enum to represent the EC2 status.
 * <p>
 * See <a href='http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeInstances.html'>
 * http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeInstances.html</a>,
 * specifically the "DescribeInstances" method.
 * 
 * @author Yu Sun
 */
public enum Ec2InstanceState {
    /**
     * Initial state for all instances, not yet running.
     */
    PENDING(0, "pending"),

    /**
     * Completed allocation, running.
     */
    RUNNING(16, "running"),

    /**
     * Getting ready for termination; aka "terminating" in other services.
     */
    SHUTTING_DOWN(32, "shutting-down"),

    /**
     * Terminated, will never come back again.
     */
    TERMINATED(48, "terminated"),

    /**
     * Like shutting down a real computer, can be "turned back on" later.
     */
    STOPPING(64, "stopping"),

    /**
     * Currently not running, but can be "turned back on" later.
     */
    STOPPED(80, "stopped");
    
    /**
     * The numerical representation of the state
     */
    private final int stateId;

    /**
     * The lexical representation of the state
     */
    private final String stateString;


    private Ec2InstanceState(final int stateId, final String stateString) {
        this.stateId = stateId;
        this.stateString = stateString;
    }

    /**
     * Returns the state that matches the specified string in a case-insensitive
     * manner, if any; otherwise, <code>null</code>.  This method never throws
     * an exception.
     * 
     * @param string the string to parse
     * @return as described
     */
    public final static Ec2InstanceState parse(final String string) {
        for (final Ec2InstanceState state : values()) {
            if (state.stateString.equalsIgnoreCase(string)) {
                return state;
            }
        }
        return null;
    }

    public String getStateString() { 
        return stateString;
    }
    
    public int getStateId() {
        return stateId;
    }

    /**
     * Returns the state that matches the specified numerical value.
     */
    public final static Ec2InstanceState parse(int code) {
        code = code & 0x000000ff;
        for (final Ec2InstanceState state : values()) {
            if (state.stateId == code) {
                return state;
            }
        }
        return null;
    }
}