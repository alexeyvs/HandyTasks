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
            mReminderParams = new ReminderParams(locationMatcher.group(1),
                    Double.parseDouble(locationMatcher.group(2).replace(',', '.')),
                    Double.parseDouble(locationMatcher.group(3).replace(',', '.')));
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

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setType(ReminderType type) {
//        mReminderType = type;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    public ReminderParams getParams() {
        return mReminderParams;
    }

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setParams(ReminderParams params) {
//        mReminderParams = params;
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public void setTimedReminder(String strDate) throws ParseException {
//        mReminderType = ReminderType.Timed;
//        SimpleDateFormat parserSDF = new SimpleDateFormat(ReminderParams.DATE_FORMAT);
//        mReminderParams = new ReminderParams(parserSDF.parse(strDate));
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    public enum ReminderType {
        Timed,
        Location
    }


}
