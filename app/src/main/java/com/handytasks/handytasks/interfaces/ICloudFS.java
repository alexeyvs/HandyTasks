package com.handytasks.handytasks.interfaces;


import java.io.IOException;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public interface ICloudFS {
    ICloudWatcher getWatcher(ICloudFile file);

    public ICloudFS initializeFS(IAsyncResult callback);

    public void ReadTextFile(ICloudFile filename, Boolean needLatest, IAsyncResult callback);

    public void WriteTextFile(ICloudFile filename, String data, IAsyncResult callback);

    public String ReadFromFile(ICloudFile file) throws IOException;

    public Boolean WriteToFile(ICloudFile file, String data) throws IOException;

    public void CreateTextFile(String filename, IOpenFileResult callback);

    public boolean isLatest(String filename);

    public ICloudAPI getAPI();
}
