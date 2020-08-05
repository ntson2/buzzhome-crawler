package com.buzzhome.models;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;

@ToString
@Getter
@Setter
public class GetFbGroupRequest {

    int pageNum;

    Integer pageSize;

    String districtLocation;
    Double priceMin;
    Double priceMax;

    public GetFbGroupRequest() {
        // for deserialization
    }
}
