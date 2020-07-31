package com.blackboard.testing.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.blackboard.testing.models.Checkpoint;
import com.blackboard.testing.models.FbGroupContent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamodbClient {

    private static final String CHECKPOINT_KEY = "checkpoint";

    public static void insert(FbGroupContent fbGroupContent) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        mapper.save(fbGroupContent);
    }

    public static void saveCheckpoint(long checkpoint) {
        Checkpoint object = new Checkpoint();
        object.setKey(CHECKPOINT_KEY);
        object.setValue(checkpoint);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        mapper.save(object);
    }

    public static Checkpoint getCheckpoint() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Checkpoint result = mapper.load(Checkpoint.class, CHECKPOINT_KEY);

        if (result == null) {
            log.error("There is no checkpoint, using default");
            Checkpoint object = new Checkpoint();
            object.setKey(CHECKPOINT_KEY);
            object.setValue(0);
            return object;
        }

        log.info("Getting checkpoint {}", result);
        return result;
    }
}
