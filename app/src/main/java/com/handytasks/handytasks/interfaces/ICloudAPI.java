package com.handytasks.handytasks.interfaces;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public interface ICloudAPI extends ICloudFSStorage {
    public boolean isReady();

    public Object getAccountManager();

    public int getNativeActivityCode();

    void setReady();

    public void forceSync(final IAsyncResult callback);

    public void connect(IInitAPI callback);
}
