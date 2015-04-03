package com.handytasks.handytasks.impl;

import android.util.Log;

/**
 * Created by avsho_000 on 3/12/2015.
 */
class ExceptionGenerator {
    public static void raise(String message, Throwable cause) throws Exception {
        raise(message, "ExceptionGenerator", cause);
    }

    private static void raise(String message, String tag, Throwable cause) throws Exception {
        Exception newEx = new Exception(message);
        newEx.initCause(cause);
        Log.e(tag, message, newEx);
        throw newEx;
    }
}
