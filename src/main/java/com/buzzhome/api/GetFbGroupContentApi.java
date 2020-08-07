package com.buzzhome.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.buzzhome.db.DynamodbClient;
import com.buzzhome.models.FbGroupContent;
import com.buzzhome.models.GetFbGroupRequest;
import com.buzzhome.models.GetFbGroupResponse;
import com.buzzhome.models.LambdaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetFbGroupContentApi implements RequestHandler<LambdaRequest, GetFbGroupResponse> {


    @Override
    public GetFbGroupResponse handleRequest(LambdaRequest lambdaRequest, Context context) {
        log.info("Getting request: {}", lambdaRequest);

        return DynamodbClient.getFbGroupContent(lambdaRequest.getBody());

    }
}
