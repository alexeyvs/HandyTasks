package com.handytasks.handytasks.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.handytasks.handytasks.controls.TasksAdapter;

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
    private static final String TAG = "Task";
    private Tasks m_Parent;
    private String mTaskText;
    private int m_LineNumber;
    private TasksAdapter m_Adapter;
    private TaskTypes.TaskListTypes mType;
    private TaskReminder mReminder;

    private Task(Parcel in) {
        setTaskText(in.readString());
        setLineNumber(in.readInt());
        setType(TaskTypes.TaskListTypes.valueOf(in.readString()));
        /*
        byte reminderExists = in.readByte();
        if ( 1 == reminderExists ) {
            mReminder = new TaskReminder();
            String reminderType = in.readString();
            switch (TaskReminder.ReminderType.valueOf(reminderType)) {
                case Timed:
                    mReminder.setType(TaskReminder.ReminderType.valueOf(reminderType));
                    try {
                        mReminder.setTimedReminder(in.readString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            // TaskReminder reminder = new TaskReminder();
            // setReminder(in.readBundle());
        }
        */

    }

    public Task(Task task) {
        setTaskText(task.getTaskText());
        setLineNumber(task.getLineNumber());
        setType(task.getType());
        setParent(task.getParent());
        setAdapter(m_Adapter);
        setReminder(task.getReminder());
    }

    public Task(String s, int lineNumber, Tasks parent) {
        setTaskText(s);
        setLineNumber(lineNumber);
        m_Parent = parent;
        setType(m_Parent.getType());
    }

    public Task() {
        setTaskText("");
        setLineNumber(0);
        setType(TaskTypes.TaskListTypes.MainList);
    }

    public boolean isCompleted() {
        return mTaskText.matches("^\\s*[+]\\s*.*$");
    }

    public void setCompleted(boolean completed) {
        if (isCompleted() == completed) {
            return;
        }

        if (completed) {
            mTaskText = "+ " + mTaskText;
        } else {
            mTaskText = mTaskText.replaceFirst("^\\s*[+]\\s*", "");
        }

    }

    public int getLineNumber() {
        return m_LineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.m_LineNumber = lineNumber;
    }

    public String getTaskText() {
        return mTaskText.replace("\r", "");
    }

    public void setTaskText(String taskText) {
        mTaskText = taskText.replace("\r", "");
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
        /*
        if ( null == mReminder ){
            dest.writeByte((byte)0);
        } else {
            dest.writeByte((byte)1);
            switch (mReminder.getType()){
                case Timed:
                    dest.writeString(TaskReminder.ReminderType.Timed.toString());
                    dest.writeString(mReminder.getParams().getTriggerDateString());
                    break;
                case Location:
                    dest.writeString(TaskReminder.ReminderType.Location.toString());
                    break;
            }

        }
        */
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

    public TaskTypes.TaskListTypes getType() {
        return mType;
    }

    public void setType(TaskTypes.TaskListTypes type) {
        mType = type;
    }

    public TaskReminder getReminder() {
        if (mTaskText.matches(".*remind:\\[(.*)\\]")) {
            Pattern reminderPattern = Pattern.compile(".*?remind:\\[(.*?)\\].*", Pattern.CASE_INSENSITIVE);
            Matcher reminderMatcher = reminderPattern.matcher(getTaskText());
            if (reminderMatcher.matches()) {
                if (reminderMatcher.groupCount() < 1) {
                    mReminder = null;
                    return null;
                } else {
                    mReminder = new TaskReminder(TaskReminder.ReminderType.Timed, new ReminderParams(reminderMatcher.group(1)));
                    return mReminder;
                }
            } else {
                return null;
            }


        } else {
            return null;
        }

    }

    public void setReminder(final TaskReminder reminder) {
        // find reminder:[...] pattern
        if (mTaskText.matches(".*remind:\\[(.*)\\]")) {
            // already set, update
            if (null == reminder) {
                setTaskText(getTaskText().replaceAll("remind:\\[(.*)\\]", ""));
                return;
            } else {
                mTaskText = mTaskText.replaceFirst("remind:\\[(.*)\\]", formatReminder(reminder));
            }

        } else {
            mTaskText += " " + formatReminder(reminder);
        }

        mReminder = reminder;

    }

    private String formatReminder(final TaskReminder reminder) {
        switch (reminder.getType()) {
            case Timed:
                return String.format("remind:[%s]", reminder.getParams().getTriggerDateString());
            case Location:
                return "";
        }
        return "";
    }
}
