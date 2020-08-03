package com.buzzhome.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataParser {

    private static final String NUMBER_VN_FORMAT_REGEX = "([0-9]+[\\.,0-9]*)";

    private static final String PRICE_REGEX_USD_1 = "\\$\\s*([0-9]+)|([0-9]+)\\s*\\$"; // eg: 250$ or $ 467
    private static final String PRICE_REGEX_UDS_2 = "([0-9]+)\\s*usd";
    private static final String PRICE_REGEX_VND_FULL = NUMBER_VN_FORMAT_REGEX + "\\s*vnd";  // eg: 6.000.000 vnd
    private static final String PRICE_REGEX_VND_ABB = NUMBER_VN_FORMAT_REGEX + "\\s*m\\s*\\/\\s*month"; //eg: 18m/ month
    private static final String PRICE_REGEX_VND_MILLION_1 = NUMBER_VN_FORMAT_REGEX + "\\s*tr"; // eg: 7.5tr or     7,5tr
    private static final String PRICE_REGEX_VND_NO_UNIT = "([0-9\\.]+00\\.?000)";  //eg: 7.500.000
    private static final String PRICE_REGEX_VND_MILLION_2 = NUMBER_VN_FORMAT_REGEX + "\\s*m\\/";  //eg: 18 m/

    private static final double A_MILLION = 1000000;
    private static final double USD_VND_RATE = 23190.61;

    private static final List<PriceMatcher> priceMatchers = Arrays.asList(
            new PriceMatcher(PRICE_REGEX_USD_1, 1),
            new PriceMatcher(PRICE_REGEX_UDS_2, 1),
            new PriceMatcher(PRICE_REGEX_VND_FULL, USD_VND_RATE),
            new PriceMatcher(PRICE_REGEX_VND_ABB, USD_VND_RATE/A_MILLION),
            new PriceMatcher(PRICE_REGEX_VND_MILLION_1, USD_VND_RATE/A_MILLION),
            new PriceMatcher(PRICE_REGEX_VND_NO_UNIT, USD_VND_RATE),
            new PriceMatcher(PRICE_REGEX_VND_MILLION_2, USD_VND_RATE/A_MILLION));

 //   private static final
    private static String sanitize(String s) {
        return s.replace(",", "").replace(".","");
    }

    public static double getPriceInUSD(String post) {
        String copied = post.toLowerCase();

        for (PriceMatcher priceMatcher : priceMatchers) {
            Pattern pattern = Pattern.compile(priceMatcher.getRegex());
            Matcher matcher = pattern.matcher(copied);
            if (matcher.find()) {
                String number = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);

                return Double.parseDouble(sanitize(number)) / priceMatcher.getMultiplyFactor();
            }
        }

        return -1;
    }
}
