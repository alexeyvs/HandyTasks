package com.handytasks.handytasks.model;

import android.app.NotificationManager;
import android.content.ContextWrapper;
import android.os.Parcel;
import android.os.Parcelable;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.controls.TasksAdapter;

import java.text.ParseException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by avsho_000 on 3/12/2015.
 */
public class Task implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    // --Commented out by Inspection (4/15/2015 11:24 PM):private static final String TAG = "Task";
    private final long mId;
    private NotificationManager mNotificationManager;
    private Tasks m_Parent;
    private String mTaskText = "";
    private int m_LineNumber;
    private TasksAdapter m_Adapter;
    private TaskTypes.TaskListTypes mType;
    private TaskReminder mReminder;
    // --Commented out by Inspection (4/15/2015 11:24 PM):private CharSequence notificationTitle;

    private Task(Parcel in) {
        mId = new Random().nextLong();
        mTaskText = in.readString();
        mTaskText = getTaskText();
        setLineNumber(in.readInt());
        setType(TaskTypes.TaskListTypes.valueOf(in.readString()));
    }

    public Task(Task task) {
        mId = new Random().nextLong();
        mTaskText = task.mTaskText;
        setLineNumber(task.getLineNumber());
        setType(task.getType());
        setParent(task.getParent());
        setAdapter(m_Adapter);
        setReminder(task.getReminder());
    }

    public Task(String s, int lineNumber, Tasks parent) {
        mId = new Random().nextLong();
        mTaskText = s;
        mTaskText = getTaskText();
        setLineNumber(lineNumber);
        m_Parent = parent;
        setType(m_Parent.getType());
    }

    public Task() {
        mId = new Random().nextLong();
        setTaskText("");
        setLineNumber(0);
        setType(TaskTypes.TaskListTypes.MainList);
    }

    public long getId() {
        return mId;
    }

    public boolean isCompleted() {
        return mTaskText.matches("^\\s*[+]\\s*.*$");
    }

    public Task setCompleted(boolean completed) {
        if (isCompleted() == completed) {
            return this;
        }

        if (completed) {
            mTaskText = "+ " + mTaskText;
            dismissNotification();
        } else {
            mTaskText = mTaskText.replaceFirst("^\\s*[+]\\s*", "");
        }
        return this;
    }

    public int getLineNumber() {
        return m_LineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.m_LineNumber = lineNumber;
    }

    public String getTaskText() {
        return mTaskText.replace("\r", "").replace("\n", "");
    }

    // set task plain text
    public void setTaskText(String taskText) {
        // keep special values
        TaskReminder reminder = getReminder();
        boolean isCompleted = isCompleted();

        // set new text, removing all the line breaks
        mTaskText = taskText.replace("\r", "").replace("\n", "");

        // restore special values
        setReminder(reminder);
        setCompleted(isCompleted);
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        mNotificationManager = notificationManager;
    }

    public void dismissNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(getTaskPlainText().hashCode() + getLineNumber());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTaskText);
        dest.writeInt(getLineNumber());
        dest.writeString(getType().toString());
    }

    public void setAdapter(TasksAdapter adapter) {
        this.m_Adapter = adapter;
    }

    public void notifyAdapterDataSetChanged() {
        if (null != m_Adapter) {
            m_Adapter.notifyDataSetChanged();
        }
    }

    public Tasks getParent() {
        return m_Parent;
    }

    public void setParent(Tasks parent) {
        m_Parent = parent;
    }

    public String getTaskPlainText() {
        return getTaskText().replaceFirst("^\\s*[+]\\s*", "").replaceAll("remind:\\[(.*)\\]", "");
    }

    TaskTypes.TaskListTypes getType() {
        return mType;
    }

    void setType(TaskTypes.TaskListTypes type) {
        mType = type;
    }

    // extract reminder params from task text if any
    public TaskReminder getReminder() {
        Pattern reminderPattern = Pattern.compile(".*?remind:\\[(.*?)\\].*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher reminderMatcher = reminderPattern.matcher(getTaskText());
        if (reminderMatcher.matches()) {
            if (reminderMatcher.matches()) {
                if (reminderMatcher.groupCount() < 1) {
                    mReminder = null;
                    return null;
                } else {
                    try {
                        mReminder = new TaskReminder(reminderMatcher.group(1));
                    } catch (ParseException e) {
                        return null;
                    }
                    return mReminder;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // update task text, based on reminder
    public void setReminder(final TaskReminder reminder) {
        mReminder = reminder;
        // find reminder:[...] pattern
        if (mTaskText.matches(".*remind:\\[(.*)\\]")) {
            // already set, update
            if (null == reminder) {
                mTaskText = getTaskText().replaceAll("remind:\\[(.*)\\]", "");
            } else {
                mTaskText = mTaskText.replaceFirst("remind:\\[(.*)\\]", mReminder.toString());
            }
        } else {
            if (mReminder != null) {
                mTaskText = mTaskText.trim() + " " + mReminder.toString();
            }
        }
    }

    public String getNotificationTitle(final ContextWrapper context) {
        TaskReminder reminder = getReminder();
        if (null == reminder) {
            return "";
        }

        if (reminder.getType() == TaskReminder.ReminderType.Timed) {
            return context.getString(R.string.time_for_task);
        }
        if (reminder.getType() == TaskReminder.ReminderType.Location) {
            return context.getString(R.string.you_are_near_location);
        }
        return "";
    }
}
