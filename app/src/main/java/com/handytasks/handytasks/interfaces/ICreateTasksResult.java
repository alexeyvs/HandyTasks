package com.handytasks.handytasks.interfaces;

import com.handytasks.handytasks.model.Tasks;

/**
 * Created by avsho_000 on 3/24/2015.
 */
public interface ICreateTasksResult {
    public void OnSuccess(Tasks result, int title);

    public void OnFailure(String result);
}
