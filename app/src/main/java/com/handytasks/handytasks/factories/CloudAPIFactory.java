package com.handytasks.handytasks.factories;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.handytasks.handytasks.impl.Dropbox.DbAPI;
import com.handytasks.handytasks.impl.GoogleDrive.GDAPI;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.IInitAPI;


/**
 * Created by avsho_000 on 3/12/2015.
 */
public class CloudAPIFactory {
    public static boolean isCurrentlySetup(Activity parent, ICloudAPI current) {
        if (null == current) {
            return false;
        }
        SharedPreferences prefs = parent.getSharedPreferences(
                "com.handytasks.handytasks", Context.MODE_PRIVATE);
        String type = prefs.getString("cloud_type", "Dropbox");
        switch (type) {
            case "Dropbox":
                return current.getClass() == DbAPI.class;
            case "Google Drive":
                return current.getClass() == GDAPI.class;
            default:
                return false;
        }
    }


    public static ICloudAPI generateAPI(ContextWrapper parent, Context context, IInitAPI callback, boolean allowStartOfNewActivities) {
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences prefs = parent.getSharedPreferences("com.handytasks.handytasks", Context.MODE_MULTI_PROCESS);
        String type = prefs.getString("cloud_type", "");


        switch (type) {
            case "Dropbox":
                return new DbAPI(parent, context, callback, allowStartOfNewActivities);
            case "Google Drive":
                return new GDAPI(parent, context, callback, allowStartOfNewActivities);
        }
        callback.OnFailure("Unsupported cloud provider");
        return null;
    }

}
