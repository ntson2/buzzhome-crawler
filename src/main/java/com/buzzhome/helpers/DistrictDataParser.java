package com.buzzhome.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistrictDataParser {
    private static final String DISTRICT_LIST_N = "(1|2|4|5|6|7|8|9|10|11|12)";
    private static final String DISTRICT_LIST_S = "(binh thanh|thu duc|go vap|phu nhuan|tan binh|tan phu|binh tan)";
    private static final String NON_WORD = "[^A-Za-z0-9_]";
    private static final String DISTRICT_REGEX_VIETNAMESE_N = NON_WORD + "quan\\s+" + DISTRICT_LIST_N + NON_WORD;
    private static final String DISTRICT_REGEX_VIETNAMESE_S = NON_WORD + "quan\\s+" + DISTRICT_LIST_S + NON_WORD;
    private static final String DISTRICT_REGEX_ENGLISH_N = NON_WORD + "district\\s+" + DISTRICT_LIST_N + NON_WORD;
    private static final String DISTRICT_REGEX_ENGLISH_S = NON_WORD + DISTRICT_LIST_S + "\\s+district" + NON_WORD;

    private static final List<String> DISTRICT_REGEX = Arrays.asList(DISTRICT_REGEX_VIETNAMESE_N,
            DISTRICT_REGEX_VIETNAMESE_S, DISTRICT_REGEX_ENGLISH_N, DISTRICT_REGEX_ENGLISH_S);

    public static Optional<String> getDistrict(String content) {
        content = VNCharacterUtils.removeAccent(content).toLowerCase();

        Optional<String> result = Optional.empty();
        int startIndex = 0;

        for (String regex : DISTRICT_REGEX) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                int start = matcher.start();

                if (!result.isPresent() || start < startIndex) {
                    startIndex = start;
                    result = Optional.of(matcher.group(1));
                }
            }
        }

        return result;
    }
}
