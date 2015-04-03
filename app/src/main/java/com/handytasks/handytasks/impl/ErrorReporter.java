package com.handytasks.handytasks.impl;

import android.app.Activity;
import android.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.handytasks.handytasks.R;


/**
 * Created by avsho_000 on 3/24/2015.
 */
public class ErrorReporter {
    public static void ReportError(Class<?> cls, Activity activity, Object data) {

        if (data.getClass() == ConnectionResult.class) {
            ReportGoogleAPIError(activity, (ConnectionResult) data);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
                .setTitle(activity.getString(R.string.error_interacting_with_cloud))
                .setMessage(activity.getString(R.string.error_details_caption) + data.toString())
                .setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static void ReportGoogleAPIError(Activity activity, ConnectionResult connectionResult) {
        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
    }
}
