package com.finalproject.mosapp;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableObject implements Parcelable {
    private int mData;

    // 99.9% of the time you can just ignore this
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ParcelableObject> CREATOR = new Parcelable.Creator<ParcelableObject>() {
        public ParcelableObject createFromParcel(Parcel in) {
            return new ParcelableObject(in);
        }

        public ParcelableObject[] newArray(int size) {
            return new ParcelableObject[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private ParcelableObject(Parcel in) {
        mData = in.readInt();
    }

    private ParcelableObject(Object o) {

    }
}