package com.handytasks.handytasks.interfaces;

/**
 * Created by avsho_000 on 3/12/2015.
 */
public abstract class ICloudFile {
    protected String m_fileName;

    protected Object m_nativeDescriptor;

    public String getFilename() {
        return m_fileName;
    }


    public Object getNativeDescriptior() {
        return m_nativeDescriptor;
    }
}
