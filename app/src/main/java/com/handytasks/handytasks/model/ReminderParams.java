package com.handytasks.handytasks.model;

import android.content.Context;
import android.text.format.DateUtils;

import com.google.android.gms.location.places.Place;
import com.handytasks.handytasks.interfaces.IReminderParamsUpdated;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class ReminderParams {
    public final static String DATE_FORMAT = "EEE MMM d HH:mm:ss zzz yyyy";
    public final static String LOCATION_FORMAT = "(.*?)\\{(.*?);(.*?)\\}";
    private Date mDate;
    private ReminderLocationData mLocation;
    private IReminderParamsUpdated mHandler;

    public ReminderParams(Date date) {
        mDate = date;
    }

    public ReminderParams(ReminderLocationData location) {
        mLocation = location;
    }

    public ReminderParams(String name, double latitude, double longitude) {
        mLocation = new ReminderLocationData(name, latitude, longitude);
        setPlace(name, latitude, longitude);
    }

    public Date getTriggerDate() {
        return mDate;
    }

    public void setTriggerDate(Date triggerDate) {
        this.mDate = triggerDate;
        signalUpdate();
    }

    public String getTriggerDateString() {
        SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
        return parserSDF.format(mDate);
    }

    public String getTriggerDateUFString(Context context, int flags) {
        // Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar c = Calendar.getInstance();
        c.setTime(mDate);
        return DateUtils.formatDateTime(context, c.getTimeInMillis(), flags);
    }

    public void setChangesHandler(IReminderParamsUpdated handler) {
        mHandler = handler;
    }

    private void signalUpdate() {
        if (mHandler != null) {
            mHandler.OnUpdateReminderParams(this);
        }
    }

    public void setPlace(Place place) {
        mLocation.setPlace(place);
        signalUpdate();
    }

    public String getPlaceInfo() {
        return mLocation.getPlaceInfo();
    }

    public void setPlace(String name, Double latitude, Double longitude) {
        mLocation.setPlace(name, latitude, longitude);
        signalUpdate();
    }

    public ReminderLocationData getLocationData() {
        return mLocation;
    }
}