package com.handytasks.handytasks.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudFSStorage;
import com.handytasks.handytasks.interfaces.ICreateTasksResult;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by avsho_000 on 3/25/2015.
 */
public class TaskTypes {
    private final static String TAG = "TaskTypes";
    private final ICloudFSStorage mFSStorage;
    private final Hashtable<TaskListTypes, TasksContainer> mTasks = new Hashtable<>();

    public TaskTypes(ICloudFSStorage storage) {
        mFSStorage = storage;
        mTasks.put(TaskListTypes.MainList, new TasksContainer("todo.txt", R.string.title_task_list_mainlist));
        mTasks.put(TaskListTypes.ArchivedList, new TasksContainer("archive.txt", R.string.title_task_list_archive));
    }

    public TaskListTypes getTasksType(Tasks source) {
        for (Map.Entry<TaskListTypes, TasksContainer> taskListTypesTasksContainerEntry : mTasks.entrySet()) {
            if (source == taskListTypesTasksContainerEntry.getValue().getTasks()) {
                return taskListTypesTasksContainerEntry.getKey();
            }
        }
        return null;
    }

    public synchronized void getTasks(boolean force, final TaskListTypes type, final ICreateTasksResult callback) {
        if (force || null == mTasks.get(type).getTasks()) {
            if (null == mFSStorage.getFS()) {
                callback.OnFailure("API is not not initialized");
                return;
            }

            new Tasks(type, mFSStorage.getFS(), mTasks.get(type).getFilename(), new ICreateTasksResult() {
                @Override
                public void OnSuccess(Tasks result, int title) {
                    mTasks.put(type, mTasks.get(type).setTasks(result));
                    callback.OnSuccess(result, mTasks.get(type).getTitle());
                }

                @Override
                public void OnFailure(String result) {
                    mTasks.put(type, mTasks.get(type).setTasks(null));
                    callback.OnFailure(result);
                }
            });
        } else {
            callback.OnSuccess(mTasks.get(type).getTasks(), mTasks.get(type).getTitle());
        }
    }

    public void archiveTask(final Task task, final IAsyncResult callback, boolean ifRequired, final Context context) {
        // find task in main list
        final Tasks mainList = mTasks.get(TaskListTypes.MainList).getTasks();

        if (ifRequired) {
            if (!task.isCompleted()) {
                if (null != callback) callback.OnSuccess(null);
                return;
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("auto_archive_completed", false)) {
                if (null != callback) callback.OnSuccess(null);
                return;
            }
        }

        getTasks(false, TaskListTypes.ArchivedList, new ICreateTasksResult() {
            @Override
            public void OnSuccess(Tasks result, int title) {
                Log.d(TAG, String.format("Archiving task %s line number %d", task.getTaskText(), task.getLineNumber()));
                result.Add(new Task(task));
                mainList.deleteTask(task);
                new Toast(context).makeText(context, String.format("Task \"%s\" archived", task.getTaskPlainText()), Toast.LENGTH_LONG).show();
                if (null != callback) callback.OnSuccess(null);
            }

            @Override
            public void OnFailure(String result) {
                if (null != callback) callback.OnFailure(result);
            }
        });

        // open archive
    }

    public void clear() {
        synchronized (mTasks) {
            Enumeration<TaskListTypes> enumKey = mTasks.keys();
            while (enumKey.hasMoreElements()) {
                TaskListTypes key = enumKey.nextElement();
                mTasks.get(key).resetTasks();
            }
        }
    }

    public void createNewTask(final String text, final ITaskCreatedResult callback) {
        getTasks(false, TaskTypes.TaskListTypes.MainList, new ICreateTasksResult() {
            @Override
            public void OnSuccess(final Tasks result, final int title) {
                Task newTask = new Task(text, 0, result);
                result.Add(newTask);
                callback.OnSuccess(newTask);
            }

            @Override
            public void OnFailure(String result) {
                callback.OnFailure(result);
            }
        });
    }

    public enum TaskListTypes {
        MainList,
        ArchivedList
    }


    public interface ITaskCreatedResult {
        public void OnSuccess(Task task);

        public void OnFailure(String error);
    }

    public class TasksContainer {
        private String mFilename;
        private int mTitle;
        private Tasks mTasks;

        public TasksContainer(String filename, int title) {
            setTitle(title);
            setFilename(filename);
        }

        public int getTitle() {
            return mTitle;
        }

        public void setTitle(int mTitle) {
            this.mTitle = mTitle;
        }

        public String getFilename() {
            return mFilename;
        }


        public TasksContainer setFilename(String mFilename) {
            this.mFilename = mFilename;
            return this;
        }

        public Tasks getTasks() {
            return mTasks;
        }


        public TasksContainer setTasks(Tasks mTasks) {
            this.mTasks = mTasks;
            return this;
        }

        public void resetTasks() {
            mTasks = null;
        }
    }
}
