package com.handytasks.handytasks.model;

import android.util.Log;

import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFile;
import com.handytasks.handytasks.interfaces.ICreateTasksResult;
import com.handytasks.handytasks.interfaces.IFSChangeHandler;
import com.handytasks.handytasks.interfaces.IOpenFileResult;
import com.handytasks.handytasks.interfaces.ITaskListChanged;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public class Tasks implements IFSChangeHandler {
    private static final String TAG = "Tasks";
    private final ICloudFS mCloudFS;
    private final TaskTypes.TaskListTypes mType;
    private final ArrayList<Task> mTasksArray = new ArrayList<>();
    private final ArrayList<ITaskListChanged> mTaskListChangedHandlers;
    private ScheduledFuture<?> mLastScheduledStartWatchForChanges;
    private ICloudFile mCloudFile;
    private Lock mLock = new ReentrantLock(true);
    private Boolean mGoingToWrite = false;
    private Runnable mWriteFileTask = new Runnable() {
        @Override
        public void run() {
            mLock.lock();
            StringBuilder data = new StringBuilder(mTasksArray.size() * 20);
            synchronized (mTasksArray) {
                for (int i = 0; i < mTasksArray.size(); i++) {
                    data.append(mTasksArray.get(i).getTaskText());
                    data.append("\n");
                }
            }
            mGoingToWrite = false;

            mCloudFS.WriteTextFile(mCloudFile, data.toString(), new IAsyncResult() {
                @Override
                public void OnSuccess(String result) {
                    mLock.unlock();
                    Log.d(TAG, "Write completed");

                }

                @Override
                public void OnFailure(String result) {
                    mLock.unlock();
                }
            });
        }
    };
    private ScheduledExecutorService mScheduledPool = Executors.newScheduledThreadPool(1);

    public Tasks(TaskTypes.TaskListTypes type, ICloudFS fs, final String filename, final ICreateTasksResult callback) {
        mType = type;
        mCloudFS = fs;
        mTaskListChangedHandlers = new ArrayList<>();

        mCloudFS.initializeFS(new IAsyncResult() {
            @Override
            public void OnSuccess(String result) {
                mCloudFS.CreateTextFile(filename, new IOpenFileResult() {
                    @Override
                    public void OnSuccess(ICloudFile result) {
                        mCloudFile = result;
                        callback.OnSuccess(Tasks.this, 0);
                        startWatchForChanges();
                    }

                    @Override
                    public void OnFailure(String result) {
                        callback.OnFailure(result);
                    }
                });
            }

            @Override
            public void OnFailure(String result) {
                callback.OnFailure(result);
            }
        });


    }

    public void addChangedEventHandler(ITaskListChanged listChangedEvent) {
        synchronized (mTaskListChangedHandlers) {
            if (!mTaskListChangedHandlers.contains(listChangedEvent)) {
                mTaskListChangedHandlers.add(listChangedEvent);
            }
        }
    }

    public void readIfEmpty(final IAsyncResult callback) {
        if (mTasksArray.isEmpty()) {
            Read(callback);
        } else {
            callback.OnSuccess(null);
        }


    }

    public synchronized Tasks Read(final IAsyncResult callback) {
        mLock.lock();

        Log.d(TAG, "Reading file...");
        mCloudFS.ReadTextFile(mCloudFile, true, new IAsyncResult() {
            @Override
            public void OnSuccess(String result) {
                synchronized (mTasksArray) {
                    mTasksArray.clear();
                    if (!result.trim().isEmpty()) {
                        String[] lines = result.split("\n");
                        for (int i = 0; i < lines.length; i++) {
                            mTasksArray.add(new Task(lines[i], i, Tasks.this));
                        }
                    }
                    Log.d(TAG, String.format("Parsed %d records", mTasksArray.size()));
                }
                mLock.unlock();

                synchronized (mTaskListChangedHandlers) {
                    for (ITaskListChanged mTaskListChangedHandler : mTaskListChangedHandlers) {
                        mTaskListChangedHandler.TaskListChanged(Tasks.this);
                    }
                }

                callback.OnSuccess(null);
                Log.d(TAG, "Read completed");
            }

            @Override
            public void OnFailure(String result) {
                mLock.unlock();
                Log.e(TAG, result);
                callback.OnFailure(result);

            }
        });

        return this;
    }

    public synchronized void Write() {
        mLock.lock();
        stopWatchForChanges();
        StringBuilder data = new StringBuilder(mTasksArray.size() * 20);

        synchronized (this) {
            for (int i = 0; i < mTasksArray.size(); i++) {
                data.append(mTasksArray.get(i).getTaskText());
                data.append("\n");
            }
        }
        if (null != mLastScheduledStartWatchForChanges) {
            mLastScheduledStartWatchForChanges.cancel(false);
        }
        mLastScheduledStartWatchForChanges = mScheduledPool.schedule(new Runnable() {
            @Override
            public void run() {
                startWatchForChanges();
            }
        }, 20, TimeUnit.SECONDS);


        mCloudFS.WriteTextFile(mCloudFile, data.toString(), new IAsyncResult() {
            @Override
            public void OnSuccess(String result) {
                mLock.unlock();
                Log.d(TAG, "Write completed");
                // delay start startWatchForChanges


            }

            @Override
            public void OnFailure(String result) {
                mLock.unlock();
            }
        });
    }


// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    // use X seconds delay and buffer all the writes to decrease server overhead
//    public void Write1() {
//        final int delayBeforeWrite = 3;
//        Log.d(TAG, "Writing file " + mCloudFile.getFilename());
//        if (false == mGoingToWrite) {
//            mGoingToWrite = true;
//            Log.d(TAG, String.format("Writing of file %s scheduled with %d seconds delay", mCloudFile.getFilename(), delayBeforeWrite));
//            mScheduledPool.schedule(mWriteFileTask, delayBeforeWrite, TimeUnit.SECONDS);
//        } else {
//            Log.d(TAG, "Writing already scheduled");
//        }
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)


    public ArrayList<Task> getList() {
        return mTasksArray;
    }

    void startWatchForChanges() {
        mCloudFS.getWatcher(mCloudFile).StartWatch(this);
    }

    void stopWatchForChanges() {
        mCloudFS.getWatcher(mCloudFile).StopWatch();
    }


    @Override
    public void PathChanged(String name) {
        if (!name.equals(mCloudFile.getFilename())) {
            return;
        }

        Log.d(TAG, "FS change event occurred");


        Read(new IAsyncResult() {
            @Override
            public void OnSuccess(String result) {

            }

            @Override
            public void OnFailure(String result) {

            }
        });

    }

    public void Add(Task taskItem) {
        synchronized (this) {
            taskItem.setParent(this);
            taskItem.setLineNumber(mTasksArray.size());
            mTasksArray.add(taskItem);
            taskListChanged();
        }

    }

    public void setTask(Task task) {
        synchronized (this) {
            for (int i = 0; i < mTasksArray.size(); i++) {
                if (mTasksArray.get(i).getLineNumber() == task.getLineNumber()) {
                    if (task.getTaskText().equals(mTasksArray.get(i).getTaskText())) {
                        // nothing important changed
                        return;
                    }
                    task.setParent(this);
                    mTasksArray.set(i, task);
                    break;
                }
            }

            taskListChanged();
        }
    }

    public ICloudAPI getAPI() {
        return mCloudFS.getAPI();
    }

    public void deleteTask(Task task) {
        synchronized (this) {
            for (int i = 0; i < mTasksArray.size(); i++) {
                if (mTasksArray.get(i).getLineNumber() == task.getLineNumber()) {
                    mTasksArray.remove(i);
                    for (int j = i; j < mTasksArray.size(); j++) {
                        mTasksArray.get(j).setLineNumber(mTasksArray.get(j).getLineNumber() - 1);
                    }
                    taskListChanged();
                    break;
                }
            }
        }
    }

    public void taskListChanged() {
        synchronized (mTaskListChangedHandlers) {
            for (ITaskListChanged mTaskListChangedHandler : mTaskListChangedHandlers) {
                mTaskListChangedHandler.TaskListChanged(this);
            }
        }

        Write();
    }

    public TaskTypes.TaskListTypes getType() {
        return mType;
    }

    public void sort(SortTypes sortType) {
        switch (sortType) {
            case AZ:
                Collections.sort(mTasksArray, new Comparator<Task>() {
                    @Override
                    public int compare(Task lhs, Task rhs) {
                        return lhs.getTaskPlainText().compareToIgnoreCase(rhs.getTaskPlainText());
                    }
                });
                break;
            case ZA:
                Collections.sort(mTasksArray, new Comparator<Task>() {
                    @Override
                    public int compare(Task lhs, Task rhs) {
                        return rhs.getTaskPlainText().compareToIgnoreCase(lhs.getTaskPlainText());
                    }
                });
                break;
            case IncompleteAZ:
                Collections.sort(mTasksArray, new Comparator<Task>() {
                    @Override
                    public int compare(Task lhs, Task rhs) {
                        if (lhs.isCompleted() && !rhs.isCompleted()) {
                            return 1;
                        } else if (!lhs.isCompleted() && rhs.isCompleted()) {
                            return -1;
                        } else {
                            return lhs.getTaskPlainText().compareToIgnoreCase(rhs.getTaskPlainText());
                        }
                    }
                });
                break;
            case IncompleteZA:
                Collections.sort(mTasksArray, new Comparator<Task>() {
                    @Override
                    public int compare(Task lhs, Task rhs) {
                        if (lhs.isCompleted() && !rhs.isCompleted()) {
                            return 1;
                        } else if (!lhs.isCompleted() && rhs.isCompleted()) {
                            return -1;
                        } else {
                            return rhs.getTaskPlainText().compareToIgnoreCase(lhs.getTaskPlainText());
                        }
                    }
                });
                break;

        }
        taskListChanged();
    }

    public boolean contains(final Task key) {
        return mTasksArray.contains(key);
    }

    public Task findByText(final String taskText) {
        synchronized (mTasksArray) {
            for (Task task : mTasksArray) {
                if (task.getTaskPlainText().equals(taskText)) {
                    return task;
                }
            }
        }
        return null;
    }

    public Task findByPlaintextAndLine(final String taskText, final int lineNumber) {
        synchronized (mTasksArray) {
            for (Task task : mTasksArray) {
                if (task.getTaskPlainText().equals(taskText) &&
                        task.getLineNumber() == lineNumber) {
                    return task;
                }
            }
        }
        return null;
    }


    public enum SortTypes {
        AZ,
        ZA,
        IncompleteAZ,
        IncompleteZA
    }
}
