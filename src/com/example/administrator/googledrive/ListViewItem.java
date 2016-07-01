package com.example.administrator.googledrive;


public class ListViewItem {
    private String fileId ;
    private String fileName ;

    public void setFileId(String fileId) {
        this.fileId = fileId ;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName ;
    }

    public String getFileId() {
        return this.fileId ;
    }
    public String getFileName() {
        return this.fileName;
    }
}