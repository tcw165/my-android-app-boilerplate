package news.com.sample;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.TestScheduler;
import news.com.sample.net.INewsServiceApi;
import news.com.sample.news.INewsContract;
import news.com.sample.news.INewsEntity;
import news.com.sample.news.NewsPresenter;

public class NewsBehaviorTest {

    private static final List<INewsEntity> TEST_ENTITIES = new ArrayList<>();
    static {
        TEST_ENTITIES.add(new NewsEntity());
        TEST_ENTITIES.add(new NewsEntity());
        TEST_ENTITIES.add(new NewsEntity());
    }

    @Mock
    private INewsContract.View mView;

    @Mock
    private INewsContract.JsonTranslator mJsonTranslator;

    @Mock
    private INewsServiceApi mService;

    private TestScheduler mScheduler;
    private NewsPresenter mPresenter;

    @Before
    public void setupNewsPresenter() {
        MockitoAnnotations.initMocks(this);

        mScheduler = new TestScheduler();
        mPresenter = new NewsPresenter(mView, mJsonTranslator, mService,
                                       mScheduler, mScheduler);
    }

    @Test
    public void testLoadValidNewsEntity() {
        // In order to test the presenter, I mock the service and view layers.
        try {
            // Given
            Mockito.when(mService.getNews())
                   .thenReturn(Maybe.just(RESPONSE_JSON));
            Mockito.when(mJsonTranslator.translateEntity(Mockito.any(String.class)))
                   .thenReturn(Single.just(TEST_ENTITIES));

            // When
            mPresenter.getNews();
            mScheduler.advanceTimeBy(1, TimeUnit.SECONDS);

            // Then
            final InOrder inOrder = Mockito.inOrder(mView);
            inOrder.verify(mView).showProgressIndicator(true);
            inOrder.verify(mView).showNews(TEST_ENTITIES);
            inOrder.verify(mView).showProgressIndicator(false);
        } catch (Exception ignored) {
            // There should be any exception.
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testLoadInvalidNewsEntity() {
        // In order to test the presenter, I mock the service and view layers.
        try {
            // Given
            Mockito.when(mService.getNews())
                   .thenReturn(Maybe.<String>error(new RuntimeException("Random fault.")));

            // When
            mPresenter.getNews();
            mScheduler.advanceTimeBy(1, TimeUnit.SECONDS);

            // Then
            final InOrder inOrder = Mockito.inOrder(mView);
            inOrder.verify(mView).showProgressIndicator(true);
            inOrder.verify(mView).showNewsPlaceholder(true);
            inOrder.verify(mView).showProgressIndicator(false);
        } catch (Exception ignored) {
            // There should be any exception.
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testOpenDetail() {
        mPresenter
            .openNewsDetails(TEST_ENTITIES.get(1));

        Mockito.verify(mView).showNewsDetailUi(TEST_ENTITIES.get(1));
    }

    private static final String RESPONSE_JSON =
        "{\"status\":\"OK\",\"copyright\":\"Copyright (c) 2015 The New York Times Company. All Rights Reserved.\",\"section\":\"technology\",\"last_updated\":\"2015-08-18T10:15:06-05:00\",\"num_results\":24,\"results\":[{\"section\":\"Business Day\",\"subsection\":\"\",\"title\":\"Work Policies May Be Kinder, but Brutal Competition Isnâ€™t\",\"abstract\":\"Top-tier employers may be changing their official policies in a nod to work-life balance, but brutal competition remains an inescapable component of workersâ€™ daily lives.\",\"url\":\"http://www.nytimes.com/2015/08/18/business/work-policies-may-be-kinder-but-brutal-competition-isnt.html\",\"byline\":\"By NOAM SCHEIBER\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-17T22:10:02-5:00\",\"created_date\":\"2015-08-17T22:10:04-5:00\",\"published_date\":\"2015-08-18T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\",\"Executives and Management (Theory)\",\"Paid Time Off\",\"Legal Profession\",\"Banking and Financial Institutions\",\"Computers and the Internet\"],\"org_facet\":[\"Amazon.com Inc\",\"Cravath Swaine & Moore\"],\"per_facet\":[\"Bezos, Jeffrey P\"],\"geo_facet\":[\"Silicon Valley (Calif)\"],\"multimedia\":[{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-thumbStandard.jpg\",\"format\":\"Standard Thumbnail\",\"height\":75,\"width\":75,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-thumbLarge.jpg\",\"format\":\"thumbLarge\",\"height\":150,\"width\":150,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-articleInline.jpg\",\"format\":\"Normal\",\"height\":127,\"width\":190,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/18/business/18EMPLOY/18EMPLOY-mediumThreeByTwo210.jpg\",\"format\":\"mediumThreeByTwo210\",\"height\":140,\"width\":210,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"People eating at the Brave Horse Tavern on the Amazon campus in Seattle in June.\",\"copyright\":\"Matthew Ryan Williams for The New York Times\"}]},{\"section\":\"Technology\",\"subsection\":\"\",\"title\":\"Depiction of Amazon Stirs a Debate About Work Culture\",\"abstract\":\"Details of working conditions at Amazon led to a response from employees, relatives and friends.\",\"url\":\"http://www.nytimes.com/2015/08/19/technology/amazon-workplace-reactions-comments.html\",\"byline\":\"By THE NEW YORK TIMES\",\"item_type\":\"Article\",\"updated_date\":\"2015-08-18T07:35:33-5:00\",\"created_date\":\"2015-08-18T07:35:35-5:00\",\"published_date\":\"2015-08-19T04:00:00-5:00\",\"material_type_facet\":\"News\",\"kicker\":\"\",\"des_facet\":[\"Workplace Environment\"],\"org_facet\":[\"Amazon.com Inc\"],\"per_facet\":\"\",\"geo_facet\":\"\",\"multimedia\":[{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-thumbStandard.jpg\",\"format\":\"Standard Thumbnail\",\"height\":75,\"width\":75,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-thumbLarge.jpg\",\"format\":\"thumbLarge\",\"height\":150,\"width\":150,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-articleInline.jpg\",\"format\":\"Normal\",\"height\":127,\"width\":190,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"},{\"url\":\"http://static01.nyt.com/images/2015/08/13/business/13amazon-selects-slide-AUYG/13amazon-selects-slide-AUYG-mediumThreeByTwo210.jpg\",\"format\":\"mediumThreeByTwo210\",\"height\":140,\"width\":210,\"type\":\"image\",\"subtype\":\"photo\",\"caption\":\"Amazon is building new offices in Seattle and, in about three years, will have enough space for about 50,000 employees.\",\"copyright\":\"Ruth Fremson/The New York Times\"}]}]}";
}
