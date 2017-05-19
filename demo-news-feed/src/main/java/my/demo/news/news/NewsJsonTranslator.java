package my.demo.news.news;

import android.os.Parcel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import my.demo.news.NewsEntity;

public class NewsJsonTranslator implements INewsContract.JsonTranslator {

    @Override
    public Single<List<INewsEntity>> translateEntity(final String json) {
        return Single
            .fromCallable(new Callable<List<INewsEntity>>() {
                @Override
                public List<INewsEntity> call() throws Exception {

                    // Register a special deserializer for handling fields that
                    // sometime is an array or a string.
                    final GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(NewsJson.class, new MyDeserializer());

                    final ResponseJson responseJson = builder
                        .create()
                        .fromJson(json, ResponseJson.class);
                    // Convert response JSON to our data structure.
                    final List<INewsEntity> entities = new ArrayList<>();
                    for (NewsJson entity : responseJson.newsEntities) {
                        entities.add(new NewsEntity(entity));
                    }

                    return entities;
                }
            })
            .subscribeOn(Schedulers.io());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private static class MyDeserializer implements JsonDeserializer<NewsJson> {

        @Override
        public NewsJson deserialize(JsonElement json,
                                    Type typeOfT,
                                    JsonDeserializationContext context)
            throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            Gson gson = new Gson();
            NewsJson data = gson.fromJson(json, NewsJson.class);

            // Special case for field, "multimedia".
            if (object.get("multimedia").isJsonArray()) {
                data.mediaArray = gson.fromJson(
                    object.get("multimedia"),
                    new TypeToken<List<MediaJson>>() {
                    }.getType());
            } else {
                data.mediaArray = Collections.emptyList();
            }

            return data;
        }
    }

    /**
     * JSON POJO.
     */
    private static class ResponseJson {
        @SerializedName("results")
        List<NewsJson> newsEntities;
    }

    /**
     * Inner JSON POJO which is also recognized as an {@link INewsEntity}
     * instance.
     */
    private static class NewsJson implements INewsEntity {

        @SerializedName("title")
        String title;

        @SerializedName("abstract")
        String summary;

        @SerializedName("url")
        String articleUrl;

        @SerializedName("byline")
        String byline;

        @SerializedName("published_date")
        String publishedDate;

        List<IMediaEntity> mediaArray;

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getSummary() {
            return summary;
        }

        @Override
        public String getArticleUrl() {
            return articleUrl;
        }

        @Override
        public String getByline() {
            return byline;
        }

        @Override
        public String getPublishedDate() {
            return publishedDate;
        }

        @Override
        public List<IMediaEntity> getMediaEntity() {
            return mediaArray;
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
            dest.writeTypedList(this.mediaArray);
        }

        public static final Creator<NewsJson> CREATOR = new Creator<NewsJson>() {
            @Override
            public NewsJson createFromParcel(Parcel source) {
                return new NewsJson(source);
            }

            @Override
            public NewsJson[] newArray(int size) {
                return new NewsJson[size];
            }
        };

        private NewsJson(Parcel in) {
            this.title = in.readString();
            this.summary = in.readString();
            this.articleUrl = in.readString();
            this.byline = in.readString();
            this.publishedDate = in.readString();

            this.mediaArray = new ArrayList<>();
            for (IMediaEntity mediaEntity : in.createTypedArrayList(MediaJson.CREATOR)) {
                this.mediaArray.add(mediaEntity);
            }
        }
    }

    /**
     * Inner JSON POJO which is also recognized as an {@link IMediaEntity}
     * instance.
     */
    private static class MediaJson implements IMediaEntity {

        @SerializedName("url")
        String url;

        @SerializedName("format")
        String format;

        @SerializedName("width")
        int width;

        @SerializedName("height")
        int height;

        @SerializedName("type")
        String type;

        @SerializedName("subtype")
        String subtype;

        @SerializedName("caption")
        String caption;

        @SerializedName("copyright")
        String copyright;

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getSubType() {
            return subtype;
        }

        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public String getCopyright() {
            return copyright;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeString(this.format);
            dest.writeInt(this.width);
            dest.writeInt(this.height);
            dest.writeString(this.type);
            dest.writeString(this.subtype);
            dest.writeString(this.caption);
            dest.writeString(this.copyright);
        }

        public static final Creator<MediaJson> CREATOR = new Creator<MediaJson>() {
            @Override
            public MediaJson createFromParcel(Parcel source) {
                return new MediaJson(source);
            }

            @Override
            public MediaJson[] newArray(int size) {
                return new MediaJson[size];
            }
        };

        MediaJson(Parcel in) {
            this.url = in.readString();
            this.format = in.readString();
            this.width = in.readInt();
            this.height = in.readInt();
            this.type = in.readString();
            this.subtype = in.readString();
            this.caption = in.readString();
            this.copyright = in.readString();
        }
    }
}
