package com.blackboard.testing.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistrictDataParser {
    private static final String DISTRICT_LIST_N = "(1|2|4|5|6|7|8|9|10|11|12)";
    private static final String DISTRICT_LIST_S = "(binh thanh|thu duc|go vap|phu nhuan|tan Binh|Tan Phu|Binh Tan)";

    private static final String DISTRICT_REGEX_VIETNAMESE_N = "\\s+quan\\s+" + DISTRICT_LIST_N + "\\s+";
    private static final String DISTRICT_REGEX_VIETNAMESE_S = "\\s+quan\\s+" + DISTRICT_LIST_S + "\\s+";
    private static final String DISTRICT_REGEX_ENGLISH_N = "\\s+district\\s+" + DISTRICT_LIST_N;
    private static final String DISTRICT_REGEX_ENGLISH_S = "\\s+" + DISTRICT_LIST_S + "\\s+district\\s+";

    private static final List<String> DISTRICT_REGEX = Arrays.asList(DISTRICT_REGEX_VIETNAMESE_N,
            DISTRICT_REGEX_VIETNAMESE_S, DISTRICT_REGEX_ENGLISH_N, DISTRICT_REGEX_ENGLISH_S);

    public static String getDistrict(String content) {
        content = VNCharacterUtils.removeAccent(content).toLowerCase();

        for (String regex : DISTRICT_REGEX) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }
}
