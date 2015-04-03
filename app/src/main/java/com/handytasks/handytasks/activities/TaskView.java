package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.model.ReminderParams;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskReminder;

import java.util.Calendar;
import java.util.Date;

public class TaskView extends Activity {

    public static final int REQUEST_CODE_ADD_MODE = 100;
    public static final int REQUEST_CODE_EDIT_MODE = 101;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_ARCHIVE = 3;
    private static final String TAG = "TaskView activity";

    private int m_RequestCode;

    private Task m_TaskItem;

    private TextView taskText = null;

    private CheckBox m_TaskCompleted = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);

        taskText = (TextView) findViewById(R.id.editText);
        m_TaskCompleted = (CheckBox) findViewById(R.id.taskCompleted);

        Bundle data = getIntent().getExtras();
        m_RequestCode = data.getInt("requestCode");

        switch (m_RequestCode) {
            case REQUEST_CODE_ADD_MODE: {
                m_TaskItem = new Task();

                m_TaskItem.setTaskText("");
                UpdateTaskText();
                break;
            }

            case REQUEST_CODE_EDIT_MODE: {
                m_TaskItem = data.getParcelable("DATA");
                UpdateTaskText();
                break;
            }
        }

        m_TaskCompleted.setChecked(m_TaskItem.isCompleted());

        taskText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                m_TaskItem.setTaskText(s.toString());
                m_TaskCompleted.setChecked(m_TaskItem.isCompleted());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ((Button) findViewById(R.id.set_reminder)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.SECOND, 120);
                ReminderParams params = new ReminderParams(c.getTime());
                m_TaskItem.setReminder(new TaskReminder(TaskReminder.ReminderType.Timed, params));
                taskText.setText(m_TaskItem.getTaskText());
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_view, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        ReturnData(ACTION_EDIT);
        super.onBackPressed();
    }

    private void ReturnData(int action) {
        Intent intent = new Intent();
        intent.putExtra("DATA", m_TaskItem);
        intent.putExtra("ACTION", action);
        setResult(Activity.RESULT_OK, intent);
    }

    private void UpdateTaskText() {
        TextView textView = (TextView) findViewById(R.id.editText);
        textView.setText(m_TaskItem.getTaskText());
    }

    public void onTaskCompleted(View view) {
        Log.d(TAG, "onTaskCompleted");
        m_TaskItem.setCompleted(m_TaskCompleted.isChecked());
        taskText.setText(m_TaskItem.getTaskText());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ReturnData(ACTION_EDIT);
                super.onBackPressed();
                return true;
            case R.id.action_delete:
                ReturnData(ACTION_DELETE);
                super.onBackPressed();
                return true;
            case R.id.action_archive:
                ReturnData(ACTION_ARCHIVE);
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
