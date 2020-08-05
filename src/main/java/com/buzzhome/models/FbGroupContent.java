package com.buzzhome.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Setter
@Getter
@DynamoDBTable(tableName="fbgsaigonairbnb")
public class FbGroupContent {

    @DynamoDBHashKey(attributeName = "id")
    String id;

    @DynamoDBRangeKey(attributeName = "postedTimestamp")
    long postedTimestamp;

    @DynamoDBAttribute(attributeName = "savedTimestamp")
    long savedTimestamp;

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

    @DynamoDBAttribute(attributeName = "postedTimeString")
    String postedTimeString;

    @DynamoDBAttribute(attributeName = "link")
    String link;

    @DynamoDBAttribute(attributeName = "photos")
    List<String> photos;

    @DynamoDBAttribute(attributeName = "price")
    Double price;

    @DynamoDBAttribute(attributeName = "districtLocation")
    String districtLocation;

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

    public FbGroupContent() {
        // Deserialization
    }
}