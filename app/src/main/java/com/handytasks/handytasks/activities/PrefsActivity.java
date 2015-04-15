package com.handytasks.handytasks.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.fragments.PrefsFragment;

public class PrefsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        getFragmentManager().beginTransaction()
                .replace(R.id.prefs_fragment, new PrefsFragment())
                        // .replace(R.id.init_cloud_fragment, new SetCloudParamsFragment())
                .commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
