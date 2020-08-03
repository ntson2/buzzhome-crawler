package com.buzzhome.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.buzzhome.models.Checkpoint;
import com.buzzhome.models.FbGroupContent;
import com.buzzhome.models.FilterRequest;
import com.buzzhome.models.GetFbGroupRequest;
import com.buzzhome.models.GetFbGroupResponse;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DynamodbClient {

    private static final String CHECKPOINT_KEY = "checkpoint";
    private static final int MAX_GET_RESULTS = 1000;
    private static final int PAGE_SIZE = 10;
    private static final String GSI_NAME = "gsi_filter";

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

    public static GetFbGroupResponse getFbGroupContent(GetFbGroupRequest request) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        FilterRequest filter = request.getFilter();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Map<String, AttributeValue> attributeValueMap = getAttributeValueMap(filter);
        String keyCondition = getKeyCondition(filter);

        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression();
        queryExpression.withKeyConditionExpression("Id = :val1 and ReplyDateTime > :val2")
                .withExpressionAttributeValues(attributeValueMap)
                .withIndexName(GSI_NAME)
                .withLimit(MAX_GET_RESULTS)
                .setScanIndexForward(false);

        PaginatedQueryList<FbGroupContent> result = mapper.query(FbGroupContent.class, queryExpression);
        int pageNum = request.getPageNum();

        return GetFbGroupResponse.builder().numResults(result.size())
                .results(result.subList(pageNum * PAGE_SIZE, (pageNum + 1) * PAGE_SIZE))
                .build();
    }

    private static Map<String, AttributeValue> getAttributeValueMap(FilterRequest filter) {
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();

        attributeValueMap.put("districtLocation", new AttributeValue(filter.getDistrictLocation()));
        attributeValueMap.put("priceMin", new AttributeValue().withN(filter.getPriceMin().toString()));
        attributeValueMap.put("priceMax", new AttributeValue().withN(filter.getPriceMax().toString()));

        return attributeValueMap;
    }

    private static String getKeyCondition(FilterRequest filter) {
        String result = StringUtils.EMPTY;
        if (filter.getDistrictLocation() != null) {
            result = result + "districtLocation = :districtLocation";
        }
        if (filter.getPriceMin() != null) {
            if (!result.equals(StringUtils.EMPTY)) {
                result = result + " and ";
            }
            result = result + "price >= :priceMin";
        }
        if (filter.getPriceMax() != null) {
            if (!result.equals(StringUtils.EMPTY)) {
                result = result + " and ";
            }
            result = result + "price >= :priceMax";
        }
        return result;
    }

}
