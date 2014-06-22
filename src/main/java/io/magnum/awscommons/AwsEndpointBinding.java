package io.magnum.awscommons;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * An enumeration of endpoints we will use.
 * <p>
 * The full list can be found at:
 * <a href='http://docs.amazonwebservices.com/general/latest/gr/index.html?rande.html'>
 * http://docs.amazonwebservices.com/general/latest/gr/index.html?rande.html</a>.
 *
 * @author Yu Sun
 */
@SuppressWarnings("serial")
public enum AwsEndpointBinding {

    /** Cloud formation endpoints */
    CLOUD_FORMATION(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "cloudformation.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "cloudformation.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "cloudformation.us-west-2.amazonaws.com");
    }})),
    
    /** Cloud Watch endpoints */
    CLOUD_WATCH(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "monitoring.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "monitoring.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "monitoring.us-west-2.amazonaws.com");
    }})),

    /** Elastic Load Balancing endpoint */
    ELASTIC_LOAD_BALANCING(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "elasticloadbalancing.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "elasticloadbalancing.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "elasticloadbalancing.us-west-2.amazonaws.com");
    }})),
    
    /** Auto Scaling endpoint */
    AUTO_SCALING(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "autoscaling.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "autoscaling.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "autoscaling.us-west-2.amazonaws.com");
    }})),
    
    /** EC2 endpoints */
    ELASTIC_COMPUTE_CLOUD(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "ec2.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "ec2.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "ec2.us-west-2.amazonaws.com");
    }})),

    /** S3 endpoints */
    SIMPLE_STORAGE_SERVICE(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "s3.amazonaws.com");
        put(AwsRegion.us_west_1, "s3-us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "s3-us-west-2.amazonaws.com");
    }})),
    
    /** DynamoDB endpoints */
    DYNAMO_DB(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "dynamodb.us-east-1.amazonaws.com");
        put(AwsRegion.us_west_1, "dynamodb.us-west-1.amazonaws.com");
        put(AwsRegion.us_west_2, "dynamodb.us-west-2.amazonaws.com");
    }})),
    
    /** Simple Workflow endpoint. Availability: only US-EAST-1. */
    SIMPLE_WORKFLOW(Collections.unmodifiableMap(new EnumMap<AwsRegion,String>(AwsRegion.class){{
        put(AwsRegion.us_east_1, "https://swf.us-east-1.amazonaws.com");
    }}));

    private final Map<AwsRegion, String> endpointAddressByRegion;

    private AwsEndpointBinding(final Map<AwsRegion, String> endpointAddressByRegion) {
        this.endpointAddressByRegion = Collections.unmodifiableMap(endpointAddressByRegion);
    }

    /** Returns the endpoint for the given region */
    public final String getEndpointAddress(final AwsRegion region) {
        final String endpointAddress = endpointAddressByRegion.get(region);
        if (endpointAddress == null) {
            throw new RuntimeException(name() + " is unavailable in region " + region);
        }
        return endpointAddress;
    }
}
