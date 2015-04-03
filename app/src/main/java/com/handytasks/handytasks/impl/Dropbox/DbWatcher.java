package com.handytasks.handytasks.impl.Dropbox;

import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.handytasks.handytasks.interfaces.ICloudFS;
import com.handytasks.handytasks.interfaces.ICloudFile;
import com.handytasks.handytasks.interfaces.ICloudWatcher;
import com.handytasks.handytasks.interfaces.IFSChangeHandler;


/**
 * Created by avsho_000 on 3/20/2015.
 */
public class DbWatcher implements ICloudWatcher {

    private final DbFS mFS;
    private final ICloudFile mFile;
    private IFSChangeHandler m_FSChangeHandler;
    private DbxFileSystem.PathListener mListener = new DbxFileSystem.PathListener() {
        @Override
        public void onPathChange(DbxFileSystem dbxFileSystem, DbxPath dbxPath, Mode mode) {
            m_FSChangeHandler.PathChanged(dbxPath.getName());
        }
    };


    public DbWatcher(ICloudFS fs, ICloudFile file) {
        mFS = (DbFS) fs;
        mFile = file;
    }

    public void StartWatch(IFSChangeHandler handler) {
        m_FSChangeHandler = handler;
        mFS.addPathListener(mListener, new DbxPath(mFile.getFilename()));
    }

    @Override
    public void StopWatch() {
        mFS.removePathListener(mListener, new DbxPath(mFile.getFilename()));
    }

}
