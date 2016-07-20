package com.example.administrator.googledrive;

import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;

import java.io.IOException;

/**
 * Created by Administrator on 2016-07-04.
 */
public class UploadFileTask extends AsyncTask<Void, Void, Void> {
    private com.google.api.services.drive.Drive dService = null;
    private com.google.api.services.drive.model.File fileMetadata =null;
    private FileContent mediacontents =null;
    private GoogleAccountCredential credential = null;
    public UploadFileTask() {
        credential=new MainUI().getCredential();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        dService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Drive API Android Quickstart")
                .build();

    }

    public void setFileMetadata(File fileMetadata){
        this.fileMetadata=fileMetadata;
    }

    public void setFileContent(FileContent mediacontents){
        this.mediacontents = mediacontents;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            dService.files().create(fileMetadata,mediacontents)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        super.onPostExecute(aVoid);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
