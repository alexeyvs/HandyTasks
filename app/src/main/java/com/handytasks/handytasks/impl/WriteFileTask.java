package com.handytasks.handytasks.impl;

import android.os.AsyncTask;

import com.handytasks.handytasks.interfaces.IAsyncResult;

public class WriteFileTask extends AsyncTask<FileTaskParams, Void, Object> {
    private static final String TAG = "DbFS.WriteFileTask";
    private IAsyncResult m_Callback;

    @Override
    protected Object doInBackground(FileTaskParams... params) {
        try {
            params[0].getLock().waitForUnlock("WriteAsync");
            params[0].getLock().lock("WriteAsync");
            m_Callback = params[0].getCallback();
            if (params[0].getFS().WriteToFile(params[0].getCloudFile(), params[0].getData())) {
                return "File written";
            } else {
                return new Exception("Failed to get contents of Google Drive");
            }

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
