package com.buzzhome.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.buzzhome.db.DynamodbClient;
import com.buzzhome.models.GetFbGroupRequest;
import com.buzzhome.models.GetFbGroupResponse;

public class GetFbGroupContent implements RequestHandler<GetFbGroupRequest, GetFbGroupResponse> {

    @Override
    public GetFbGroupResponse handleRequest(GetFbGroupRequest getFbGroupRequest, Context context) {
        return DynamodbClient.getFbGroupContent(getFbGroupRequest);
    }
}
