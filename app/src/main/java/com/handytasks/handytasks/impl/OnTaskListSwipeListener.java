package com.handytasks.handytasks.impl;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.handytasks.handytasks.activities.TaskList;
import com.handytasks.handytasks.activities.TaskView;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskTypes;


/**
 * Created by avsho_000 on 3/9/2015.
 */
public class OnTaskListSwipeListener implements View.OnTouchListener {

    private final Task mTask;
    private final TaskList mActivity;
    private GestureDetector gestureDetector = null;

    public OnTaskListSwipeListener(Context context, Task task, TaskList activity) {
        mTask = task;
        mActivity = activity;
        gestureDetector = new GestureDetector(context, new TaskListGestureListener(task, context, activity));
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    private TaskTypes getTaskTypes() {
        return ((HTApplication) mActivity.getApplication()).getTaskTypes();
    }

    void onSwipeRight() {
        mTask.setCompleted(true);
        mTask.getParent().setTask(mTask);
        if (getTaskTypes().getTasksType(mTask.getParent()) == TaskTypes.TaskListTypes.MainList) {
            getTaskTypes().archiveTask(mTask, new IAsyncResult() {
                @Override
                public void OnSuccess(String result) {

                }

                @Override
                public void OnFailure(String result) {

                }
            }, true, mActivity.getApplicationContext());
        }
    }

    void onSwipeLeft() {
        mTask.setCompleted(false);
        mTask.getParent().setTask(mTask);

    }

    void onSwipeTop() {
    }

    void onSwipeBottom() {
    }

    private final class TaskListGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private static final String TAG = "TaskListGestureListener";

        private Task mTask = null;

        private Context mContext = null;

        private TaskList mActivity = null;

        public TaskListGestureListener(Task task, Context context, TaskList activity) {
            mTask = task;
            mContext = context;
            mActivity = activity;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            /// return super.onDown(e);
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            Intent intent = new Intent(mContext, TaskView.class);
            intent.putExtra("requestCode", TaskView.REQUEST_CODE_EDIT_MODE);
            intent.putExtra("DATA", mTask);
            mActivity.startActivityForResult(intent, TaskView.REQUEST_CODE_EDIT_MODE);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            Log.d(TAG, "Long press");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return true;
        }
    }
}
