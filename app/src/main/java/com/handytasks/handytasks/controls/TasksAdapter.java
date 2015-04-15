package com.handytasks.handytasks.controls;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.activities.TaskList;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.impl.OnTaskListSwipeListener;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ITaskListChanged;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskTypes;
import com.handytasks.handytasks.model.Tasks;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;

//import com.terlici.dragndroplist.DragNDropAdapter;
//import com.terlici.dragndroplist.DragNDropListView;

/**
 * Created by avsho_000 on 3/20/2015.
 */
public class TasksAdapter extends ArrayAdapter<Task> implements ITaskListChanged, Swappable, UndoAdapter /*,DragNDropAdapter */ {
    private static final long INVALID_ID = -1;
    private final ArrayList<Task> mObjects;
    private final Tasks mTasks;
    private Context mContext = null;
    private String mFilter = null;
    private TaskList mActivity;

    public TasksAdapter(Context context, Tasks tasks) {
        super(tasks.getList());
        mTasks = tasks;
        mContext = context;
        mObjects = tasks.getList();
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Task getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mObjects.size()) {
            return INVALID_ID;
        }

        return getItem(position).getId();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @NonNull
    public Task remove(final int location) {
        Task result = mObjects.remove(location);
        notifyDataSetChanged();
        mTasks.Write();
        return result;
    }

    private int getAppearance(final int value) {
        switch (value) {
            case 1:
                return android.R.style.TextAppearance_Small;
            case 3:
                return android.R.style.TextAppearance_Large;
            default:
                return android.R.style.TextAppearance_Medium;
        }
    }

    private TaskTypes getTaskTypes() {
        return ((HTApplication) mActivity.getApplication()).getTaskTypes();
    }

    @Override
    public View getView(final int position, final View convertView1, final ViewGroup parent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String fontSizeVal = prefs.getString("task_list_font_size", "2");

        Task task = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (mFilter != null && !task.getTaskText().contains(mFilter)) {
            return inflater.inflate(R.layout.empty_view, parent, false);
        }

        View resultView = inflater.inflate(R.layout.item_taskview, parent, false);
        TextView textView = (TextView) resultView.findViewById(R.id.task_text);
        textView.setTextAppearance(getContext(), getAppearance(Integer.parseInt(fontSizeVal)));

        textView.setText(task.getTaskPlainText());
        task.setAdapter(this);
        final CheckBox currentItemCompleted = (CheckBox) resultView.findViewById(R.id.isCompleted);
        if (task.isCompleted()) {
            currentItemCompleted.setChecked(true);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            currentItemCompleted.setChecked(false);
        }

        ImageView reminderIcon = (ImageView) resultView.findViewById(R.id.reminder_set);
        if (null != task.getReminder()) {
            switch (task.getReminder().getType()) {
                case Timed: {
                    Drawable icon = mActivity.getResources().getDrawable(R.drawable.ic_notification_timed);
                    icon.mutate().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                    reminderIcon.setImageDrawable(icon);
                    break;
                }
                case Location: {
                    Drawable icon = mActivity.getResources().getDrawable(R.drawable.ic_notification_location);
                    icon.mutate().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                    reminderIcon.setImageDrawable(icon);
                    break;
                }
            }

            reminderIcon.setVisibility(View.VISIBLE);
        } else {
            reminderIcon.setVisibility(View.GONE);
        }

        currentItemCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = getItem(position);
                task.setCompleted(!task.isCompleted());
                if (task.isCompleted()) {
                    if (getTaskTypes().getTasksType(mTasks) == TaskTypes.TaskListTypes.MainList) {
                        getTaskTypes().archiveTask(task, new IAsyncResult() {
                            @Override
                            public void OnSuccess(String result) {

                            }

                            @Override
                            public void OnFailure(String result) {

                            }
                        }, true, mActivity.getApplicationContext());
                    }
                }

                task.notifyAdapterDataSetChanged();
                assert (task.getParent() != null);
                task.getParent().Write();
            }
        });

        resultView.findViewById(R.id.task_text).setOnTouchListener(new OnTaskListSwipeListener(mActivity.getApplicationContext(), task, mActivity));

        return resultView;
    }

    public void setActivity(TaskList activity) {
        this.mActivity = activity;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }

    @Override
    public void swapItems(final int startPosition, final int endPosition) {
        Task startTask = getItem(startPosition);
        Task newTask = getItem(endPosition);

        startTask.setLineNumber(endPosition);
        newTask.setLineNumber(startPosition);

        mObjects.set(startPosition, newTask);
        mObjects.set(endPosition, startTask);
    }

    public void commitSwap() {
        notifyDataSetChanged();
        mTasks.Write();
    }

    @NonNull
    @Override
    public View getUndoView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.undo_row, parent, false);
        }
        return view;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull final View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }

    @Override
    public void TaskListChanged(Tasks tasks) {
        notifyDataSetChanged();
    }
}