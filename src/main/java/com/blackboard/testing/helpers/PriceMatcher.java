package com.blackboard.testing.helpers;

import lombok.Value;

@Value
public class PriceMatcher {
    String regex;
    double multiplyFactor;
}
