package com.handytasks.handytasks.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class TaskReminder {
    private ReminderParams mReminderParams;
    private ReminderType mReminderType;
    private String date;


    public TaskReminder() {

    }

    public TaskReminder(ReminderType mReminderType, ReminderParams params) {
        this.mReminderType = mReminderType;
        this.mReminderParams = params;
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
