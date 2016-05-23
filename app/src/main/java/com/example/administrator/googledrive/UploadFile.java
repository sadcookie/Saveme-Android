package com.example.administrator.googledrive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016-05-23.
 */
public class UploadFile extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE = 101;
    public static String drive_id;
    public static DriveId driveID;
    private GoogleApiClient mGoogle;
    private File textfile;
    private final String TAG = "Upload File";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textfile = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator
                + "log.txt");
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogle.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogle != null) ;
        {
            Log.i(TAG, "GoogleClient Disconnecting");
            mGoogle.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult - connectiong");
            mGoogle.connect();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }


    public void onConnected(Bundle bundle){
        Drive.DriveApi.newDriveContents(mGoogle).setResultCallback(driveContentCallback);
    }

    @Override
    public void onConnectionSuspended(int i)  {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    new Thread() {
                        public void run() {
                            OutputStream outputStream = driveContents.getOutputStream();
                            addTextfileToOutputStream(outputStream);

                            MetadataChangeSet changeset = new MetadataChangeSet.Builder()
                                    .setTitle("test.txt")
                                    .setMimeType("text/plain")
                                    .setDescription("This is upload by android")
                                    .setStarred(true).build();
                            Drive.DriveApi.getRootFolder(mGoogle)
                                    .createFile(mGoogle, changeset, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };

    private void addTextfileToOutputStream(OutputStream outputStream) {
        Log.i(TAG, "adding text file to outputstream...");
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(textfile));
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.i(TAG, "problem converting input stream to output stream: " + e);
            e.printStackTrace();
        }
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating the file");
                        Toast.makeText(UploadFile.this,
                                "Error adding file to Drive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG, "File added to Drive");
                    Log.i(TAG, "Created a file with content: "
                            + result.getDriveFile().getDriveId());
                    Toast.makeText(UploadFile.this,
                            "File successfully added to Drive", Toast.LENGTH_SHORT).show();
                    final PendingResult<DriveResource.MetadataResult> metadata
                            = result.getDriveFile().getMetadata(mGoogle);
                    metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                        @Override
                        public void onResult(DriveResource.MetadataResult metadataResult) {
                            Metadata data = metadataResult.getMetadata();
                            Log.i(TAG, "Title: " + data.getTitle());
                            drive_id = data.getDriveId().encodeToString();
                            Log.i(TAG, "DrivId: " + drive_id);
                            driveID = data.getDriveId();
                            Log.i(TAG, "Description: " + data.getDescription().toString());
                            Log.i(TAG, "MimeType: " + data.getMimeType());
                            Log.i(TAG, "File size: " + String.valueOf(data.getFileSize()));
                        }
                    });
                }
            };

    private void buildGoogleApiClient(){
        if(mGoogle==null){
            mGoogle=new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .build();
        }
    }
}