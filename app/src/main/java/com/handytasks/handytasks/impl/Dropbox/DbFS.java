package com.handytasks.handytasks.impl.Dropbox;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
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

import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public class DbFS extends ACloudAPIHolder implements ICloudFS {
    private DbxFileSystem m_DbFS;
    private ICloudWatcher m_dbWatcher;
    private Hashtable<ICloudFile, ICloudWatcher> mWatchers = new Hashtable<ICloudFile, ICloudWatcher>();


    public DbFS(ICloudAPI api) {
        mAPI = api;

        try {
            m_DbFS = DbxFileSystem.forAccount(((DbxAccountManager) mAPI.getAccountManager()).getLinkedAccount());
        } catch (DbxException.Unauthorized unauthorized) {
            unauthorized.printStackTrace();
        }
    }

    @Override
    public Boolean WriteToFile(ICloudFile file, String data) throws IOException {
        DbxFile currentFile = null;
        boolean isOpen = false;
        try {
            if (!m_DbFS.exists(new DbxPath(file.getFilename()))) {
                currentFile = m_DbFS.create(new DbxPath(file.getFilename()));
            } else {
                currentFile = m_DbFS.open(new DbxPath(file.getFilename()));
            }

            isOpen = true;

            currentFile.writeString(data);
            return true;
        } catch (Exception e) {
            throw e;
        } finally {
            if (isOpen) {
                currentFile.close();
            }
        }
    }

    @Override
    public String ReadFromFile(ICloudFile file) throws IOException {
        DbxFile currentFile = null;

        boolean isOpen = false;
        try {
            if (!m_DbFS.exists(new DbxPath(file.getFilename()))) {
                currentFile = m_DbFS.create(new DbxPath(file.getFilename()));
            } else {
                currentFile = m_DbFS.open(new DbxPath(file.getFilename()));
            }

            isOpen = true;

            return currentFile.readString();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (isOpen) {
                currentFile.close();
            }
        }
    }

    @Override
    public ICloudWatcher getWatcher(ICloudFile file) {
        if (!mWatchers.containsKey(file)) {
            mWatchers.put(file, new DbWatcher(this, file));
        }
        return mWatchers.get(file);
    }


    @Override
    public ICloudFS initializeFS(IAsyncResult callback) {
        // do nothing for in dropbox case
        callback.OnSuccess(null);
        return this;
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
    public void CreateTextFile(String filename, IOpenFileResult callback) {
        DbxFile currentFile = null;
        boolean isOpen = false;

        try {
            if (!m_DbFS.exists(new DbxPath(filename))) {
                currentFile = m_DbFS.create(new DbxPath(filename));
                isOpen = true;
            } else {
                callback.OnSuccess(new DbFile(filename));
            }
        } catch (Exception e) {
            callback.OnFailure(e.getMessage());
        } finally {
            if (isOpen) {
                currentFile.close();
                callback.OnSuccess(new DbFile(filename));
            }
        }
    }

    @Override
    public boolean isLatest(String filename) {

        DbxFile currentFile = null;

        boolean isOpen = false;
        boolean Result = false;
        try {
            if (!m_DbFS.exists(new DbxPath(filename))) {
                currentFile = m_DbFS.create(new DbxPath(filename));
            } else {
                currentFile = m_DbFS.open(new DbxPath(filename));
            }

            isOpen = true;

            DbxFileStatus CurrentStatus = currentFile.getSyncStatus();
            Result = CurrentStatus.isLatest && CurrentStatus.pending == DbxFileStatus.PendingOperation.NONE;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (isOpen) {
                currentFile.close();
            }
            return Result;
        }
    }

    public void addPathListener(DbxFileSystem.PathListener pathListener, DbxPath dbxPath) {
        m_DbFS.addPathListener(pathListener, dbxPath, DbxFileSystem.PathListener.Mode.PATH_ONLY);
    }

    public void removePathListener(DbxFileSystem.PathListener pathListener, DbxPath dbxPath) {
        m_DbFS.removePathListener(pathListener, dbxPath, DbxFileSystem.PathListener.Mode.PATH_ONLY);
    }

}
