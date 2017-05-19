package my.demo.news;

import android.os.Parcel;

import my.demo.news.news.IMediaEntity;

/**
 * This class represents a media item
 */
public class MediaEntity implements IMediaEntity {

    private String url;
    private String format;
    private int height;
    private int width;
    private String type;
    private String subType;
    private String caption;
    private String copyright;

    public MediaEntity(IMediaEntity other) {
        url = other.getUrl();
        format = other.getFormat();
        width = other.getWidth();
        height = other.getHeight();
        type = other.getType();
        subType = other.getSubType();
        caption = other.getCaption();
        copyright = other.getCopyright();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.format);
        dest.writeInt(this.height);
        dest.writeInt(this.width);
        dest.writeString(this.type);
        dest.writeString(this.subType);
        dest.writeString(this.caption);
        dest.writeString(this.copyright);
    }

    @Override
    public String getUrl() {
        return ensure(url);
    }

    @Override
    public String getFormat() {
        return ensure(format);
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
        return ensure(type);
    }

    @Override
    public String getSubType() {
        return ensure(subType);
    }

    @Override
    public String getCaption() {
        return ensure(caption);
    }

    @Override
    public String getCopyright() {
        return ensure(copyright);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaEntity that = (MediaEntity) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (subType != null ? !subType.equals(that.subType) : that.subType != null) return false;
        if (caption != null ? !caption.equals(that.caption) : that.caption != null) return false;
        return copyright != null ? copyright.equals(that.copyright) : that.copyright == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (subType != null ? subType.hashCode() : 0);
        result = 31 * result + (caption != null ? caption.hashCode() : 0);
        result = 31 * result + (copyright != null ? copyright.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MediaEntity{" +
               "url='" + url + '\'' +
               ", format='" + format + '\'' +
               ", height=" + height +
               ", width=" + width +
               '}';
    }

    public static final Creator<MediaEntity> CREATOR = new Creator<MediaEntity>() {
        @Override
        public MediaEntity createFromParcel(Parcel source) {
            return new MediaEntity(source);
        }

        @Override
        public MediaEntity[] newArray(int size) {
            return new MediaEntity[size];
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private MediaEntity(Parcel in) {
        this.url = in.readString();
        this.format = in.readString();
        this.height = in.readInt();
        this.width = in.readInt();
        this.type = in.readString();
        this.subType = in.readString();
        this.caption = in.readString();
        this.copyright = in.readString();
    }

    private String ensure(final String string) {
        return string == null ? "" : string;
    }
}
