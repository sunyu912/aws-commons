package io.magnum.awscommons.experiment;

import io.magnum.awscommons.AwsContext;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;

/**
 * A simple class to test the AwsContext.
 *
 * @author Yu Sun
 */
public class AwsContextExperiment {

    public static void main(String[] args) {
        // AWSCredentials.properties must be filled with the right keys
        AwsContext awsContext = AwsContext.createAwsContextFromFile(
                "us-east-1", "test-resources/AWSCredentials.properties");

        // It outputs the list of EC2 instances in the given region
        DescribeInstancesResult result = awsContext.getEc2Client().describeInstances();
        for (Reservation r : result.getReservations()) {
            System.out.println(r.getInstances());
        }
    }
}
