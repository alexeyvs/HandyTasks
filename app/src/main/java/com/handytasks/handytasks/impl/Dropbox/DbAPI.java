package com.handytasks.handytasks.impl.Dropbox;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import com.dropbox.sync.android.DbxAccountManager;
import com.handytasks.handytasks.R;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.IInitAPI;


/**
 * Created by avsho_000 on 3/12/2015.
 */
public class DbAPI implements ICloudAPI {
    private static final int REQUEST_LINK_TO_DBX = 500;
    private final DbxAccountManager m_DbxAcctMgr;
    private final String APP_KEY = "0f8doa1bhogj84t";
    private final String APP_SECRET = "um6onx22mk7g10l";
    private final Context mContext;
    private ICloudFS m_CloudFS;
    private boolean m_isReady = false;

    public DbAPI(ContextWrapper activity, Context context, IInitAPI callback, boolean allowStartOfNewActivities) {
        mContext = context;
        m_DbxAcctMgr = DbxAccountManager.getInstance(context, APP_KEY, APP_SECRET);
        if (!m_DbxAcctMgr.hasLinkedAccount()) {
            if (allowStartOfNewActivities) {
                m_DbxAcctMgr.startLink((Activity) activity, REQUEST_LINK_TO_DBX);
                callback.OnActionRequired(this);
            } else {
                callback.OnFailure(R.string.dropbox_not_linked);
            }
        } else {
            setReady();
            callback.OnSuccess(this);
        }
    }

    @Override
    public ICloudFS getFS() {
        return m_CloudFS;
    }

    @Override
    public ICloudFS setFS(ICloudFS fs) {
        m_CloudFS = fs;
        return m_CloudFS;
    }

    public boolean isReady() {
        return m_isReady;
    }

    @Override
    public Object getAccountManager() {
        return m_DbxAcctMgr;
    }

    @Override
    public int getNativeActivityCode() {
        return REQUEST_LINK_TO_DBX;
    }

    @Override
    public void setReady() {
        m_CloudFS = new DbFS(this);
        m_isReady = true;
    }

    @Override
    public void forceSync(IAsyncResult callback) {
        callback.OnSuccess(null);
    }

    @Override
    public void connect(IInitAPI callback) {
        setReady();
        callback.OnSuccess(this);
    }

}
