package com.vasanth.attachfile.attachment.model;

import android.net.Uri;

/**
 * Attachment File Detail.
 * <p>
 * 1. Responsibility.
 * 1.a. Model used to hold details about attachment file.
 *
 * @author Vasanth
 */
public class AttachmentFileDetail {

    private String name;
    private long size;
    private Uri uri;
    private String mimeType;

    /**
     * Constructor.
     *
     * @param name     Attachment file name.
     * @param size     Attachment file size in Bytes.
     * @param mimeType Attachment file mimeType.
     * @param uri      User Device Attachment file uri.
     */
    public AttachmentFileDetail(final String name, final long size, final String mimeType, final Uri uri) {
        this.name = name;
        this.size = size;
        this.uri = uri;
        this.mimeType = mimeType;
    }

    /**
     * Getter's & Setter's.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * To String.
     */
    @Override
    public String toString() {
        return "AttachmentFileDetail{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", uri=" + uri +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
