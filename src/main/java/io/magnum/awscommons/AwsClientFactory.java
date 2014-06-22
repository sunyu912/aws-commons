package io.magnum.awscommons;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

/**
 * A factory class that returns AWS service clients we will use.
 * <p>
 * This class is NOT thread-safe.
 *
 * @author Yu Sun
 */
public class AwsClientFactory {

    /** AWS credentials provider */
    private final AWSCredentialsProvider provider;

    /** Configured region */
    private final AwsRegion region;

    /** Creates a new client factory for the specified region with the given credentials */
    public AwsClientFactory(final AwsRegion region, final AWSCredentialsProvider provider) {
        if (region == null) {
            throw new IllegalArgumentException("AWS region must not be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("AWS credentail provider must not be null");
        }
        this.provider = provider;
        this.region = region;
    }

    public AmazonEC2 newEc2Client() {
        final AmazonEC2 client = new AmazonEC2Client(provider);
        client.setEndpoint(AwsEndpointBinding.ELASTIC_COMPUTE_CLOUD.getEndpointAddress(region));
        return client;
    }
    
    public AmazonSimpleEmailServiceClient newSimleEmailClient(){
    	final AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(provider);
    	return client;
    }

    public AmazonS3 newS3Client() {
        final AmazonS3 client = new AmazonS3Client(provider);
        client.setEndpoint(AwsEndpointBinding.SIMPLE_STORAGE_SERVICE.getEndpointAddress(region));
        return client;
    }

    public AmazonCloudFormation newCloudFormationClient() {
        final AmazonCloudFormation client = new AmazonCloudFormationClient(provider);
        client.setEndpoint(AwsEndpointBinding.CLOUD_FORMATION.getEndpointAddress(region));
        return client;
    }
    
    public AmazonDynamoDB newDynamoDBClient() {
        final AmazonDynamoDB client = new AmazonDynamoDBClient(provider);
        client.setEndpoint(AwsEndpointBinding.DYNAMO_DB.getEndpointAddress(region));
        return client;
    }
    
    public AmazonCloudWatch newCloudWatchClient() {
        final AmazonCloudWatch client = new AmazonCloudWatchClient(provider);
        client.setEndpoint(AwsEndpointBinding.CLOUD_WATCH.getEndpointAddress(region));
        return client;
    }
    
    public AmazonSimpleWorkflow newSimpleWorkflowClient() {
        final AmazonSimpleWorkflow client = new AmazonSimpleWorkflowClient(provider);
        client.setEndpoint(AwsEndpointBinding.SIMPLE_WORKFLOW.getEndpointAddress(region));
        return client;
    }
    
    public AmazonElasticLoadBalancing newElasticLoadBalancingClient() {
        final AmazonElasticLoadBalancing client = new AmazonElasticLoadBalancingClient(provider);
        client.setEndpoint(AwsEndpointBinding.ELASTIC_LOAD_BALANCING.getEndpointAddress(region));
        return client;
    }
    
    public AmazonAutoScaling newAutoScalingClient() {
        final AmazonAutoScaling client = new AmazonAutoScalingClient(provider);
        client.setEndpoint(AwsEndpointBinding.AUTO_SCALING.getEndpointAddress(region));
        return client;
    }
}
