package so.brendan.robust.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

/**
 * A parcelable model for holding Robust user data.
 */
@Parcel
public class RobustUser {
    private String id;
    private String name;
    private String handle;
    private String bio;
    private String location;
    private int timezone;
    @SerializedName("twitter_uid") private String twitterUid;
    @SerializedName("display_picture") private String displayPictureURL;
    @SerializedName("display_picture_large") private String largeDisplayPictureURL;
    private List<String> channels;

    RobustUser() {}

    public List<String> getChannels() {
        return channels;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public String getId() { return id; }

    public String getDisplayPictureURL() {
        return displayPictureURL;
    }

    public String getLargeDisplayPictureURL() {
        return largeDisplayPictureURL;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public String getHandle() {
        return handle;
    }
}
