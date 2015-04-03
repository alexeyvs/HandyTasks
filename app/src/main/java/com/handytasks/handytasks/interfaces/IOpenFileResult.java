package com.handytasks.handytasks.interfaces;

public interface IOpenFileResult {
    void OnSuccess(ICloudFile result);

    void OnFailure(String result);
}
