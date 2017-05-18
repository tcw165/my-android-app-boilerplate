package news.com.sample;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import news.com.sample.news_detail.INewsDetailContract;
import news.com.sample.news_detail.NewsDetailPresenter;

public class NewsDetailBehaviorTest {

    private static final String URL = "http://www.google.com";

    @Mock
    private INewsDetailContract.View mView;

    private NewsDetailPresenter mPresenter;

    @Before
    public void setupNewsPresenter() {
        MockitoAnnotations.initMocks(this);

        mPresenter = new NewsDetailPresenter(mView);
    }

    @Test
    public void testOpenFullStory() {
        // Given, when ...
        mPresenter.openFullStory(URL);

        // Then
        Mockito.verify(mView).showFullStoryUi(URL);
    }
}
