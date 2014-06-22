package io.magnum.awscommons;

/**
 * An enumeration of regions in AWS.
 * <p>
 * This enumeration currently only includes the US regions. The full list
 * can be found at <a href="http://aws.amazon.com/about-aws/globalinfrastructure/">
 * http://aws.amazon.com/about-aws/globalinfrastructure/</a>
 *
 * @author Yu Sun
 */
public enum AwsRegion {

    /** Western United States: Northern California */
    us_west_1,

    /** Western United States: Oregon */
    us_west_2,

    /** Eastern United States: Northern Virginia */
    us_east_1;

    /**
     * Parses the textual form of this constant from the specified string.
     * <p>
     * To facilitate the usage of regions, hyphens can be treated as underscores
     * and the name is taken in a case-insensitive way.
     */
    public static AwsRegion parse(String string) {
        string = string.toLowerCase().replace('-', '_');
        for(AwsRegion region : values()) {
            if (region.name().equals(string)) return region;
        }
        return null;
    }
}
