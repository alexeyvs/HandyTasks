package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICreateTasksResult;
import com.handytasks.handytasks.interfaces.IInitAPI;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskTypes;
import com.handytasks.handytasks.model.Tasks;
import com.handytasks.handytasks.service.HTService;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getAction().equals("com.google.android.gm.action.AUTO_SEND")) {
            // create new task
            Intent downloadIntent = new Intent(getIntent());
            downloadIntent.setClass(this, HTService.class);
            startService(downloadIntent);
            finish();
        }
    }

    private void moveOut() {
        if (!((HTApplication) getApplication()).isAPIInitialized()) {
            ((HTApplication) getApplication()).generateAPI(this, getApplicationContext(), new IInitAPI() {
                @Override
                public void OnSuccess(ICloudAPI result) {
                    ((HTApplication) getApplication()).setAPI(result);
                    if (!((HTApplication) getApplication()).isAPIInitialized()) {
                        // start activity to moveToTaskListActivity CloudAPI
                        // this could happen with half uninitialized dropbox
                        moveToInitCloudActivity();
                    } else {
                        apiIsReady();
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
            apiIsReady();
        }
    }

    private void apiIsReady() {
        moveToTaskListActivity();
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

    private void moveToTaskListActivity() {
        Log.d(TAG, "Action: " + getIntent().getAction());
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            for (String key : extras.keySet()) {
                Log.d(TAG, key);
                Object obj = extras.get(key);   //later parse it as per your required type
                Log.d(TAG, obj.toString());
            }
        }

        if (getIntent().getAction().equals(Intent.ACTION_SEND)) {
            // create new task and show it in gui
            createNewTaskFromSendTo(getIntent());
            return;
        }

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

    private void createNewTaskFromSendTo(final Intent intent) {

        if (intent.getExtras() != null
                && (intent.getExtras().containsKey(Intent.EXTRA_TEXT) || intent.getExtras().containsKey(Intent.EXTRA_SUBJECT))) {
            Bundle extras = intent.getExtras();
            String taskText = (extras.getString(Intent.EXTRA_SUBJECT, "") + " " +
                    extras.getString(Intent.EXTRA_TEXT, "")).trim();
            ((HTApplication) getApplication()).getTaskTypes().createNewTask(taskText, new TaskTypes.ITaskCreatedResult() {
                @Override
                public void OnSuccess(Task task) {
                    intent.removeExtra(Intent.EXTRA_TEXT);
                    Intent newIntent = new Intent(getApplicationContext(), TaskView.class);
                    newIntent.putExtra("requestCode", TaskView.REQUEST_CODE_EDIT_MODE);
                    newIntent.putExtra("DATA", task);

                    startActivityForResult(newIntent, TaskView.REQUEST_CODE_EDIT_MODE);
                    getIntent().removeExtra("action");
                    getIntent().removeExtra("task_text");
                }

                @Override
                public void OnFailure(String error) {
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (InitCloud.REQUEST_CODE_SELECT_PROVIDER): {
                if (resultCode == Activity.RESULT_OK) {
                    moveToTaskListActivity();
                } else {
                    startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_SELECT_PROVIDER);
                }
                break;
            }
            case (InitCloud.REQUEST_CODE_INIT_CLOUD): {
                if (resultCode == Activity.RESULT_OK) {
                    moveToTaskListActivity();
                } else {
                    startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_INIT_CLOUD);
                }
                break;
            }
            case (TaskView.REQUEST_CODE_EDIT_MODE): {
                // save changes and finish
                if (null == data.getExtras()) {
                    return;
                }

                final Task taskItem = data.getParcelableExtra("DATA");
                final int action = data.getExtras().getInt("ACTION");

                ((HTApplication) getApplication()).getTaskTypes().getTasks(false, TaskTypes.TaskListTypes.MainList, new ICreateTasksResult() {
                    @Override
                    public void OnSuccess(final Tasks result, final int title) {
                        switch (action) {
                            case TaskView.ACTION_EDIT: {
                                if (null != taskItem) {
                                    result.setTask(taskItem);
                                    getTaskTypes().archiveTask(taskItem, null, true, MainActivity.this.getApplicationContext());
                                }
                                break;
                            }
                            case TaskView.ACTION_DELETE: {
                                if (null != taskItem) {
                                    result.deleteTask(taskItem);
                                }
                                break;
                            }
                            case TaskView.ACTION_ARCHIVE: {
                                getTaskTypes().archiveTask(taskItem, null, false, MainActivity.this.getApplicationContext());
                                break;
                            }

                        }
                        finish();
                    }

                    @Override
                    public void OnFailure(String result) {
                        finish();
                    }
                });


            }
        }
    }

    private TaskTypes getTaskTypes() {
        return ((HTApplication) getApplication()).getTaskTypes();
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
