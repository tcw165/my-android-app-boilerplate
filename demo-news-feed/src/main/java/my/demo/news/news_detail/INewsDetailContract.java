package my.demo.news.news_detail;

public interface INewsDetailContract {

    public interface View {

        void showFullStoryUi(String url);
    }

    public interface UserInteraction {

        void openFullStory(String url);
    }
}
