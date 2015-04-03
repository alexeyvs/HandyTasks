package com.handytasks.handytasks.impl.GoogleDrive;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFile;
import com.handytasks.handytasks.interfaces.ICloudWatcher;
import com.handytasks.handytasks.interfaces.IFSChangeHandler;


/**
 * Created by avsho_000 on 3/24/2015.
 */
public class GdWatcher implements ICloudWatcher {

    private final GDFS mFS;
    private ICloudFile mFile;
    private IFSChangeHandler mFSChangeHandler;
    private ChangeListener mListener = new ChangeListener() {
        @Override
        public void onChange(ChangeEvent changeEvent) {
            if (changeEvent.hasContentChanged()) {
                mFSChangeHandler.PathChanged(mFile.getFilename());
            }
        }

        ;
    };


    public GdWatcher(ICloudFS fs, ICloudFile file) {
        mFile = file;
        mFS = (GDFS) fs;
    }

    @Override
    public void StartWatch(final IFSChangeHandler handler) {
        mFSChangeHandler = handler;
        DriveFile file = Drive.DriveApi.getFile(mFS.getClient(),
                (DriveId) mFile.getNativeDescriptior());
        file.addChangeListener(mFS.getClient(), mListener);
    }

    @Override
    public void StopWatch() {
        DriveFile file = Drive.DriveApi.getFile(mFS.getClient(),
                (DriveId) mFile.getNativeDescriptior());
        file.removeChangeListener(mFS.getClient(), mListener);
    }
}
