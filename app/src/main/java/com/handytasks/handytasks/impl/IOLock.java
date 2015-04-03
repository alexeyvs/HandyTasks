package com.handytasks.handytasks.impl;

import android.util.Log;

/**
 * Created by avsho_000 on 3/31/2015.
 */
public class IOLock {
    private static final String TAG = "IOLock";
    private boolean mIsLocked;
    private String mComment = "none";

    public IOLock() {
        mIsLocked = false;
    }

    public void lock(String comment) {
        mComment = comment;
        Log.d(TAG, "locked " + mComment);
        mIsLocked = true;
    }

    public void unlock() {
        Log.d(TAG, "unlocked " + mComment);
        mIsLocked = false;
    }

    public void waitForUnlock(String comment) {
        Log.d(TAG, "waiting " + comment);
        while (true) {
            if (!mIsLocked) {
                Log.d(TAG, "waiting completed for " + comment);
                return;
            }
            Thread.yield();
        }
    }
}