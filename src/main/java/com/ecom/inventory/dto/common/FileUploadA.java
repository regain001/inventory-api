package com.ecom.inventory.dto.common;

import java.util.Date;

public class FileUploadA {
    private Long fileId;
    private String objectType;
    private Long objectId;
    private String fileName;
    private String downloadPath;
    private String viewPath;
    private String mimeType;
    private String fileExtension;
    private Double fileSizeMb;
    private String thumbnailBas64;
    private Date creationDate;
    private Long createdBy;

    public String getObjectType() {
        return objectType;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Double getFileSizeMb() {
        return fileSizeMb;
    }

    public void setFileSizeMb(Double fileSizeMb) {
        this.fileSizeMb = fileSizeMb;
    }

    public String getThumbnailBas64() {
        return thumbnailBas64;
    }

    public void setThumbnailBas64(String thumbnailBas64) {
        this.thumbnailBas64 = thumbnailBas64;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
}
