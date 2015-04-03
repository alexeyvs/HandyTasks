package com.handytasks.handytasks.impl.Dropbox;

import com.handytasks.handytasks.interfaces.ICloudFile;

/**
 * Created by avsho_000 on 3/24/2015.
 */
class DbFile extends ICloudFile {
    public DbFile(String filename) {
        m_fileName = filename;
        m_nativeDescriptor = null;
    }

}
