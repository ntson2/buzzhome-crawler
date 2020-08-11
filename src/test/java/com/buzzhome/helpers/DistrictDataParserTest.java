package com.buzzhome.helpers;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DistrictDataParserTest {

    private static final String CONTENT_1 = "<3 PARAGON APARTMENT IN CENTRE OF BINH THANH – BIG DISCOUNT JUST FROM 430 $ (10 MIL) " +
            "FOR STUDIO <3\\nAddress: 178/33B Nguyen Van Thuong Street, Ward 25, Binh Thanh District\\n\\uD83D\\uDC49 Conveniently located: " +
            "Conveniently located, easy to go through District 1, 2 3, Phu Nhuan, Go Vap. It only takes 5 minutes to go to Sai Gon Bridge and " +
            "VINHOMES center Park (VIEW LANDMARK 81)\\n\\uD83D\\uDC49 There are many famous shopping, Supermarket, food service, GYM, and entertainment";
    private static final String RESULT_1 = "binh thanh";

    private static final List<String> TESTED_CONTENT = Arrays.asList(CONTENT_1);
    private static final List<String> EXPECTED_RESULT = Arrays.asList(RESULT_1);

    @Test
    public void runTest() {
        for (int indx = 0; indx <= TESTED_CONTENT.size(); indx++) {
            assertEquals(EXPECTED_RESULT.get(indx), DistrictDataParser.getDistrict(TESTED_CONTENT.get(indx)));
        }
    }
}
