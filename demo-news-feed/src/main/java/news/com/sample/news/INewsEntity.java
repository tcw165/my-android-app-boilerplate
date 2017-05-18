package news.com.sample.news;

import android.os.Parcelable;

import java.util.List;

public interface INewsEntity extends Parcelable {

    String getTitle();

    String getSummary();

    String getArticleUrl();

    String getByline();

    String getPublishedDate();

    List<IMediaEntity> getMediaEntity();
}
