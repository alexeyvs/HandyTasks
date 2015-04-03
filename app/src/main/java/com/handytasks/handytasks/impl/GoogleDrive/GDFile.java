package com.handytasks.handytasks.impl.GoogleDrive;

import com.google.android.gms.drive.DriveId;
import com.handytasks.handytasks.interfaces.ICloudFile;

/**
 * Created by avsho_000 on 3/24/2015.
 */
class GDFile extends ICloudFile {
    GDFile(DriveId driveId, String filename) {
        m_nativeDescriptor = driveId;
        m_fileName = filename;
    }

}
