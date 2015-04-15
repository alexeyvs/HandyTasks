package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.handytasks.handytasks.R;

/**
 * Created by avsho_000 on 4/15/2015.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "HandyTasksTheme");
        if (theme.equals("HandyTasksTheme")) {
            setTheme(R.style.HandyTasksTheme);
        } else if (theme.equals("HandyTasksThemeDark")) {
            setTheme(R.style.HandyTasksThemeDark);
        }
        super.onCreate(savedInstanceState);
    }
}
