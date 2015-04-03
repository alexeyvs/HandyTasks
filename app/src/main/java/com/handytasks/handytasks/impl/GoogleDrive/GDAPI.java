package com.handytasks.handytasks.impl.GoogleDrive;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.IInitAPI;


/**
 * Created by avsho_000 on 3/20/2015.
 */
public class GDAPI implements ICloudAPI, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "GDAPI";
    private static final int REQUEST_CODE_RESOLUTION = 4;
    private final GoogleApiClient m_GoogleApiClient;
    private final Activity mActivity;
    private final Context mContext;
    private boolean m_Ready = false;
    private GDFS m_FS;
    private IInitAPI mConnectionCallback;

    public GDAPI(Activity activity, Context context, final IInitAPI callback, final boolean allowStartOfNewActivities) {
        mContext = context;
        mConnectionCallback = callback;
        mActivity = activity;
        m_GoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (!connectionResult.hasResolution()) {
                            mConnectionCallback.OnFailure(connectionResult);
                            return;
                        }

                        try {
                            if (allowStartOfNewActivities) {
                                connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
                                callback.OnActionRequired(GDAPI.this);
                            } else {
                                callback.OnFailure("Failed to resolve connection issue");
                            }

                        } catch (IntentSender.SendIntentException e) {
                            callback.OnFailure("Exception while starting resolution activity");
                        }
                    }
                })
                .build();

        m_GoogleApiClient.connect();
    }


    @Override
    public ICloudFS getFS() {
        return m_FS;
    }

    @Override
    public ICloudFS setFS(ICloudFS fs) {
        m_FS = (GDFS) fs;
        return m_FS;
    }

    @Override
    public boolean isReady() {
        return m_Ready;
    }

    @Override
    public Object getAccountManager() {
        return m_GoogleApiClient;
    }

    @Override
    public int getNativeActivityCode() {
        return REQUEST_CODE_RESOLUTION;
    }

    @Override
    public void setReady() {
        m_FS = new GDFS(this);
        m_Ready = true;
        mConnectionCallback.OnSuccess(this);
    }

    @Override
    public void forceSync(final IAsyncResult callback) {
        Drive.DriveApi.requestSync(m_GoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    callback.OnSuccess(null);
                } else {
                    callback.OnFailure(status.getStatusMessage());
                }
            }
        });
    }

    @Override
    public void connect(IInitAPI callback) {
        mConnectionCallback = callback;
        m_GoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "GoogleAPI Connected");
        setReady();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
