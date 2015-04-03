package com.handytasks.handytasks.interfaces;

/**
 * Created by avsho_000 on 3/22/2015.
 */
public interface IAsyncResult {
    void OnSuccess(String result);

    void OnFailure(String result);
}

