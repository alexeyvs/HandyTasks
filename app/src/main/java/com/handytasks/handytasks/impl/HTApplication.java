package com.handytasks.handytasks.impl;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.handytasks.handytasks.factories.CloudAPIFactory;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFSStorage;
import com.handytasks.handytasks.interfaces.IInitAPI;
import com.handytasks.handytasks.model.TaskTypes;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;


/**
 * Created by avsho_000 on 3/12/2015.
 */
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://handytasks.iriscouch.com/acra-myapp/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "htreporter",
        formUriBasicAuthPassword = "htreporter"
)
public class HTApplication extends Application implements ICloudFSStorage {

    // --Commented out by Inspection (4/15/2015 11:24 PM):public static final int OPEN_TASK = 333;
    private final TaskTypes mTaskTypes = new TaskTypes(this);
    private ICloudAPI m_CloudAPI;
    // --Commented out by Inspection (4/15/2015 11:24 PM):private ICloudWatcher m_CloudWatcher;
    private boolean mConnectionSetupInProgress;

    public void setAPI(ICloudAPI api) {
        m_CloudAPI = api;
    }

    public void forceSync(IAsyncResult callback) {
        if (null != m_CloudAPI) {
            m_CloudAPI.forceSync(callback);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

    public void generateAPI(ContextWrapper activity, Context context, IInitAPI callback, boolean allowStartOfNewActivities) {
        CloudAPIFactory.generateAPI(activity, context, callback, allowStartOfNewActivities);
    }

    public ICloudAPI getCloudAPI() {
        return m_CloudAPI;
    }

    public void resetCloudAPI() {
        m_CloudAPI = null;
        mTaskTypes.clear();
    }

    public boolean isAPIInitialized() {
        return (m_CloudAPI != null && m_CloudAPI.isReady());
    }

    @Override
    public ICloudFS getFS() {
        return m_CloudAPI.getFS();
    }

    /* @Override
     public ICloudFS setFS(ICloudFS fs) {
         m_CloudAPI.setFS(fs);
         return fs;
     }
 */
    public TaskTypes getTaskTypes() {
        return mTaskTypes;
    }

    public boolean isConnectionSetupInProgress() {
        return mConnectionSetupInProgress;
    }

    public void setConnectionSetupInProgress(boolean value) {
        mConnectionSetupInProgress = value;
    }
}
