package com.handytasks.handytasks.model;

import android.app.Activity;
import android.text.format.DateUtils;

import com.handytasks.handytasks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by avsho_000 on 4/12/2015.
 */
public class TimeIntervals {
    private final Activity mActivity;
    private ArrayList<TimeInterval> mIntervals;

    public TimeIntervals(Activity activity) {
        mActivity = activity;
        mIntervals = new ArrayList<>(4);
        mIntervals.add(new TimeInterval(activity.getString(R.string.timeinterval_morning), 9, 0));
        mIntervals.add(new TimeInterval(activity.getString(R.string.timeinterval_afternoon), 13, 0));
        mIntervals.add(new TimeInterval(activity.getString(R.string.timeinterval_evening), 17, 0));
        mIntervals.add(new TimeInterval(activity.getString(R.string.timeinterval_night), 20, 0));
    }

    public TimeInterval getInterval(int position) {
        return mIntervals.get(position);
    }

    public String getCaptionByTime(Date triggerDate) {
        Calendar c = Calendar.getInstance();

        for (TimeInterval interval : mIntervals) {
            if (interval.match(triggerDate)) {
                return interval.getCaption();
            }
        }
        return null;
    }

    public int getCount() {
        return mIntervals.size();
    }

    public ArrayList<TimeInterval> getTimeIntervals() {
        return mIntervals;
    }

    public String formatMenuTitle(TimeInterval interval) {
        return String.format("%s (%s)", interval.getCaption(), formatTime(interval.getHours(), interval.getMinutes()));
    }

    private String formatTime(int hours, int minutes) {
        // Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        return DateUtils.formatDateTime(mActivity.getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
    }

    public class TimeInterval {
        private final String mCaption;
        private final int mHour;
        private final int mMinute;

        private TimeInterval(String caption, int hour, int minute) {
            this.mCaption = caption;
            this.mHour = hour;
            this.mMinute = minute;
        }

        public String getCaption() {
            return mCaption;
        }

        public int getHours() {
            return mHour;
        }

        public int getMinutes() {
            return mMinute;
        }

        public boolean match(Date date) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            if (c.get(Calendar.HOUR_OF_DAY) == getHours() && c.get(Calendar.MINUTE) == getMinutes()) {
                return true;
            } else {
                return false;
            }
        }
    }

}
