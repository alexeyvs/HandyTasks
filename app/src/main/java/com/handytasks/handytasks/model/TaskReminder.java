package com.handytasks.handytasks.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class TaskReminder {
    private ReminderParams mReminderParams;
    private ReminderType mReminderType;

    public TaskReminder(ReminderType mReminderType, ReminderParams params) {
        this.mReminderType = mReminderType;
        this.mReminderParams = params;
    }


    public TaskReminder(String reminderParams) throws ParseException {
        Pattern locationPattern = Pattern.compile(ReminderParams.LOCATION_FORMAT);
        Matcher locationMatcher = locationPattern.matcher(reminderParams);
        if (locationMatcher.matches()) {
            mReminderParams = new ReminderParams(locationMatcher.group(1), Double.parseDouble(locationMatcher.group(2)), Double.parseDouble(locationMatcher.group(3)));
            mReminderType = ReminderType.Location;
            return;
        } else {
            SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
            try {
                Date date = parserSDF.parse(reminderParams);
                mReminderType = ReminderType.Timed;
                mReminderParams = new ReminderParams(date);
                return;
            } catch (ParseException e) {
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        switch (getType()) {
            case Timed:
                return String.format("remind:[%s]", getParams().getTriggerDateString());
            case Location:
                ReminderLocationData locationData = getParams().getLocationData();
                return String.format("remind:[%s {%f;%f}]", locationData.getName(), locationData.getLatitude(), locationData.getLongitude());
        }
        return "";
    }

    public ReminderType getType() {
        return mReminderType;
    }

    public void setType(ReminderType type) {
        mReminderType = type;
    }

    public ReminderParams getParams() {
        return mReminderParams;
    }

    public void setParams(ReminderParams params) {
        mReminderParams = params;
    }

    public void setTimedReminder(String strDate) throws ParseException {
        mReminderType = ReminderType.Timed;
        SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
        mReminderParams = new ReminderParams(parserSDF.parse(strDate));
    }

    public enum ReminderType {
        Timed,
        Location
    }


}
