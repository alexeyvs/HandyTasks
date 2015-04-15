package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.impl.Dropbox.DbAPI;
import com.handytasks.handytasks.impl.ErrorReporter;
import com.handytasks.handytasks.impl.GoogleDrive.GDFS;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFile;
import com.handytasks.handytasks.interfaces.IInitAPI;
import com.handytasks.handytasks.interfaces.IOpenFileResult;

public class InitCloud extends Activity {
    public static final int REQUEST_CODE_INIT_CLOUD = 1;
    public static final int REQUEST_CODE_SELECT_PROVIDER = 2;
    private static final String TAG = "InitCloud activity";

    private ICloudAPI m_CloudAPI;

    private void setConnectionSetupInProgress(boolean value) {
        ((HTApplication) getApplication()).setConnectionSetupInProgress(value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConnectionSetupInProgress(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_cloud);
    }

    public void onInitDropbox(View view) {
        SharedPreferences prefs = getSharedPreferences(
                "com.handytasks.handytasks", Context.MODE_MULTI_PROCESS);
        prefs.edit().putString("cloud_type", "Dropbox").apply();

        ((HTApplication) getApplication()).resetCloudAPI();
        startInitCloud();
    }

    public void onInitGoogleDrive(View view) {
        SharedPreferences prefs = getSharedPreferences(
                "com.handytasks.handytasks", Context.MODE_MULTI_PROCESS);
        prefs.edit().putString("cloud_type", "Google Drive").apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Warning")
                .setMessage("Google Drive sync are not always happen immediately when your todo list changes somewhere outside this application. To force sync use 'Sync now' menu item in task list.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((HTApplication) getApplication()).resetCloudAPI();
                        startInitCloud();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onTest(View view) {
        m_CloudAPI = ((HTApplication) getApplication()).getCloudAPI();
        if (null == m_CloudAPI) {
            ((HTApplication) getApplication()).generateAPI(this, getApplicationContext(), new IInitAPI() {
                @Override
                public void OnSuccess(ICloudAPI result) {
                    m_CloudAPI = result;
                    ((HTApplication) getApplication()).setAPI(result);
                    m_CloudAPI.getFS().initializeFS(new IAsyncResult() {
                        @Override
                        public void OnSuccess(String result) {
                            try {
                                m_CloudAPI.getFS().CreateTextFile("todo.txt", new IOpenFileResult() {
                                    @Override
                                    public void OnSuccess(ICloudFile result) {
                                        m_CloudAPI.getFS().ReadTextFile(result, true, new IAsyncResult() {
                                            @Override
                                            public void OnSuccess(String result) {
                                                Log.d(TAG, result);
                                            }

                                            @Override
                                            public void OnFailure(String result) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void OnFailure(String result) {

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void OnFailure(String result) {

                        }
                    });
                }

                @Override
                public void OnActionRequired(Object action) {

                }

                @Override
                public void OnFailure(Object result) {
                    Log.e(TAG, result.toString());
                    ErrorReporter.ReportError(GDFS.class, InitCloud.this, result);
                }
            }, true);
        }
    }

    void startInitCloud() {
        m_CloudAPI = ((HTApplication) getApplication()).getCloudAPI();
        if (null == m_CloudAPI) {
            ((HTApplication) getApplication()).generateAPI(this, getApplicationContext(), new IInitAPI() {
                @Override

                public void OnSuccess(ICloudAPI result) {
                    m_CloudAPI = result;
                    ((HTApplication) getApplication()).setAPI(result);
                    if (m_CloudAPI.isReady()) {
                        Intent intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                        setConnectionSetupInProgress(false);
                        finish();
                    }
                }

                @Override
                public void OnActionRequired(Object action) {
                    m_CloudAPI = (ICloudAPI) action;
                    assert (m_CloudAPI.getClass() == DbAPI.class);
                }

                @Override
                public void OnFailure(Object result) {
                    if (m_CloudAPI != null) {
                        ErrorReporter.ReportError(m_CloudAPI.getClass(), InitCloud.this, result);
                    } else {
                        ErrorReporter.ReportError(this.getClass(), InitCloud.this, result);
                    }

                }
            }, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setConnectionSetupInProgress(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == m_CloudAPI) {
            return;
        }
        if (m_CloudAPI.getNativeActivityCode() == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                m_CloudAPI.connect(new IInitAPI() {

                    @Override
                    public void OnSuccess(ICloudAPI result) {
                        ((HTApplication) getApplication()).setAPI(m_CloudAPI);
                        Intent intent = new Intent(InitCloud.this, MainActivity.class);
                        startActivity(intent);
                        setConnectionSetupInProgress(false);
                        finish();
                    }

                    @Override
                    public void OnActionRequired(Object action) {

                    }

                    @Override
                    public void OnFailure(Object result) {
                        ErrorReporter.ReportError(m_CloudAPI.getClass(), InitCloud.this, result.toString());
                    }
                });
            } else {
                ErrorReporter.ReportError(this.getClass(), InitCloud.this, "Connection setup canceled");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_init_cloud, menu);
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
