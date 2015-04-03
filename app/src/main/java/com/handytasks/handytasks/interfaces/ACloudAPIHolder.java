package com.handytasks.handytasks.interfaces;

import com.handytasks.handytasks.impl.IOLock;

/**
 * Created by avsho_000 on 3/28/2015.
 */
public abstract class ACloudAPIHolder {
    protected final static IOLock mIOLock = new IOLock();
    protected ICloudAPI mAPI;

    public ICloudAPI getAPI() {
        return mAPI;
    }

    public void setAPI(ICloudAPI api) {
        mAPI = api;
    }
}
