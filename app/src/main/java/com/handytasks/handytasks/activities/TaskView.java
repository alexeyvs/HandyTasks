package com.handytasks.handytasks.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.handytasks.handytasks.R;
import com.handytasks.handytasks.interfaces.IReminderParamsUpdated;
import com.handytasks.handytasks.model.ReminderLocationData;
import com.handytasks.handytasks.model.ReminderParams;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskReminder;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;

public class TaskView extends FragmentActivity implements DatePickerDialog.OnDateSetListener, com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener, IReminderParamsUpdated {

    public static final int REQUEST_CODE_ADD_MODE = 100;
    public static final int REQUEST_CODE_EDIT_MODE = 101;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_ARCHIVE = 3;
    public static final int PLACE_PICKER_REQUEST = 1;
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final String TAG = "TaskView activity";
    private int mRequestCode;
    private Task m_TaskItem;
    private TextView taskText = null;
    private CheckBox mTaskCompleted = null;
    private TextView mDateSelector;
    private TextView mTimeSelector;
    private ReminderParams mReminderParams;
    private TaskReminder.ReminderType mReminderType;
    private TextView mReminderTypeSelect;
    private TextView mLocationSelector;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("requestCode", mRequestCode);
        outState.putParcelable("taskItem", m_TaskItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);

        taskText = (TextView) findViewById(R.id.editText);

        if (savedInstanceState != null) {
            mRequestCode = savedInstanceState.getInt("requestCode");
            m_TaskItem = savedInstanceState.getParcelable("taskItem");
        } else {
            Bundle data = getIntent().getExtras();
            mRequestCode = data.getInt("requestCode");
            switch (mRequestCode) {
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
        }

        mTaskCompleted = (CheckBox) findViewById(R.id.taskCompleted);
        mTaskCompleted.setChecked(m_TaskItem.isCompleted());

        mReminderTypeSelect = ((TextView) findViewById(R.id.reminder_type_select));
        mDateSelector = ((TextView) findViewById(R.id.reminder_timed_date));
        mTimeSelector = ((TextView) findViewById(R.id.reminder_timed_time));
        mLocationSelector = ((TextView) findViewById(R.id.reminder_select_location));

        // update reminder controls
        if (null != m_TaskItem.getReminder()) {
            onToggleReminder(null);
        }

    }

    private boolean isToday(Date date) {
        return com.handytasks.handytasks.utils.DateUtils.isToday(date);
    }

    private boolean isTomorrow(Date date) {
        return com.handytasks.handytasks.utils.DateUtils.isWithinDaysFuture(date, 1);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        if (v == mTimeSelector) {
            inflater.inflate(R.menu.menu_context_select_time, menu);
        } else if (v == mDateSelector) {
            inflater.inflate(R.menu.menu_context_select_date, menu);
        } else if (v == mReminderTypeSelect) {
            inflater.inflate(R.menu.menu_context_select_reminder_type, menu);
        }
    }

    public void onToggleReminder(View view) {
        LinearLayout layout = ((LinearLayout) findViewById(R.id.reminder_params_layout));
        if (layout.getVisibility() == View.VISIBLE) {
            m_TaskItem.setReminder(null);
            UpdateTaskText();
            layout.setVisibility(View.GONE);
            return;
        } else {
            layout.setVisibility(View.VISIBLE);
            Animation anima = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            layout.startAnimation(anima);

        }

        if (null == m_TaskItem.getReminder() && mReminderParams != null) {
            // reuse cached values
            m_TaskItem.setReminder(new TaskReminder(mReminderType, mReminderParams));
        } else if (m_TaskItem.getReminder() != null) {
            // get default values from task if any
            mReminderParams = m_TaskItem.getReminder().getParams();
            mReminderType = m_TaskItem.getReminder().getType();
        } else if (m_TaskItem.getReminder() == null) {
            // set defaults
            mReminderParams = new ReminderParams(new Date());
            mReminderType = TaskReminder.ReminderType.Timed;
        }

        mReminderParams.setChangesHandler(this);
        OnUpdateReminderParams(mReminderParams);
        initReminderControls();

    }

