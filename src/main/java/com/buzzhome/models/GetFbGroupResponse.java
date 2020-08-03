package com.buzzhome.models;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;
import org.junit.runner.Result;

@Value
@Builder
public class GetFbGroupResponse {

    int numResults;
    List<FbGroupContent> results;
}