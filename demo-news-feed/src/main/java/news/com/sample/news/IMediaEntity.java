package news.com.sample.news;

import android.os.Parcelable;

public interface IMediaEntity extends Parcelable {

    String getUrl();

    String getFormat();

    int getHeight();

    int getWidth();

    String getType();

    String getSubType();

    String getCaption();

    String getCopyright();
}
