package com.example.administrator.googledrive;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016-07-02.
 */

public class DownLoadFileTask extends AsyncTask<Void, Void, ByteArrayOutputStream> {
    private int fileSize;
    private Context mContext;
    private com.google.api.services.drive.Drive dService = null;
    private String fileName=null, fileID=null;
    public DownLoadFileTask(Context mContext,GoogleAccountCredential credential, String fileID, String fileName) {
        this.mContext= mContext;
        this.fileID=fileID;
        this.fileName=fileName;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        dService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Drive API Android Quickstart")
                .build();
    }


    @Override
    protected ByteArrayOutputStream doInBackground(Void... params) {
        getContext();
        return downloadFile(fileID);
    }

    @Override
    protected void onPostExecute(ByteArrayOutputStream os) {
        super.onPostExecute(os);
        byte[] filebyte=os.toByteArray();
        String filePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                        .toString()+"/SaveMeFolder";
        try {
            java.io.File dir = new java.io.File(filePath);
            dir.mkdir();
            Toast.makeText(getContext(),"디렉터리 생성",Toast.LENGTH_SHORT).show();
            FileOutputStream fop = new FileOutputStream(new File(dir.getAbsolutePath().toString()
            +"/"+fileName),true);
            fop.flush();
            fop.write(filebyte);
            fop.close();
            Toast.makeText(getContext(),"파일 다운로드 완료",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 길게 터치하면 파일 Id를 받아와서 executeMediaAndDownloadTo 메소드 실행
     *
     */
    public ByteArrayOutputStream downloadFile(String fileId) {
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            dService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            setFileSize(dService.files().get(fileId).size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (ByteArrayOutputStream)outputStream;
    }

    public Context getContext(){
        return mContext;
    }

    public void setFileSize(int fileSize){
        this.fileSize=fileSize;
    }

    public int getFileSize(){
        return fileSize;
    }
}