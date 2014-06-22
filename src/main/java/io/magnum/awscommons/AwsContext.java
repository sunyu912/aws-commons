package io.magnum.awscommons;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

/**
 * A class that provides the key resources and interfaces needed for
 * using AWS services.
 *
 * @author Yu Sun
 */
// I have only added the services I think we are likely to use in
// the short term.  Please feel free to add more here as we evolve.
// And this is the same for AwsRegion, AwsEndpointBinding, and AwsClientFactory.
public class AwsContext {

    /** Our AWS credentials provider */
    private final AWSCredentialsProvider provider;

    /** The region for this context */
    private final AwsRegion region;

    /** The client factory */
    private final AwsClientFactory clientFactory;

    private final AtomicReference<AmazonEC2> ec2Client;
    private final AtomicReference<AmazonCloudWatch> cloudWatchClient;
    private final AtomicReference<AmazonS3> s3Client;
    private final AtomicReference<AmazonCloudFormation> cloudFormationClient;
    private final AtomicReference<AmazonDynamoDB> dynamoDBClient;
    private final AtomicReference<AmazonSimpleWorkflow> simpleWorkflowClient;
    private final AtomicReference<AmazonSimpleEmailServiceClient> simpleEmailClient;
    private final AtomicReference<AmazonAutoScaling> autoScalingClient;
    private final AtomicReference<AmazonElasticLoadBalancing> elasticLoadBalancingClient;

    /**
     * The factory method to create an AWS context from a static properties
     * file that contains the accessKey and secretKey.
     */
    public static AwsContext createAwsContextFromFile(String region, String awsCredentialsPath) {
        AWSCredentials creds;
        try {
            creds = new PropertiesCredentials(new File(awsCredentialsPath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize AWS credentials: " + e.getMessage(), e);
        }
        return new AwsContext(AwsRegion.parse(region), creds);
    }
    
    /**
     * The factory method to create an AWS context from a an
     * accesskey and secretkey
     */
    public static AwsContext createAwsContext(String region, String accesskey, String secretkey) {
        AWSCredentials creds;
        try {
            creds = new BasicAWSCredentials(accesskey, secretkey);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize AWS credentials: " + e.getMessage(), e);
        }
        return new AwsContext(AwsRegion.parse(region), creds);
    }

    private AwsContext(AwsRegion region, AWSCredentials credentials) {
        this(region, new StaticCredentialsProvider(credentials));
    }

    private AwsContext(final AwsRegion region, final AWSCredentialsProvider provider) {
        this.region = region;
        this.provider = provider;
        this.clientFactory = new AwsClientFactory(region, provider);
        this.cloudFormationClient = new AtomicReference<AmazonCloudFormation>();
        this.ec2Client = new AtomicReference<AmazonEC2>();
        this.s3Client = new AtomicReference<AmazonS3>();
        this.dynamoDBClient = new AtomicReference<AmazonDynamoDB>();
        this.simpleWorkflowClient = new AtomicReference<AmazonSimpleWorkflow>();
        this.cloudWatchClient = new AtomicReference<AmazonCloudWatch>();
        this.simpleEmailClient = new AtomicReference<AmazonSimpleEmailServiceClient>();
        this.autoScalingClient = new AtomicReference<AmazonAutoScaling>();
        this.elasticLoadBalancingClient = new AtomicReference<AmazonElasticLoadBalancing>();
    }

    public AmazonEC2 getEc2Client() {
        AmazonEC2 client = ec2Client.get();
        if (client == null) {
            ec2Client.compareAndSet(null, clientFactory.newEc2Client());
        }
        return ec2Client.get();
    }

    public AmazonS3 getS3Client() {
        AmazonS3 client = s3Client.get();
        if (client == null) {
            s3Client.compareAndSet(null, clientFactory.newS3Client());
        }
        return s3Client.get();
    }
    
    public AmazonSimpleEmailServiceClient getSimpleEmailClient(){
    	AmazonSimpleEmailServiceClient client = simpleEmailClient.get();
    	if(client == null){
    		simpleEmailClient.compareAndSet(null, clientFactory.newSimleEmailClient());
    	}
    	return simpleEmailClient.get();
    }

    public AmazonCloudFormation getCloudFormationClient() {
        AmazonCloudFormation client = cloudFormationClient.get();
        if (client == null) {
            cloudFormationClient.compareAndSet(null, clientFactory.newCloudFormationClient());
        }
        return cloudFormationClient.get();
    }
    
    public AmazonDynamoDB getDynamoDBClient() {
        AmazonDynamoDB client = dynamoDBClient.get();
        if (client == null) {
            dynamoDBClient.compareAndSet(null, clientFactory.newDynamoDBClient());
        }
        return dynamoDBClient.get();
    }

    public AmazonSimpleWorkflow getSimpleWorkflowClient() {
        AmazonSimpleWorkflow client = simpleWorkflowClient.get();
        if (client == null) {
            simpleWorkflowClient.compareAndSet(null, clientFactory.newSimpleWorkflowClient());
        }
        return simpleWorkflowClient.get();
    }
    
    public AmazonCloudWatch getCloudWatchClient() {
        AmazonCloudWatch client = cloudWatchClient.get();
        if (client == null) {
            cloudWatchClient.compareAndSet(null, clientFactory.newCloudWatchClient());
        }
        return cloudWatchClient.get();
    }
    
    public AmazonElasticLoadBalancing getElasticLoadBalancingClient() {
        AmazonElasticLoadBalancing client = elasticLoadBalancingClient.get();
        if (client == null) {
            elasticLoadBalancingClient.compareAndSet(null, clientFactory.newElasticLoadBalancingClient());
        }
        return elasticLoadBalancingClient.get();
    }
    
    public AmazonAutoScaling getAutoScalingClient() {
        AmazonAutoScaling client = autoScalingClient.get();
        if (client == null) {
            autoScalingClient.compareAndSet(null, clientFactory.newAutoScalingClient());
        }
        return autoScalingClient.get();
    }
}
