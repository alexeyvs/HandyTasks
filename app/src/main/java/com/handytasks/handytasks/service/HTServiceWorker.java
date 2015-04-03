package com.handytasks.handytasks.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.handytasks.handytasks.R;
import com.handytasks.handytasks.activities.MainActivity;
import com.handytasks.handytasks.impl.HTApplication;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICreateTasksResult;
import com.handytasks.handytasks.interfaces.IInitAPI;
import com.handytasks.handytasks.model.ReminderParams;
import com.handytasks.handytasks.model.Task;
import com.handytasks.handytasks.model.TaskReminder;
import com.handytasks.handytasks.model.TaskTypes;
import com.handytasks.handytasks.model.Tasks;

import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.media.RingtoneManager.getRingtone;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class HTServiceWorker implements Runnable {
    private static final String TAG = "HTServiceWorker";
    private final HTService mService;
    private boolean mStopSignal = false;
    private boolean mStopped;
    private ScheduledExecutorService mScheduledPool = Executors.newScheduledThreadPool(1);
    private Hashtable<Task, ScheduledFuture<?>> mSchedules = new Hashtable<>();

    public HTServiceWorker(HTService service) {
        mService = service;
    }

    public void signalStop() {
        mStopSignal = true;
    }

    private void sendNotification(String title, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mService.getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mService.getApplicationContext(), MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mService.getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mService.getSystemService(mService.getApplicationContext().NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void processSchedules(Tasks tasks) {
        synchronized (tasks) {
            for (final Task task : tasks.getList()) {
                if (task.isCompleted()) {
                    continue;
                }

                TaskReminder reminder = task.getReminder();
                if (null != reminder) {
                    if (reminder.getType() == TaskReminder.ReminderType.Timed) {
                        ReminderParams params = reminder.getParams();
                        Date triggerDate = params.getTriggerDate();
                        Date currentDate = new Date();
                        long diffInMS = triggerDate.getTime() - currentDate.getTime();
                        long diffSeconds = TimeUnit.SECONDS.convert(diffInMS, TimeUnit.MILLISECONDS);
                        if (diffSeconds < 0) {
                            // reset reminder if we are too late
                            remind(task);
                            continue;
                        }
                        // get current schedule if any
                        if (mSchedules.containsKey(task)) {
                            mSchedules.get(task).cancel(true);
                        }

                        ScheduledFuture<?> beeperHandle =
                                mScheduledPool.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        remind(task);
                                    }
                                }, diffSeconds, TimeUnit.SECONDS);
                        mSchedules.put(task, beeperHandle);

                    }
                }
            }
        }
    }

    private void remind(Task task) {
        Log.d(TAG, "Reminder for task " + task.getTaskPlainText() + " sent");
        sendNotification("Task reminder", task.getTaskPlainText());
        playRingtoneIfRequired();
        task.setReminder(null);
        mService.sendMessageToUI(1);
        // task.getParent().requestUpdateFromService();
    }


    @Override
    public void run() {
        int i = 0;
        while (!mStopSignal) {
            if (((HTApplication) mService.getApplication()).isAPIInitialized()) {
                ///sendNotification("API status", "ready");

                ((HTApplication) mService.getApplication()).getTaskTypes().getTasks(false, TaskTypes.TaskListTypes.MainList, new ICreateTasksResult() {
                    @Override
                    public void OnSuccess(Tasks result, int title) {
                        processSchedules(result);
                    }

                    @Override
                    public void OnFailure(String result) {

                    }
                });

                try {
                    Thread.sleep(5000, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                //sendNotification("API status", "not ready");
                ((HTApplication) mService.getApplication()).generateAPI(null, mService.getApplicationContext(), new IInitAPI() {
                    @Override
                    public void OnSuccess(ICloudAPI result) {
                        ((HTApplication) mService.getApplication()).setAPI(result);
                        sendNotification("API status", "now ready");
                    }

                    @Override
                    public void OnActionRequired(Object action) {
                        //sendNotification("API status", "action required");
                    }

                    @Override
                    public void OnFailure(Object result) {
                        sendNotification("API status", "error: " + result.toString());
                    }
                }, false);
            }

            try {
                Thread.sleep(5000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }
        mStopped = true;
    }

    private void playRingtoneIfRequired() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mService.getApplicationContext());
        Boolean enableNotifications = prefs.getBoolean("reminder_notify", false);
        if (enableNotifications) {
            String ringTone = prefs.getString("notification_ringtone", "default ringtone");
            Uri notification = Uri.parse(ringTone);
            Ringtone r = getRingtone(mService.getApplicationContext(), notification);
            r.play();
        }
    }

    public boolean isStopped() {
        return mStopped;
    }
}