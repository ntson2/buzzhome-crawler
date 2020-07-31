package com.blackboard.testing.lambda;

import lombok.Value;

@Value
public class PriceMatcher {
    String regex;
    double multiplyFactor;
}
