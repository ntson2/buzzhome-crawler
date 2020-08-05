package com.buzzhome.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.buzzhome.db.DynamodbClient;
import com.buzzhome.models.GetFbGroupRequest;
import com.buzzhome.models.GetFbGroupResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetFbGroupContentApi implements RequestHandler<GetFbGroupRequest, GetFbGroupResponse> {

    @Override
    public GetFbGroupResponse handleRequest(GetFbGroupRequest getFbGroupRequest, Context context) {
        log.info("Getting request: {}", getFbGroupRequest);
        return DynamodbClient.getFbGroupContent(getFbGroupRequest);
    }
}
