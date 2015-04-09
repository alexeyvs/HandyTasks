package com.handytasks.handytasks.controls;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
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
import com.terlici.dragndroplist.DragNDropAdapter;
import com.terlici.dragndroplist.DragNDropListView;

import java.util.ArrayList;

/**
 * Created by avsho_000 on 3/20/2015.
 */
public class TasksAdapter extends StableArrayAdapter<Task> implements ITaskListChanged, DragNDropAdapter {
    private final ArrayList<Task> mObjects;
    private final Tasks mTasks;
    private Context mContext = null;
    private String mFilter = null;
    private TaskList mActivity;

    public TasksAdapter(Context context, Tasks tasks) {
        super(context, R.layout.item_taskview, tasks.getList());
        mTasks = tasks;
        mObjects = tasks.getList();
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
    public int getPosition(Task item) {
        return mObjects.indexOf(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String fontSizeVal = prefs.getString("task_list_font_size", "2");
        try {
            Integer.parseInt(fontSizeVal);
        } catch (Exception e) {
            fontSizeVal = "2";
        }


        // Get the data item for this position
        Task task = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        // if (convertView == null) {
        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (mFilter != null && !task.getTaskText().contains(mFilter)) {
            return inflater.inflate(R.layout.empty_view, parent, false);
        }

        convertView = inflater.inflate(R.layout.item_taskview, parent, false);
        viewHolder.Text = (TextView) convertView.findViewById(R.id.TaskText);
        ((TextView) convertView.findViewById(R.id.TaskText)).setTextAppearance(getContext(), getAppearance(Integer.parseInt(fontSizeVal)));

        convertView.setTag(viewHolder);
        // } else {
        //    viewHolder = (ViewHolder) convertView.getTag();
        // }
        // Populate the data into the template view using the data object
        viewHolder.Text.setText(task.getTaskPlainText());
        task.setAdapter(this);
        final CheckBox currentItemCompleted = (CheckBox) convertView.findViewById(R.id.isCompleted);
        if (task.isCompleted()) {
            TextView currentItem = (TextView) convertView.findViewById(R.id.TaskText);

            currentItemCompleted.setChecked(true);
            currentItem.setPaintFlags(currentItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            currentItemCompleted.setChecked(false);
        }

        ImageView reminderIcon = (ImageView) convertView.findViewById(R.id.reminder_set);
        if (null != task.getReminder()) {
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

        convertView.findViewById(R.id.TaskText).setOnTouchListener(new OnTaskListSwipeListener(mActivity.getApplicationContext(), task, mActivity));

        return convertView;
    }

    public void setActivity(TaskList activity) {
        this.mActivity = activity;
    }

    @Override
    public void TaskListChanged() {
        notifyDataSetChanged();
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }

    @Override
    public int getDragHandler() {
        return R.id.drag_handle;
    }

    @Override
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    @Override
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        Task startTask = getItem(startPosition);
        Task newTask = getItem(endPosition);

        startTask.setLineNumber(endPosition);
        newTask.setLineNumber(startPosition);

        mObjects.set(startPosition, newTask);
        mObjects.set(endPosition, startTask);

        notifyDataSetChanged();
        newTask.getParent().Write();
    }

    // View lookup cache
    private static class ViewHolder {
        TextView Text;
    }
}