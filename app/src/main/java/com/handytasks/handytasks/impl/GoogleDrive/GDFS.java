package com.handytasks.handytasks.impl.GoogleDrive;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.handytasks.handytasks.impl.FileTaskParams;
import com.handytasks.handytasks.impl.ReadFileTask;
import com.handytasks.handytasks.impl.WriteFileTask;
import com.handytasks.handytasks.interfaces.ACloudAPIHolder;
import com.handytasks.handytasks.interfaces.IAsyncResult;
import com.handytasks.handytasks.interfaces.ICloudAPI;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFile;
import com.handytasks.handytasks.interfaces.ICloudWatcher;
import com.handytasks.handytasks.interfaces.IOpenFileResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Created by avsho_000 on 3/20/2015.
 */
public class GDFS extends ACloudAPIHolder implements ICloudFS {
    private static final String TAG = "GDFS";
    private static final String APPLICATION_FOLDER = "Handy Tasks";

    private final GoogleApiClient mClient;

    private DriveId mApplicationFolder;
    private final ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // TODO: handle
                        mApplicationFolder = null;
                        mInitializeFSCallback.OnFailure(result.toString());
                        return;
                    }

                    mApplicationFolder = result.getDriveFolder().getDriveId();
                    mInitializeFSCallback.OnSuccess(null);
                }
            };
    private IAsyncResult mInitializeFSCallback;
    // --Commented out by Inspection (4/15/2015 11:24 PM):private IAsyncResult mReadFileCallback;
    private Hashtable<ICloudFile, ICloudWatcher> mWatchers = new Hashtable<ICloudFile, ICloudWatcher>();

    public GDFS(ICloudAPI api) {
        mAPI = api;
        mClient = (GoogleApiClient) mAPI.getAccountManager();
    }

    public GoogleApiClient getClient() {
        return mClient;
    }

    @Override
    public ICloudFS initializeFS(IAsyncResult callback) {
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(APPLICATION_FOLDER).build();
        mInitializeFSCallback = callback;

        // list root childs and check if folder already exists
        Drive.DriveApi.getRootFolder(mClient).listChildren(mClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    mInitializeFSCallback.OnFailure(metadataBufferResult.getStatus().getStatusMessage());
                } else {
                    try {
                        for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            Log.d(TAG, metadata.getTitle());
                            if (APPLICATION_FOLDER.equals(metadata.getTitle()) &&
                                    !metadata.isTrashed()
                                    && metadata.getMimeType().equals("application/vnd.google-apps.folder")) {

                                // folder already exists
                                mInitializeFSCallback.OnSuccess(null);
                                mApplicationFolder = metadata.getDriveId();
                                return;
                            }
                        }
                    } finally {
                        metadataBufferResult.getMetadataBuffer().release();
                    }


                    Drive.DriveApi.getRootFolder(mClient).createFolder(
                            mClient, changeSet).setResultCallback(folderCreatedCallback);
                }
            }
        });


        return this;
    }

    @Override
    public ICloudWatcher getWatcher(ICloudFile file) {
        if (!mWatchers.containsKey(file)) {
            mWatchers.put(file, new GdWatcher(this, file));
        }
        return mWatchers.get(file);
    }


    @Override
    public void ReadTextFile(ICloudFile file, Boolean needLatest, IAsyncResult callback) {
        new ReadFileTask().execute(new FileTaskParams(this, file, needLatest, callback, mIOLock));
    }

    @Override
    public void WriteTextFile(ICloudFile file, String data, IAsyncResult callback) {
        new WriteFileTask().execute(new FileTaskParams(this, file, data, callback, mIOLock));
    }

    @Override
    public void CreateTextFile(final String filename, final IOpenFileResult callback) {
        Drive.DriveApi.newDriveContents(mClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(final DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            callback.OnSuccess(null);
                            return;
                        }

                        Drive.DriveApi.getFolder(mClient, mApplicationFolder).listChildren(mClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                            @Override
                            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                                if (!metadataBufferResult.getStatus().isSuccess()) {
                                    callback.OnFailure(metadataBufferResult.getStatus().getStatusMessage());
                                } else {
                                    try {
                                        for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                                            Log.d(TAG, metadata.getTitle());
                                            if (filename.equals(metadata.getTitle()) &&
                                                    !metadata.isTrashed()
                                                    && metadata.getMimeType().equals("text/plain")) {

                                                // folder already exists
                                                callback.OnSuccess(new GDFile(metadata.getDriveId(), filename));
                                                return;
                                            }
                                        }
                                    } finally {
                                        metadataBufferResult.getMetadataBuffer().release();
                                    }
                                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                            .setTitle(filename)
                                            .setMimeType("text/plain").build();

                                    Drive.DriveApi.getFolder(mClient, mApplicationFolder)
                                            .createFile(mClient, changeSet, result.getDriveContents())
                                            .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                                @Override
                                                public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                                    if (driveFileResult.getStatus().isSuccess()) {
                                                        callback.OnSuccess(new GDFile(driveFileResult.getDriveFile().getDriveId(), filename));
                                                    } else {
                                                        callback.OnFailure(driveFileResult.getStatus().getStatusMessage());
                                                    }
                                                }
                                            });
                                }
                            }
                        });


                    }
                });
    }

    @Override
    public boolean isLatest(String filename) {
        return true;
    }


    @Override
    public String ReadFromFile(ICloudFile file) throws IOException {
        String contents = null;
        DriveFile driveFile = Drive.DriveApi.getFile(mClient, (DriveId) file.getNativeDescriptior());
        DriveApi.DriveContentsResult driveContentsResult =
                driveFile.open(mClient, DriveFile.MODE_READ_ONLY, null).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            return null;
        }
        DriveContents driveContents = driveContentsResult.getDriveContents();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(driveContents.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\r\n");
            }
            contents = builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading from the stream", e);
        }

        driveContents.discard(mClient);
        return contents;
    }


    @Override
    public Boolean WriteToFile(ICloudFile file, String data) throws IOException {
        DriveFile driveFile = Drive.DriveApi.getFile(mClient, (DriveId) file.getNativeDescriptior());
        DriveApi.DriveContentsResult driveContentsResult =
                driveFile.open(mClient, DriveFile.MODE_WRITE_ONLY, null).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            return false;
        }
        DriveContents driveContents = driveContentsResult.getDriveContents();
        // StringWriter writer = new StringWriter();
        OutputStream output = driveContents.getOutputStream();
        output.write(data.getBytes());

        driveContents.commit(mClient, null).await();

        return true;
    }

}
