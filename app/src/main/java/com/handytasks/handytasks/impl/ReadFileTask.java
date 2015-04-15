package com.handytasks.handytasks.impl;

import android.os.AsyncTask;

import com.handytasks.handytasks.interfaces.IAsyncResult;

import java.util.Calendar;
import java.util.Date;

public class ReadFileTask extends AsyncTask<FileTaskParams, Void, Object> {
    // --Commented out by Inspection (4/15/2015 11:24 PM):private static final String TAG = "DbFS.ReadFileTask";
    private static final int GET_LATEST_VERSION_TIMEOUT = 10;
    private IAsyncResult m_Callback;

    @Override
    protected Object doInBackground(FileTaskParams... params) {
        try {
            params[0].getLock().waitForUnlock("WriteAsync");
            params[0].getLock().lock("ReadAsync");

            m_Callback = params[0].getCallback();
            if (params[0].getNeedLatest()) {
                Calendar c = Calendar.getInstance();
                Date currentDate = c.getTime();
                Date timeout = new Date();
                c.setTime(timeout);
                c.add(Calendar.SECOND, GET_LATEST_VERSION_TIMEOUT);
                timeout = c.getTime();

                while (!params[0].getFS().isLatest(params[0].getFilename())) {
                    if (currentDate.after(timeout)) {
                        break;
                    }
                    Thread.yield();
                }
            }

            return params[0].getFS().ReadFromFile(params[0].getCloudFile());
        } catch (Exception e) {
            return e;
        } finally {
            params[0].getLock().unlock();
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result instanceof String) {
            m_Callback.OnSuccess((String) result);
        } else {
            m_Callback.OnFailure(((Exception) result).getMessage());
        }
    }
}
