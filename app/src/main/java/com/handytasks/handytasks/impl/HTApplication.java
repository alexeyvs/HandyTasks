package com.handytasks.handytasks.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.handytasks.handytasks.factories.CloudAPIFactory;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFSStorage;
import com.handytasks.handytasks.interfaces.ICloudWatcher;
import com.handytasks.handytasks.interfaces.IInitAPI;
import com.handytasks.handytasks.model.TaskTypes;


/**
 * Created by avsho_000 on 3/12/2015.
 */
public class HTApplication extends Application implements ICloudFSStorage {

    private final TaskTypes mTaskTypes = new TaskTypes(this);
    private ICloudAPI m_CloudAPI;
    private ICloudWatcher m_CloudWatcher;

    public void setAPI(ICloudAPI api) {
        m_CloudAPI = api;
    }

    public void forceSync(IAsyncResult callback) {
        if (null != m_CloudAPI) {
            m_CloudAPI.forceSync(callback);
        }
    }

    public void generateAPI(Activity activity, Context context, IInitAPI callback, boolean allowStartOfNewActivities) {
        CloudAPIFactory.generateAPI(activity, context, callback, allowStartOfNewActivities);
    }


    public ICloudAPI getCloudAPI() {
        return m_CloudAPI;
    }

    public void resetCloudAPI() {
        m_CloudAPI = null;
    }

    public boolean isAPIInitialized() {
        return (m_CloudAPI != null && m_CloudAPI.isReady());
    }

    @Override
    public ICloudFS getFS() {
        return m_CloudAPI.getFS();
    }

    @Override
    public ICloudFS setFS(ICloudFS fs) {
        m_CloudAPI.setFS(fs);
        return fs;
    }


    public TaskTypes getTaskTypes() {
        return mTaskTypes;
    }

}
