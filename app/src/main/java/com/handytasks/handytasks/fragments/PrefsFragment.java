package com.handytasks.handytasks.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.handytasks.handytasks.R;

/**
 * Created by avsho_000 on 3/9/2015.
 */
public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);
    }
}
