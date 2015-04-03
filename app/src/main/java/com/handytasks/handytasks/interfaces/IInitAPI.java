package com.handytasks.handytasks.interfaces;

/**
 * Created by avsho_000 on 3/24/2015.
 */
public interface IInitAPI {
    public void OnSuccess(ICloudAPI result);

    public void OnActionRequired(Object action);

    public void OnFailure(Object result);
}
