package my.demo.news;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import my.demo.news.news.INewsContract;
import my.demo.news.news.NewsJsonTranslator;

public class JsonTranslationTest {

    private INewsContract.JsonTranslator mJsonTranslator;

    @Before
    public void setupService() {
        MockitoAnnotations.initMocks(this);

        // Init the JSON translator.
        mJsonTranslator = new NewsJsonTranslator();
    }

    @Test
    public void testTranslation1() throws Exception {
        // Make sure to get two entities.
        Assert.assertTrue(
            mJsonTranslator
                .translateEntity(RESPONSE_JSON_WITH_MULTIMEDIA)
                .blockingGet()
                .size() == 2);

        // Make sure the "multimedia" field has value.
        Assert.assertTrue(
            mJsonTranslator
                .translateEntity(RESPONSE_JSON_WITH_MULTIMEDIA)
                .blockingGet()
                .get(0)
                .getMediaEntity()
                .size() > 0);
    }

    @Test
    public void testTranslation2() throws Exception {
        // Make sure to get one entities.
        Assert.assertTrue(
            mJsonTranslator
                .translateEntity(RESPONSE_JSON_WITHOUT_MULTIMEDIA)
                .blockingGet()
                .size() == 1);

        // Make sure the "multimedia" field is empty.
        Assert.assertTrue(
            mJsonTranslator
                .translateEntity(RESPONSE_JSON_WITHOUT_MULTIMEDIA)
                .blockingGet()
                .get(0)
                .getMediaEntity()
                .size() == 0);
    }

    @Test
    public void testTranslation3() throws Exception {
        // Intentionally remove the filed, "summary" to see if it crashes.
        Assert.assertTrue(
            mJsonTranslator
                .translateEntity(RESPONSE_JSON_WITHOUT_SUMMARY)
                .blockingGet()
                .get(0)
                .getSummary()
                .length() == 0);
    }

    private static final String RESPONSE_JSON_WITH_MULTIMEDIA =
        "{\"status\":\"OK\",\"copyright\":\"Copyright (c) 2015 The New York Times Company. All Rights Reserved.\",\"section\":\"technology\",\"last_updated\":\"2015-08-18T10:15:06-05:00\",\"num_results\":24,\"results\":[{\"section\":\"Business Day\",\"subsection\":\"\",\"title\":\"Work Policies May Be Kinder, but Brutal Competition Isnâ€™t\",\"abstract\":\"Top-tier employers may be changing their official policies in a nod to work-life balance, but brutal competition remains an inescapable component of workersâ€™ daily lives.\",\"url\":\"http://www.nytimes.com/2015/08/18/business/work-policies-may-be-kinder-but-brutal-competition-isnt.html\",\"byline\":\"By NOAM SCHEIBER\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-17T22:10:02-5:00\",\"created_date\":\"2015-08-17T22:10:04-5:00\",\"published_date\":\"2015-08-18T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\",\"Executives and Management (Theory)\",\"Paid Time Off\",\"Legal Profession\",\"Banking and Financial Institutions\",\"Computers and the Internet\"],\"org_facet\":[\"Amazon.com Inc\",\"Cravath Swaine & Moore\"],\"per_facet\":[\"Bezos, Jeffrey P\"],\"geo_facet\":[\"Silicon Valley (Calif)\"],\"multimedia\":[{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-thumbStandard.jpg\",\"format\":\"Standard Thumbnail\",\"height\":75,\"width\":75,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-thumbLarge.jpg\",\"format\":\"thumbLarge\",\"height\":150,\"width\":150,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-articleInline.jpg\",\"format\":\"Normal\",\"height\":127,\"width\":190,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-mediumThreeByTwo210.jpg\",\"format\":\"mediumThreeByTwo210\",\"height\":140,\"width\":210,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"}]},{\"section\":\"Technology\",\"subsection\":\"\",\"title\":\"Depiction of Amazon Stirs a Debate About Work Culture\",\"abstract\":\"Details of working conditions at Amazon led to a response from employees, relatives and friends.\",\"url\":\"http://www.nytimes.com/2015/08/19/technology/amazon-workplace-reactions-comments.html\",\"byline\":\"By THE NEW YORK TIMES\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-18T07:35:33-5:00\",\"created_date\":\"2015-08-18T07:35:35-5:00\",\"published_date\":\"2015-08-19T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\"],\"org_facet\":[\"Amazon.com Inc\"],\"per_facet\":\"\",\"geo_facet\":\"\",\"multimedia\":[{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-thumbStandard.jpg\",\"format\":\"Standard Thumbnail\",\"height\":75,\"width\":75,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-thumbLarge.jpg\",\"format\":\"thumbLarge\",\"height\":150,\"width\":150,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-articleInline.jpg\",\"format\":\"Normal\",\"height\":127,\"width\":190,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-mediumThreeByTwo210.jpg\",\"format\":\"mediumThreeByTwo210\",\"height\":140,\"width\":210,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"}]}]}";

    private static final String RESPONSE_JSON_WITHOUT_MULTIMEDIA =
        "{\"status\":\"OK\",\"copyright\":\"Copyright (c) 2015 The New York Times Company. All Rights Reserved.\",\"section\":\"technology\",\"last_updated\":\"2015-08-18T10:15:06-05:00\",\"num_results\":24,\"results\":[{\"section\":\"Business Day\",\"subsection\":\"\",\"title\":\"Work Policies May Be Kinder, but Brutal Competition Isnâ€™t\",\"abstract\":\"Top-tier employers may be changing their official policies in a nod to work-life balance, but brutal competition remains an inescapable component of workersâ€™ daily lives.\",\"url\":\"http://www.nytimes.com/2015/08/18/business/work-policies-may-be-kinder-but-brutal-competition-isnt.html\",\"byline\":\"By NOAM SCHEIBER\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-17T22:10:02-5:00\",\"created_date\":\"2015-08-17T22:10:04-5:00\",\"published_date\":\"2015-08-18T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\",\"Executives and Management (Theory)\",\"Paid Time Off\",\"Legal Profession\",\"Banking and Financial Institutions\",\"Computers and the Internet\"],\"org_facet\":[\"Amazon.com Inc\",\"Cravath Swaine & Moore\"],\"per_facet\":[\"Bezos, Jeffrey P\"],\"geo_facet\":[\"Silicon Valley (Calif)\"],\"multimedia\":\"\"}]}";

    private static final String RESPONSE_JSON_WITHOUT_SUMMARY =
        "{\"status\":\"OK\",\"copyright\":\"Copyright (c) 2015 The New York Times Company. All Rights Reserved.\",\"section\":\"technology\",\"last_updated\":\"2015-08-18T10:15:06-05:00\",\"num_results\":24,\"results\":[{\"section\":\"Business Day\",\"subsection\":\"\",\"title\":\"Work Policies May Be Kinder, but Brutal Competition Isnâ€™t\",\"url\":\"http://www.nytimes.com/2015/08/18/business/work-policies-may-be-kinder-but-brutal-competition-isnt.html\",\"byline\":\"By NOAM SCHEIBER\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-17T22:10:02-5:00\",\"created_date\":\"2015-08-17T22:10:04-5:00\",\"published_date\":\"2015-08-18T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\",\"Executives and Management (Theory)\",\"Paid Time Off\",\"Legal Profession\",\"Banking and Financial Institutions\",\"Computers and the Internet\"],\"org_facet\":[\"Amazon.com Inc\",\"Cravath Swaine & Moore\"],\"per_facet\":[\"Bezos, Jeffrey P\"],\"geo_facet\":[\"Silicon Valley (Calif)\"],\"multimedia\":\"\"}]}";
}
