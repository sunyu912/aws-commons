package io.magnum.awscommons;

/**
 * An enumeration of AWS resource type strings.
 * 
 * @author Yu Sun
 */
public enum AwsResourceType {

	EC2_SECURITY_GROUP("AWS::EC2::SecurityGroup"),
    AUTOSCALING_LAUNCH_CONFIGURATION("AWS::AutoScaling::LaunchConfiguration"),
    AUTOSCALING_GROUP("AWS::AutoScaling::AutoScalingGroup"),
    ELASTIC_LOAD_BALANCER("AWS::ElasticLoadBalancing::LoadBalancer");

    private final String typeString;
    
    private AwsResourceType(final String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() { 
        return typeString;
    }
   
    public static AwsResourceType parse(final String typeString) {
        for (final AwsResourceType type : values()) {
            if (type.typeString.equalsIgnoreCase(typeString)) {
                return type;
            }
        }
        return null;
    }
}