package com.handytasks.handytasks.model;

import android.content.ContextWrapper;

import com.google.android.gms.location.places.Place;
import com.handytasks.handytasks.R;

/**
 * Created by avsho_000 on 4/7/2015.
 */
public class ReminderLocationData {
    // --Commented out by Inspection (4/15/2015 11:24 PM):private Place mPlace;
    private Double mLatitude;
    private String mName;
    private Double mLongitude;

    public ReminderLocationData(String name, double latitude, double longitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public static ReminderLocationData getDefault(ContextWrapper context) {
        return new ReminderLocationData(context.getString(R.string.pick_location), 0, 0);
    }

    public void setPlace(Place place) {
        this.mName = place.getAddress().toString();
        this.mLatitude = place.getLatLng().latitude;
        this.mLongitude = place.getLatLng().longitude;
    }

    public String getPlaceInfo(final ContextWrapper context) {
        if (mName != null) {
            if (!mName.trim().equals("")) {
                return mName;
            } else {
                return String.format("%f;%f", mLatitude, mLongitude);
            }

        } else {
            return context.getString(R.string.no_place_info);
        }
    }

    public void setPlace(String name, Double latitude, Double longitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setLatitude(Double latitude) {
//        this.mLatitude = latitude;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    public String getName() {
        return mName;
    }

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setName(String name) {
//        this.mName = name;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    public Double getLongitude() {
        return mLongitude;
    }

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setLongitude(Double longitude) {
//        this.mLongitude = longitude;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)
}
