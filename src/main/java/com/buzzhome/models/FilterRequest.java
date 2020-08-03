package com.buzzhome.models;

import lombok.Value;

@Value
public class FilterRequest {
    String districtLocation;
    Double priceMin;
    Double priceMax;
}