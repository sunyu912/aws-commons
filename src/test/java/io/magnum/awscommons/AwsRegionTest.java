package io.magnum.awscommons;

import org.junit.Assert;
import org.junit.Test;

public class AwsRegionTest {

    @Test
    public void testParse() {
        Assert.assertSame(AwsRegion.us_east_1, AwsRegion.parse("us-east-1"));
        Assert.assertSame(AwsRegion.us_east_1, AwsRegion.parse("us-easT-1"));
        Assert.assertSame(AwsRegion.us_east_1, AwsRegion.parse("us_eaST_1"));
        Assert.assertSame(AwsRegion.us_east_1, AwsRegion.parse("us-eaSt_1"));

        Assert.assertSame(AwsRegion.us_west_1, AwsRegion.parse("us-west-1"));
        Assert.assertSame(AwsRegion.us_west_1, AwsRegion.parse("us-wesT-1"));
        Assert.assertSame(AwsRegion.us_west_1, AwsRegion.parse("us_weST_1"));
        Assert.assertSame(AwsRegion.us_west_1, AwsRegion.parse("us-weSt_1"));

        Assert.assertSame(AwsRegion.us_west_2, AwsRegion.parse("us-west-2"));
        Assert.assertSame(AwsRegion.us_west_2, AwsRegion.parse("us-wesT-2"));
        Assert.assertSame(AwsRegion.us_west_2, AwsRegion.parse("us_weST_2"));
        Assert.assertSame(AwsRegion.us_west_2, AwsRegion.parse("us-weSt_2"));

        Assert.assertNull(AwsRegion.parse("us-west"));
        Assert.assertNull(AwsRegion.parse("us-east"));
        Assert.assertNull(AwsRegion.parse("us east 1"));
        Assert.assertNull(AwsRegion.parse(""));

        try {
            Assert.assertNull(AwsRegion.parse(null));
            Assert.fail("should have died on null input");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}
