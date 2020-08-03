package com.buzzhome.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FbPage {
    String text;
    String link;
}
