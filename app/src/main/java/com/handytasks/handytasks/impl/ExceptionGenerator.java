package com.handytasks.handytasks.impl;

import android.util.Log;

/**
 * Created by avsho_000 on 3/12/2015.
 */
class ExceptionGenerator {
// --Commented out by Inspection START (4/15/2015 11:24 PM):
//    public static void raise(String message, Throwable cause) throws Exception {
//        raise(message, "ExceptionGenerator", cause);
//    }
// --Commented out by Inspection STOP (4/15/2015 11:24 PM)

    private static void raise(String message, String tag, Throwable cause) throws Exception {
        Exception newEx = new Exception(message);
        newEx.initCause(cause);
        Log.e(tag, message, newEx);
        throw newEx;
    }
}