    private void initReminderControls() {
        if (mReminderType == TaskReminder.ReminderType.Timed) {
            mReminderTypeSelect.setText("Remind on time");
            ((LinearLayout) findViewById(R.id.timed_params)).setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.location_params)).setVisibility(View.GONE);

            // set dates
            if (isToday(mReminderParams.getTriggerDate())) {
                mDateSelector.setText("Today");
            } else if (isTomorrow(mReminderParams.getTriggerDate())) {
                mDateSelector.setText("Tomorrow");
            } else {
                mDateSelector.setText(mReminderParams.getTriggerDateUFString(getApplicationContext(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE));
            }

            // set time
            mTimeSelector.setText(mReminderParams.getTriggerDateUFString(getApplicationContext(), DateUtils.FORMAT_SHOW_TIME));

        } else if (mReminderType == TaskReminder.ReminderType.Location) {
            mReminderTypeSelect.setText("Remind near location");
            ((LinearLayout) findViewById(R.id.timed_params)).setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.location_params)).setVisibility(View.VISIBLE);
            mLocationSelector.setText(mReminderParams.getPlaceInfo());
        }
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
        taskText.setText(m_TaskItem.getTaskPlainText());
    }

    public void onTaskCompleted(View view) {
        Log.d(TAG, "onTaskCompleted");
        m_TaskItem.setCompleted(mTaskCompleted.isChecked());
        UpdateTaskText();
    }

    private void setToday() {
        mDateSelector.setText("Today");
        Calendar c = Calendar.getInstance(),
                cNow = Calendar.getInstance();
        c.setTime(mReminderParams.getTriggerDate());
        Date now = new Date();
        cNow.setTime(now);
        c.set(Calendar.YEAR, cNow.get(Calendar.YEAR));
        c.set(Calendar.MONTH, cNow.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, cNow.get(Calendar.DAY_OF_MONTH));
        mReminderParams.setTriggerDate(c.getTime());
    }

    private void setTomorrow() {
        mDateSelector.setText("Tomorrow");
        Calendar c = Calendar.getInstance(),
                cNow = Calendar.getInstance();
        c.setTime(mReminderParams.getTriggerDate());
        Date now = new Date();
        cNow.setTime(now);
        c.set(Calendar.YEAR, cNow.get(Calendar.YEAR));
        c.set(Calendar.MONTH, cNow.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, cNow.get(Calendar.DAY_OF_MONTH));
        c.add(Calendar.HOUR, 24);
        mReminderParams.setTriggerDate(c.getTime());
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Calendar calendar = Calendar.getInstance();
        switch (item.getItemId()) {
            case R.id.action_select_today: {
                setToday();
                break;
            }
            case R.id.action_select_tomorrow: {
                setTomorrow();
                break;
            }
            case R.id.action_select_pick_date:
                final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(2015, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                break;
            case R.id.action_select_pick_time:
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
                timePickerDialog.setVibrate(false);
                timePickerDialog.setCloseOnSingleTapMinute(false);
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
                break;
            case R.id.reminder_type_timed:
                if (mReminderType != TaskReminder.ReminderType.Timed) {
                    mReminderParams = new ReminderParams(new Date());
                    mReminderParams.setChangesHandler(this);
                }
                mReminderType = TaskReminder.ReminderType.Timed;
                initReminderControls();
                OnUpdateReminderParams(mReminderParams);
                break;
            case R.id.reminder_type_location:
                if (mReminderType != TaskReminder.ReminderType.Location) {
                    mReminderParams = new ReminderParams(ReminderLocationData.getDefault());
                    mReminderParams.setChangesHandler(this);
                }
                mReminderType = TaskReminder.ReminderType.Location;
                initReminderControls();
                OnUpdateReminderParams(mReminderParams);
                break;
        }
        return super.onContextItemSelected(item);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.in, R.anim.out);
    }


    public void setUnderline(TextView textView, boolean underline) {
        if (underline) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }
    }

    public void onPickLocation(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Context context = getApplicationContext();
        try {
            startActivityForResult(builder.build(getApplicationContext()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        // Intent intent = new Intent(getApplicationContext(), PickLocationActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(data, this);
                mReminderParams.setPlace(place);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                mLocationSelector.setText(mReminderParams.getPlaceInfo());
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showPopupMenu(View v) {

        PopupMenu popupMenu = new PopupMenu(TaskView.this, v);
        if (v == mTimeSelector) {
            popupMenu.getMenuInflater().inflate(R.menu.menu_context_select_time, popupMenu.getMenu());
        } else if (v == mDateSelector) {
            popupMenu.getMenuInflater().inflate(R.menu.menu_context_select_date, popupMenu.getMenu());
        } else if (v == mReminderTypeSelect) {
            popupMenu.getMenuInflater().inflate(R.menu.menu_context_select_reminder_type, popupMenu.getMenu());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final Calendar calendar = Calendar.getInstance();
                switch (item.getItemId()) {
                    case R.id.action_select_today: {
                        setToday();
                        break;
                    }
                    case R.id.action_select_tomorrow: {
                        setTomorrow();
                        break;
                    }
                    case R.id.action_select_pick_date:
                        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(TaskView.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                        datePickerDialog.setVibrate(false);
                        datePickerDialog.setYearRange(2015, 2028);
                        datePickerDialog.setCloseOnSingleTapDay(false);
                        datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                        break;
                    case R.id.action_select_pick_time:
                        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(TaskView.this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
                        timePickerDialog.setVibrate(false);
                        timePickerDialog.setCloseOnSingleTapMinute(false);
                        timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
                        break;
                    case R.id.reminder_type_timed:
                        if (mReminderType != TaskReminder.ReminderType.Timed) {
                            mReminderParams = new ReminderParams(new Date());
                            mReminderParams.setChangesHandler(TaskView.this);
                        }
                        mReminderType = TaskReminder.ReminderType.Timed;
                        initReminderControls();
                        OnUpdateReminderParams(mReminderParams);
                        break;
                    case R.id.reminder_type_location:
                        if (mReminderType != TaskReminder.ReminderType.Location) {
                            mReminderParams = new ReminderParams(ReminderLocationData.getDefault());
                            mReminderParams.setChangesHandler(TaskView.this);
                        }
                        mReminderType = TaskReminder.ReminderType.Location;
                        initReminderControls();
                        OnUpdateReminderParams(mReminderParams);
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }

    public void onSelectDateClick(View view) {
        showPopupMenu(view);
    }

    public void onSelectTimeClick(View view) {
        showPopupMenu(view);
    }

    public void onSelectReminderType(View view) {
        showPopupMenu(view);
        // view.showContextMenu();
    }

    public void setTimedReminder(View view) {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
        datePickerDialog.setVibrate(true);
        datePickerDialog.setYearRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + 10);
        datePickerDialog.setCloseOnSingleTapDay(true);
        datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);

    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(mReminderParams.getTriggerDate());
        c.set(year, month, day);
        mReminderParams.setTriggerDate(c.getTime());
        mDateSelector.setText(mReminderParams.getTriggerDateUFString(getApplicationContext(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE));
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTime(mReminderParams.getTriggerDate());
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        mReminderParams.setTriggerDate(c.getTime());
        mTimeSelector.setText(mReminderParams.getTriggerDateUFString(getApplicationContext(), DateUtils.FORMAT_SHOW_TIME));
    }

    @Override
    public void OnUpdateReminderParams(ReminderParams params) {
        m_TaskItem.setReminder(new TaskReminder(mReminderType, mReminderParams));
        UpdateTaskText();
    }
}
