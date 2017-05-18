package news.com.sample;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import news.com.sample.news.IMediaEntity;
import news.com.sample.news.INewsEntity;

/**
 * This represents a news item
 */
public class NewsEntity implements INewsEntity {

    private static final String TAG = NewsEntity.class.getSimpleName();

    private String title;
    private String summary;
    private String articleUrl;
    private String byline;
    private String publishedDate;
    private List<IMediaEntity> mediaEntityList;

    public NewsEntity() {
        // DUMMY CONSTRUCTOR FOR UNIT-TESTS.
    }

    public NewsEntity(String title,
                      String summary,
                      String articleUrl,
                      String byline,
                      String publishedDate) {
        // DUMMY CONSTRUCTOR FOR UNIT-TESTS.
        this.title = title;
        this.summary = summary;
        this.articleUrl = articleUrl;
        this.byline = byline;
        this.publishedDate = publishedDate;
        this.mediaEntityList = Collections.emptyList();
    }

    public NewsEntity(INewsEntity other) {
        title = other.getTitle();
        summary = other.getSummary();
        articleUrl = other.getArticleUrl();
        byline = other.getByline();
        publishedDate = other.getPublishedDate();

        mediaEntityList = new ArrayList<>();
        for (IMediaEntity entity : other.getMediaEntity()) {
            mediaEntityList.add(new MediaEntity(entity));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.summary);
        dest.writeString(this.articleUrl);
        dest.writeString(this.byline);
        dest.writeString(this.publishedDate);
        dest.writeList(this.mediaEntityList);
    }

    @Override
    public String getTitle() {
        return ensure(title);
    }

    @Override
    public String getSummary() {
        return ensure(summary);
    }

    @Override
    public String getArticleUrl() {
        return ensure(articleUrl);
    }

    @Override
    public String getByline() {
        return ensure(byline);
    }

    @Override
    public String getPublishedDate() {
        return ensure(publishedDate);
    }

    @Override
    public List<IMediaEntity> getMediaEntity() {
        return mediaEntityList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsEntity entity = (NewsEntity) o;

        if (title != null ? !title.equals(entity.title) : entity.title != null) return false;
        if (summary != null ? !summary.equals(entity.summary) : entity.summary != null) return false;
        if (articleUrl != null ? !articleUrl.equals(entity.articleUrl) : entity.articleUrl != null) return false;
        if (byline != null ? !byline.equals(entity.byline) : entity.byline != null) return false;
        if (publishedDate != null ? !publishedDate.equals(entity.publishedDate) : entity.publishedDate != null)
            return false;
        return mediaEntityList != null ? mediaEntityList
            .equals(entity.mediaEntityList) : entity.mediaEntityList == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (articleUrl != null ? articleUrl.hashCode() : 0);
        result = 31 * result + (byline != null ? byline.hashCode() : 0);
        result = 31 * result + (publishedDate != null ? publishedDate.hashCode() : 0);
        result = 31 * result + (mediaEntityList != null ? mediaEntityList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NewsEntity{" +
               "title='" + title + '\'' +
               ", publishedDate='" + publishedDate + '\'' +
               '}';
    }

    public static final Creator<NewsEntity> CREATOR = new Creator<NewsEntity>() {
        @Override
        public NewsEntity createFromParcel(Parcel source) {
            return new NewsEntity(source);
        }

        @Override
        public NewsEntity[] newArray(int size) {
            return new NewsEntity[size];
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private NewsEntity(Parcel in) {
        this.title = in.readString();
        this.summary = in.readString();
        this.articleUrl = in.readString();
        this.byline = in.readString();
        this.publishedDate = in.readString();
        this.mediaEntityList = new ArrayList<>();
        in.readList(this.mediaEntityList, IMediaEntity.class.getClassLoader());
    }

    private String ensure(final String string) {
        return string == null ? "" : string;
    }
}
