package com.handytasks.handytasks.model;

import com.google.android.gms.location.places.Place;

/**
 * Created by avsho_000 on 4/7/2015.
 */
public class ReminderLocationData {
    private Place mPlace;
    private Double mLatitude;
    private String mName;
    private Double mLongitude;

    public ReminderLocationData(String name, double latitude, double longitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public static ReminderLocationData getDefault() {
        return new ReminderLocationData("Pick location", 0, 0);
    }

    public void setPlace(Place place) {
        this.mPlace = place;
        this.mName = place.getAddress().toString();
        this.mLatitude = place.getLatLng().latitude;
        this.mLongitude = place.getLatLng().longitude;
    }

    public String getPlaceInfo() {
        if (mName != null) {
            if (!mName.trim().equals("")) {
                return mName;
            } else {
                return String.format("%f;%f", mLatitude, mLongitude);
            }

        } else {
            return "No place info";
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

    public void setLatitude(Double latitude) {
        this.mLatitude = latitude;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        this.mLongitude = longitude;
    }
}
