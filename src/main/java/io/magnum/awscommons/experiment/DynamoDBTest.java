package io.magnum.awscommons.experiment;

import io.magnum.awscommons.AwsContext;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

public class DynamoDBTest {

	public static void main(String[] args) {
		AwsContext awsContext = AwsContext.createAwsContextFromFile(
                "us-east-1", "test-resources/AWSCredentials.properties");
		AmazonDynamoDB db = awsContext.getDynamoDBClient();
		System.out.println(db.listTables());
	}
}
