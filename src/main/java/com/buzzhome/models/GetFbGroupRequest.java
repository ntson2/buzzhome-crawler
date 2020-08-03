package com.buzzhome.models;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetFbGroupRequest {

    int pageNum;

    FilterRequest filter;
}
