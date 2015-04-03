package com.handytasks.handytasks.interfaces;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public interface ICloudWatcher {
    public void StartWatch(IFSChangeHandler handler);

    public void StopWatch();
}
