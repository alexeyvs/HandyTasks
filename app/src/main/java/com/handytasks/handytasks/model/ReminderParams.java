package com.handytasks.handytasks.model;

import android.location.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class ReminderParams {
    public final static String DATE_FORMAT = "EEE MMM d HH:mm:ss zzz yyyy";
    private Date mDate;
    private Location mLocation;

    public ReminderParams(Date date) {
        mDate = date;
    }

    public ReminderParams(Location location) {
        mLocation = location;
    }

    public ReminderParams(String reminderStringValue) {
        //TODO add location
        SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
        try {
            mDate = parserSDF.parse(reminderStringValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getTriggerDate() {
        return mDate;
    }

    public String getTriggerDateString() {
        SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
        return parserSDF.format(mDate);
    }
}