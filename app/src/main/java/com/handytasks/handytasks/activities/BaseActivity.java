package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.handytasks.handytasks.R;
import com.handytasks.handytasks.impl.HTApplication;

/**
 * Created by avsho_000 on 4/15/2015.
 */
public class BaseActivity extends Activity {
    public static void sendScreenView(Activity activity) {
        Tracker t = ((HTApplication) activity.getApplication()).getTracker(HTApplication.TrackerName.APP_TRACKER);
        t.setScreenName(activity.getClass().getName());
        t.send(new HitBuilders.ScreenViewBuilder().build());

    }

    public static void setTheme(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = prefs.getString("theme", "HandyTasksTheme");
        if (theme.equals("HandyTasksTheme")) {
            activity.setTheme(R.style.HandyTasksTheme);
        } else if (theme.equals("HandyTasksThemeDark")) {
            activity.setTheme(R.style.HandyTasksThemeDark);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendScreenView(this);
    }
}
