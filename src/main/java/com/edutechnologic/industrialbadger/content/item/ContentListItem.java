package com.edutechnologic.industrialbadger.content.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by H.D. "Chip" McCullough on 12/18/2018.
 */
public class ContentListItem implements Parcelable {

    private int id;
    private String title;
    private String contentSlug;

    public ContentListItem(int id, String title, String slug) {
        this.id = id;
        this.title = title;
        this.contentSlug = slug;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContentSlug() {
        return contentSlug;
    }

    //region Parcelable
    private static ContentListItem fromParcel(Parcel in) {
        return new ContentListItem(in.readInt(), in.readString(), in.readString());
//        int id = in.readInt();
//        title = in.readString();
//        contentSlug = in.readString();
    }

    public static final Parcelable.Creator<ContentListItem> CREATOR = new Parcelable.Creator<ContentListItem>() {

        /**
         * Create a new instance of the Parcelable class, instantiating it
         * from the given Parcel whose data had previously been written by
         * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
         *
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public ContentListItem createFromParcel(Parcel source) {
            return ContentListItem.fromParcel(source);
        }

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         * initialized to null.
         */
        @Override
        public ContentListItem[] newArray(int size) {
            return new ContentListItem[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(contentSlug);
    }
    //endregion
}
