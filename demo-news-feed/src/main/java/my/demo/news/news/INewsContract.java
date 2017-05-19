package my.demo.news.news;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public interface INewsContract {

    /**
     * Represents a news UI.
     */
    interface View {

        void showNews(List<INewsEntity> entities);

        void showNewsPlaceholder(boolean value);

        void showNewsDetailUi(INewsEntity entity);

        void showProgressIndicator(boolean value);
    }

    /**
     * Any kinds of user interaction.
     */
    interface UserInteraction {

        Disposable getNews();

        void openNewsDetails(INewsEntity entity);
    }

    /**
     * Translate JSON for news feed.
     */
    interface JsonTranslator {
        /**
         * Translate the JSON to a list of {@link INewsEntity}.
         *
         * @param json The response JSON.
         * @return {@link Observable} of entity list.
         */
        Single<List<INewsEntity>> translateEntity(final String json);
    }
}
