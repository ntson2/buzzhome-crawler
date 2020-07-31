package com.blackboard.testing.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@DynamoDBTable(tableName="operation")
public class Checkpoint {
    @DynamoDBHashKey(attributeName = "key")
    String key;

    @DynamoDBAttribute(attributeName = "value")
    long value;

    public Checkpoint() {
        // silly method for deserialization
    }
}
