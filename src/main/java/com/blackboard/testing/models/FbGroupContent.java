package com.blackboard.testing.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Value
@Builder
@DynamoDBTable(tableName="fbsaigonairbnb")
@Slf4j
public class FbGroupContent {

    @DynamoDBHashKey(attributeName = "id")
    String id;

    @DynamoDBRangeKey(attributeName = "timestamp")
    long timestamp;

    @DynamoDBTypeConverted(converter = FbPageConverter.class)
    @DynamoDBAttribute(attributeName = "author")
    FbPage author;

    @DynamoDBTypeConverted(converter = FbPageConverter.class)
    @DynamoDBAttribute(attributeName = "location")
    FbPage location;

    @DynamoDBTypeConverted(converter = FbPageConverter.class)
    @DynamoDBAttribute(attributeName = "tagged")
    FbPage tagged;

    @DynamoDBAttribute(attributeName = "content")
    String content;

    @DynamoDBAttribute(attributeName = "timeString")
    String timeString;

    @DynamoDBAttribute(attributeName = "photos")
    List<String> photos;

    static public class FbPageConverter implements DynamoDBTypeConverter<String, FbPage> {
        @Override
        public String convert(FbPage object) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                log.error("Fail to convert {}", object, e);
                return null;
            }
        }

        @Override
        public FbPage unconvert(String object) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(object, FbPage.class);
            } catch (IOException e) {
                log.error("Fail to unconvert {}", object, e);
                return null;
            }
        }
    }
}