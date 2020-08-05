package com.buzzhome.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Setter
@Getter
public class FbPage {
    String text;
    String link;

    public FbPage() {
        // Deserialization
    }
}
