package com.handytasks.handytasks.impl;

import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFile;

/**
 * Created by avsho_000 on 3/24/2015.
 */
public class FileTaskParams {
    private final IOLock mIOLock;
    private IAsyncResult m_Callback;
    private ICloudFS mCloudFS;
    private ICloudFile mCloudFile;
    private Boolean m_needLatest;
    private String m_data;

    public FileTaskParams(ICloudFS fs, ICloudFile file, Boolean needLatest, IAsyncResult callback, IOLock lock) {
        mCloudFS = fs;
        m_Callback = callback;
        mCloudFile = file;
        m_needLatest = needLatest;
        mIOLock = lock;
    }

    public FileTaskParams(ICloudFS fs, ICloudFile file, String data, IAsyncResult callback, IOLock lock) {
        mCloudFS = fs;
        m_Callback = callback;
        mCloudFile = file;
        m_data = data;
        mIOLock = lock;
    }

    public IOLock getLock() {
        return mIOLock;
    }

    public IAsyncResult getCallback() {
        return m_Callback;
    }

    public ICloudFS getFS() {
        return mCloudFS;
    }

    public String getFilename() {
        return mCloudFile.getFilename();
    }

    public boolean getNeedLatest() {
        return m_needLatest;
    }

    public String getData() {
        return m_data;
    }

    public ICloudFile getCloudFile() {
        return mCloudFile;
    }
}

