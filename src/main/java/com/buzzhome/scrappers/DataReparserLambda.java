package com.buzzhome.scrappers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.buzzhome.db.DynamodbClient;
import com.buzzhome.helpers.DistrictDataParser;
import com.buzzhome.helpers.PriceDataParser;
import com.buzzhome.models.FbGroupContent;
import com.buzzhome.models.IntegerRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DataReparserLambda implements RequestHandler<IntegerRequest, String> {

    private static final double EPSILON = 0.00001;

    private boolean compareDouble(double first, double second) {
        return Math.abs(first - second) < EPSILON;
    }

    public String handleRequest(IntegerRequest request, Context context) {
        log.info("DataReparserLamba getting request to reparse data in the last {} days", request.getValue());
        List<FbGroupContent> fbGroupContent = DynamodbClient.getPosts(request.getValue());

        for (FbGroupContent post : fbGroupContent) {

            Optional<String> districtLocation = DistrictDataParser.getDistrict(post.getContent());
            double price = PriceDataParser.getPriceInUSD(post.getContent());

            if ((!districtLocation.isPresent() && post.getDistrictLocation() != null)
                    || (districtLocation.isPresent() && !districtLocation.get().equals(post.getDistrictLocation()))
                    || !compareDouble(price, post.getPrice())) {
                if (districtLocation.isPresent()) {
                    post.setDistrictLocation(districtLocation.get());
                } else {
                    post.setDistrictLocation(null);
                }
                post.setPrice(price);

                log.info("Updating record with hashkey={}, rangekey={}: districtLocation={}, price={}",
                        post.getId(), post.getPostedTimestamp(), districtLocation, price);
                DynamodbClient.update(post);
            }
        }

        return "success";
    }
}
