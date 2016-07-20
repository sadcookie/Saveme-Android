package com.example.administrator.googledrive;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016-07-19.
 */
public class DivideFile {
    private com.google.api.services.drive.model.File fileMetadata
            =new com.google.api.services.drive.model.File();
    private FileContent fileContent=null;
    private FileInputStream inputstream;
    private java.io.File mfile;
    private String NewFileName;
    private int fileSize;
    private int fileIdx=0;


    public DivideFile(){

    }

    public DivideFile(java.io.File mfile){
        Log.d("test","생성자");
        this.mfile = mfile;
    }

    public void setFile(java.io.File mfilePath){
        this.mfile=mfilePath;
    }

    public void chunkFile(){
        fileSize=(int)mfile.length();
        int read = 0;

        byte[] readfilebyte = new byte[fileSize/2];
        try {
            Log.d("test","파일 분리");
            inputstream = new FileInputStream(mfile);
            BufferedInputStream bfi = new BufferedInputStream(inputstream);
            Log.d("test","퍼버 갖음");
            while(fileSize>0) {
                read = bfi.read(readfilebyte);
                NewFileName= mfile.getAbsolutePath()+"-"+(fileIdx++);
                FileOutputStream fo = new FileOutputStream(NewFileName);
                fo.write(readfilebyte,0,read);
                fileSize-=read;
                fo.flush();
                fo.close();
                Log.d("test","정상작동함");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void uploadChunkFileNDeleteFile(){
        for(int i=0; i<fileIdx;i++) {
            java.io.File file =
                    new java.io.File(mfile.getName()+"-"+(i));
            fileMetadata.setName(file.getName());
            fileMetadata.setMimeType("application/vnd.google-apps.file");
            fileContent = new FileContent("application/vnd.google-apps.file", mfile);
            UploadFileTask uft = new UploadFileTask();
            uft.setFileContent(fileContent);
            uft.setFileMetadata(fileMetadata);
            uft.execute();
            Log.d("파일업로드","정상 업로드");

        }
    }

    public void deleteFile(java.io.File file){
        file.delete();
        Log.d("파일 삭제","정상삭제");
    }


}
