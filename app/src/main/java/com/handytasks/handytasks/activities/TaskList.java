package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.controls.TasksAdapter;
import com.handytasks.handytasks.factories.CloudAPIFactory;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICreateTasksResult;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskTypes;
import com.handytasks.handytasks.model.Tasks;
import com.handytasks.handytasks.service.HTService;

public class TaskList extends Activity {
    private static final String TAG = "TaskList activity";
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, HTService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
    boolean mIsBound;
    private Tasks mTasks;
    private TasksAdapter mAdapter;
    private TaskTypes.TaskListTypes mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Bundle extras = getIntent().getExtras();
        if (null == extras) {
            mType = TaskTypes.TaskListTypes.MainList;
        } else {
            mType = TaskTypes.TaskListTypes.valueOf(getIntent().getExtras().getString("TasksType", TaskTypes.TaskListTypes.MainList.toString()));
        }

        if (!HTService.isRunning()) {
            startService(new Intent(this, HTService.class));
        }
        ;

        bindService();
    }

    private void bindService() {
        bindService(new Intent(this, HTService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, HTService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private TaskTypes getTaskTypes() {
        return ((HTApplication) getApplication()).getTaskTypes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");
        if (null == mTasks) {
            initTasks(false);
        } else {
            initTasks(!CloudAPIFactory.isCurrentlySetup(this, mTasks.getAPI()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    private void initTasks(boolean force) {


        getTaskTypes().getTasks(force, mType, new ICreateTasksResult() {
            @Override
            public void OnSuccess(Tasks result, int title) {
                setTitle(title);
                mTasks = result;
                mAdapter = new TasksAdapter(TaskList.this, mTasks);
                mTasks.setChangedEventHandler(mAdapter);
                mAdapter.setActivity(TaskList.this);

                com.terlici.dragndroplist.DragNDropListView listView = ((com.terlici.dragndroplist.DragNDropListView) findViewById(R.id.todoListView));
                listView.setDragNDropAdapter(mAdapter);
                // listView.setAdapter(mAdapter);
                // listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                mTasks.readIfEmpty(new IAsyncResult() {
                    @Override
                    public void OnSuccess(String result) {
                        mTasks.startWatchForChanges();
                        // check if we need to enable filter
                        Intent intent = getIntent();
                        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                            mAdapter.setFilter(intent.getStringExtra(SearchManager.QUERY));
                            setTitle("Search results");
                        }


                    }

                    @Override
                    public void OnFailure(String result) {
                        Log.e(TAG, result);
                    }
                });
            }

            @Override
            public void OnFailure(String result) {
                mTasks = null;
                HandleError(result);
            }
        });
    }

    void HandleError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Failed to initialize cloud connection")
                .setMessage("Error details: " + message + "Try again?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                initTasks(false);
                            }
                        }
                );
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            return true;
        }

        getMenuInflater().inflate(R.menu.menu_task_list, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultsActivity.class)));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (TaskView.REQUEST_CODE_ADD_MODE): {
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == Activity.RESULT_OK) {

                        int action = data.getExtras().getInt("ACTION");
                        Task taskItem = data.getParcelableExtra("DATA");

                        switch (action) {
                            case TaskView.ACTION_EDIT: {
                                if (null != taskItem) {
                                    mTasks.Add(taskItem);
                                }
                                break;
                            }
                            case TaskView.ACTION_DELETE: {
                                if (null != taskItem) {
                                    // just do not add
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            }

            case (TaskView.REQUEST_CODE_EDIT_MODE): {
                if (resultCode == Activity.RESULT_OK) {
                    if (null == data.getExtras()) {
                        return;
                    }

                    final Task taskItem = data.getParcelableExtra("DATA");
                    int action = data.getExtras().getInt("ACTION");

                    switch (action) {
                        case TaskView.ACTION_EDIT: {
                            if (null != taskItem) {
                                mTasks.setTask(taskItem);
                                getTaskTypes().archiveTask(taskItem, null, true, TaskList.this.getApplicationContext());
                            }
                            break;
                        }
                        case TaskView.ACTION_DELETE: {
                            if (null != taskItem) {
                                mTasks.deleteTask(taskItem);
                            }
                            break;
                        }
                        case TaskView.ACTION_ARCHIVE: {
                            getTaskTypes().archiveTask(taskItem, null, false, TaskList.this.getApplicationContext());
                            break;
                        }

                    }


                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent;
                intent = new Intent(this, PrefsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            }
            case R.id.action_create_task: {
                Intent intent;
                intent = new Intent(this, TaskView.class);
                intent.putExtra("requestCode", TaskView.REQUEST_CODE_ADD_MODE);
                startActivityForResult(intent, TaskView.REQUEST_CODE_ADD_MODE);
                return true;
            }
            case R.id.action_select_cloud_provider:
                startActivityForResult(new Intent(this, InitCloud.class), InitCloud.REQUEST_CODE_SELECT_PROVIDER);
                return true;
            case R.id.action_sync_now:
                ((HTApplication) getApplication()).forceSync(new IAsyncResult() {
                    @Override
                    public void OnSuccess(String result) {
                        mTasks.Read(new IAsyncResult() {
                            @Override
                            public void OnSuccess(String result) {
                                new Toast(getApplicationContext()).makeText(getApplicationContext(), "Sync requested", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void OnFailure(String result) {

                            }
                        });
                    }

                    @Override
                    public void OnFailure(String result) {
                        new Toast(getApplicationContext()).makeText(getApplicationContext(), "Failed to sync: " + result, Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            case R.id.action_view_archive: {
                Intent intent;
                intent = new Intent(this, ViewHistoryActivity.class);
                intent.putExtra("TasksType", TaskTypes.TaskListTypes.ArchivedList.toString());
                // intent.putExtra("filename", "archive.txt");
                startActivityForResult(intent, 0);
                return true;
            }
            case R.id.action_about: {
                Intent intent;
                intent = new Intent(this, AboutActivity.class);
                startActivityForResult(intent, 0);
                return true;
            }
            case R.id.action_sort_az:
                mTasks.sort(Tasks.SortTypes.AZ);
                break;
            case R.id.action_sort_za:
                mTasks.sort(Tasks.SortTypes.ZA);
                break;
            case R.id.action_sort_incomplete_az:
                mTasks.sort(Tasks.SortTypes.IncompleteAZ);
                break;
            case R.id.action_sort_incomplete_za:
                mTasks.sort(Tasks.SortTypes.IncompleteZA);
                break;
            case R.id.action_test:
                Intent startServiceIntent = new Intent(getApplicationContext(), HTService.class);
                getApplicationContext().startService(startServiceIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HTService.MSG_SET_INT_VALUE:
                    // textIntValue.setText("Int Message: " + msg.arg1);
                    mTasks.taskListChanged();
                    break;
                case HTService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("str1");
                    mTasks.taskListChanged();
                    // textStrValue.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
