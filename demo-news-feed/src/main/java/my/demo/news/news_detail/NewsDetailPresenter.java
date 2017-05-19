package my.demo.news.news_detail;

public class NewsDetailPresenter implements INewsDetailContract.UserInteraction {

    private final INewsDetailContract.View mView;

    public NewsDetailPresenter(INewsDetailContract.View view) {
        mView = view;
    }

    @Override
    public void openFullStory(String url) {
        mView.showFullStoryUi(url);
    }
}
