package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.os.Bundle;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.fragments.PrefsFragment;

public class PrefsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

}
