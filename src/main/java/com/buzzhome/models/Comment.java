package com.buzzhome.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Setter
@Getter
@Builder
public class Comment {

    String commenter;
    String content;
}
