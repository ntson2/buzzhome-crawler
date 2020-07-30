package com.blackboard.testing.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.blackboard.testing.models.FbGroupContent;

public class DynamodbClient {

    public static void insert(FbGroupContent fbGroupContent) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        mapper.save(fbGroupContent);
    }
}
