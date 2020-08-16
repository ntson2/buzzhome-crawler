package com.buzzhome.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Setter
@Getter
@Builder
@DynamoDBDocument
public class Comment {

    String commenter;
    String content;
}
