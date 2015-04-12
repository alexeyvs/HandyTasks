package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.IInitAPI;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        moveOut();
    }

    private void moveOut() {
        if (!((HTApplication) getApplication()).isAPIInitialized()) {
            ((HTApplication) getApplication()).generateAPI(this, getApplicationContext(), new IInitAPI() {
                @Override
                public void OnSuccess(ICloudAPI result) {
                    ((HTApplication) getApplication()).setAPI(result);
                    if (!((HTApplication) getApplication()).isAPIInitialized()) {
                        // start activity to moveToTaskViewActivity CloudAPI
                        // this could happen with half uninitialized dropbox
                        moveToInitCloudActivity();
                    } else {
                        moveToTaskViewActivity();
                    }
                }

                @Override
                public void OnActionRequired(Object action) {

                }

                @Override
                public void OnFailure(Object result) {
                    moveToInitCloudActivity();
                }
            }, false);
        } else {
            moveToTaskViewActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        moveOut();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void moveToInitCloudActivity() {
        // startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_INIT_CLOUD);
        startActivity(new Intent(this, InitCloud.class));
        finish();
    }

    private void moveToTaskViewActivity() {
        Intent intent = new Intent(this, TaskList.class);
        String action = getIntent().getStringExtra("action");
        String text = getIntent().getStringExtra("task_text");
        if (action != null) {
            intent.putExtra("action", action);
            getIntent().removeExtra("action");
        }
        if (text != null) {
            intent.putExtra("task_text", text);
            getIntent().removeExtra("task_text");
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (InitCloud.REQUEST_CODE_SELECT_PROVIDER): {
                if (resultCode == Activity.RESULT_OK) {
                    moveToTaskViewActivity();
                } else {
                    startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_SELECT_PROVIDER);
                }
                break;
            }
            case (InitCloud.REQUEST_CODE_INIT_CLOUD): {
                if (resultCode == Activity.RESULT_OK) {
                    moveToTaskViewActivity();
                } else {
                    startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_INIT_CLOUD);
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
