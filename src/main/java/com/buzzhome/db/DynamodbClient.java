package com.buzzhome.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.buzzhome.models.Checkpoint;
import com.buzzhome.models.FbGroupContent;
import com.buzzhome.models.GetFbGroupRequest;
import com.buzzhome.models.GetFbGroupResponse;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamodbClient {

    private static final String CHECKPOINT_KEY = "checkpoint";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String GSI_NAME = "gsi_filter";
    private static final int DAYS_TO_LOOK_BACK = 30;
    private static final String HASH_KEY = "fb-post-item";

    public static void insert(FbGroupContent fbGroupContent) {
        fbGroupContent.setId(HASH_KEY);
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

    public static GetFbGroupResponse getFbGroupContent(GetFbGroupRequest request) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Map<String, AttributeValue> attributeValueMap = getAttributeValueMap(request);
        String keyCondition = getKeyCondition(request);

        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize() == null ? DEFAULT_PAGE_SIZE : request.getPageSize();
        int startIndex = pageNum * pageSize;

        if (request.getDistrictLocation() != null) {

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression();

            queryExpression.withIndexName(GSI_NAME)
                    .withKeyConditionExpression(keyCondition)
                    .withExpressionAttributeValues(attributeValueMap)
                    .withFilterExpression("postedTimestamp > :minTimestamp");
            queryExpression.setConsistentRead(false); // Consistent reads are not supported on global secondary indexes

            PaginatedQueryList<FbGroupContent> result = mapper.query(FbGroupContent.class, queryExpression);

            List<FbGroupContent> copied = new ArrayList(result);
            copied.sort((o1, o2) -> Long.compare(o2.getPostedTimestamp(), o1.getPostedTimestamp()));

            if (startIndex >= copied.size()) {
                return GetFbGroupResponse.builder().numResults(copied.size()).results(Collections.emptyList()).build();
            } else {
                return GetFbGroupResponse.builder().numResults(copied.size())
                        .results(copied.subList(startIndex, Math.min(copied.size(), (pageNum + 1) * pageSize)))
                        .build();
            }
        } else {
            attributeValueMap.put(":hash_key", new AttributeValue(HASH_KEY));

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression();
            queryExpression.withKeyConditionExpression("id = :hash_key and postedTimestamp > :minTimestamp ")
                    .withExpressionAttributeValues(attributeValueMap)
                    .setScanIndexForward(false);

            if (!keyCondition.isEmpty()) {
                queryExpression.withFilterExpression(keyCondition);
            }

            PaginatedQueryList<FbGroupContent> result = mapper.query(FbGroupContent.class, queryExpression);

            if (startIndex >= result.size()) {
                return GetFbGroupResponse.builder().numResults(result.size()).results(Collections.emptyList()).build();
            } else {
                return GetFbGroupResponse.builder().numResults(result.size())
                        .results(result.subList(startIndex, Math.min(result.size(), (pageNum + 1) * pageSize)))
                        .build();
            }
        }
    }

    private static Map<String, AttributeValue> getAttributeValueMap(GetFbGroupRequest request) {
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();

        if (request.getDistrictLocation() != null) {
            attributeValueMap.put(":districtLocation", new AttributeValue(request.getDistrictLocation()));
        }
        if (request.getPriceMin() != null) {
            attributeValueMap.put(":priceMin", new AttributeValue().withN(request.getPriceMin().toString()));
        }
        if (request.getPriceMax() != null) {
            attributeValueMap.put(":priceMax", new AttributeValue().withN(request.getPriceMax().toString()));
        }
        DateTime timeToLookBack = new DateTime().minusDays(DAYS_TO_LOOK_BACK);
        attributeValueMap.put(":minTimestamp",
                new AttributeValue().withN(Long.toString(timeToLookBack.getMillis() / 1000)));
        return attributeValueMap;
    }

    private static String getKeyCondition(GetFbGroupRequest request) {
        String result = StringUtils.EMPTY;
        if (request.getDistrictLocation() != null) {
            result = "districtLocation = :districtLocation";
        }

        if (request.getPriceMin() != null && request.getPriceMax() != null) {
            if (!result.equals(StringUtils.EMPTY)) {
                result = result + " and ";
            }
            result = result + "price between :priceMin and :priceMax";
        } else {
            if (request.getPriceMin() != null) {
                if (!result.equals(StringUtils.EMPTY)) {
                    result = result + " and ";
                }
                result = result + "price >= :priceMin";
            }
            if (request.getPriceMax() != null) {
                if (!result.equals(StringUtils.EMPTY)) {
                    result = result + " and ";
                }
                result = result + "price <= :priceMax";
            }
        }
        return result;
    }

}
