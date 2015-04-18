package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;

public class TaskList extends BaseActivity {
    private static final String TAG = "TaskList activity";
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService = null;
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
    private boolean mIsBound;
    private Tasks mTasks;
    private TasksAdapter mAdapter;
    private TaskTypes.TaskListTypes mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Bundle extras = getIntent().getExtras();
        if (null == extras || null == extras.getString("TasksType")) {
            mType = TaskTypes.TaskListTypes.MainList;
        } else {
            mType = TaskTypes.TaskListTypes.valueOf(getIntent().getExtras().getString("TasksType", TaskTypes.TaskListTypes.MainList.toString()));
        }

        if (!HTService.isRunning()) {
            startService(new Intent(this, HTService.class));
        }

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
        final ProgressDialog loadingProgress = new ProgressDialog(this);
        loadingProgress.setTitle(getString(R.string.loading_tasks));
        loadingProgress.show();

        getTaskTypes().getTasks(force, mType, new ICreateTasksResult() {
            @Override
            public void OnSuccess(Tasks result, int title) {
                setTitle(title);
                mTasks = result;
                mAdapter = new TasksAdapter(TaskList.this, mTasks);
                mTasks.addChangedEventHandler(mAdapter);
                mAdapter.setActivity(TaskList.this);

                final com.nhaarman.listviewanimations.itemmanipulation.DynamicListView listView = ((com.nhaarman.listviewanimations.itemmanipulation.DynamicListView) findViewById(R.id.todoListView));
                listView.enableDragAndDrop();
                listView.setDraggableManager(new TouchViewDraggableManager(R.id.grep_handle));
                listView.setOnItemMovedListener(new MyOnItemMovedListener(mAdapter));
                listView.setOnItemLongClickListener(new MyOnItemLongClickListener(listView));

                SimpleSwipeUndoAdapter simpleSwipeUndoAdapter = new SimpleSwipeUndoAdapter(mAdapter, getApplicationContext(), new MyOnDismissCallback(mAdapter));

                AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(simpleSwipeUndoAdapter);
                animAdapter.setAbsListView(listView);
                assert animAdapter.getViewAnimator() != null;
                animAdapter.getViewAnimator().setInitialDelayMillis(500);
                listView.setAdapter(animAdapter);

                /* Enable swipe to dismiss */
                listView.enableSimpleSwipeUndo();
                listView.setDivider(null);
                listView.setDividerHeight(0);

                mTasks.readIfEmpty(new IAsyncResult() {
                    @Override
                    public void OnSuccess(String result) {
                        loadingProgress.dismiss();
                        // check if we need to enable filter
                        Intent intent = getIntent();
                        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                            mAdapter.setFilter(intent.getStringExtra(SearchManager.QUERY));
                            setTitle(getString(R.string.search_results) + ": " + intent.getStringExtra(SearchManager.QUERY));
                        }

                        // edit task
                        if (intent.getStringExtra("action") != null && intent.getStringExtra("action").equals("open_task")) {
                            // find task
                            Task taskToOpen = mTasks.findByText(intent.getStringExtra("task_text"));
                            if (null != taskToOpen) {
                                Intent newIntent = new Intent(getApplicationContext(), TaskView.class);
                                newIntent.putExtra("requestCode", TaskView.REQUEST_CODE_EDIT_MODE);
                                newIntent.putExtra("DATA", taskToOpen);
                                startActivityForResult(newIntent, TaskView.REQUEST_CODE_EDIT_MODE);
                                intent.removeExtra("action");
                                intent.removeExtra("task_text");
                            }
                        }

                        // open task
                        if (intent.getStringExtra("action") != null && intent.getStringExtra("action").equals("new_task")) {
                            Intent newIntent;
                            newIntent = new Intent(TaskList.this, TaskView.class);
                            newIntent.putExtra("requestCode", TaskView.REQUEST_CODE_ADD_MODE);
                            if (intent.getExtras().containsKey("task_text")) {
                                newIntent.putExtra("task_text", intent.getStringExtra("task_text"));
                                intent.removeExtra("task_text");
                            } else {
                                newIntent.putExtra("task_text", intent.getStringExtra(""));
                            }
                            intent.removeExtra("action");
                            startActivityForResult(newIntent, TaskView.REQUEST_CODE_ADD_MODE);
                        }
                    }

                    @Override
                    public void OnFailure(String result) {
                        loadingProgress.dismiss();
                        Log.e(TAG, result);
                    }
                });
                Log.d(TAG, "OnSuccess of getTasks done");
            }

            @Override
            public void OnFailure(String result) {
                mTasks = null;
                loadingProgress.dismiss();
                HandleError(result);
            }
        });
    }

    void HandleError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.failed_to_initialize_cloud_connection))
                .setMessage(getString(R.string.error_details) + message + getString(R.string.try_again_question))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                initTasks(false);
                            }
                        }
                );
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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

        switch (id) {
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
                overridePendingTransition(R.anim.in, R.anim.out);
                return true;
            }
            case R.id.action_sync_now:
                ((HTApplication) getApplication()).forceSync(new IAsyncResult() {
                    @Override
                    public void OnSuccess(String result) {
                        mTasks.Read(new IAsyncResult() {
                            @Override
                            public void OnSuccess(String result) {
                                new Toast(getApplicationContext()).makeText(getApplicationContext(), getString(R.string.sync_requested), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void OnFailure(String result) {

                            }
                        });
                    }

                    @Override
                    public void OnFailure(String result) {
                        new Toast(getApplicationContext()).makeText(getApplicationContext(), getString(R.string.failed_to_sync) + result, Toast.LENGTH_LONG).show();
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
        }

        return super.onOptionsItemSelected(item);
    }

    private static class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final DynamicListView mListView;

        MyOnItemLongClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (mListView != null) {
                mListView.startDragging(position - mListView.getHeaderViewsCount());
            }
            return true;
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HTService.MSG_SET_INT_VALUE:
                    // textIntValue.setText("Int Message: " + msg.arg1);
                    if (mTasks != null) {
                        mTasks.taskListChanged();
                    }
                    break;
                case HTService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("str1");
                    if (mTasks != null) {
                        mTasks.taskListChanged();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class MyOnItemMovedListener implements OnItemMovedListener {

        private final TasksAdapter mAdapter;

        private Toast mToast;

        MyOnItemMovedListener(final TasksAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemMoved(final int originalPosition, final int newPosition) {
            if (mToast != null) {
                mToast.cancel();
            }

            mToast = Toast.makeText(getApplicationContext(), "Moved", Toast.LENGTH_SHORT);
            mToast.show();
            mAdapter.commitSwap();
        }
    }

    private class MyOnDismissCallback implements OnDismissCallback {

        private final TasksAdapter mAdapter;

// --Commented out by Inspection START (4/15/2015 11:24 PM):
//        @Nullable
//        private Toast mToast;
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

        MyOnDismissCallback(final TasksAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                getTaskTypes().archiveTask(mAdapter.getItem(position), null, false, TaskList.this.getApplicationContext());
            }
        }
    }

}
