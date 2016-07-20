
package com.example.administrator.googledrive;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.model.File;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.util.List;

public class DownLaodListUI extends Activity{
    GoogleAccountCredential mCredential;
    private ListViewAdapter adapter = new ListViewAdapter();
    private ListView mListView;
    private TextView mOutputText;

    static final int REQUEST_AUTHORIZATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOutputText = (TextView) findViewById(R.id.textview);
        mListView = (ListView) findViewById(R.id.listViewResults);
        mCredential = new MainUI().getCredential();
        mOutputText.setText("구글 드라이브 파일");
        new MakeRequestTask(new MainUI().getCredential()).execute();

    }

    private class MakeRequestTask extends AsyncTask<Void, Void, ListViewAdapter>
    implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected ListViewAdapter doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private ListViewAdapter getDataFromApi() throws IOException {
            ListViewAdapter lva = new ListViewAdapter();
            // Get a list of up to 10 files.
            FileList result = mService.files().list()
                    .setPageSize(30)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    lva.addItem(String.format("%s",file.getName()),String.format("%s",file.getId()));
                }
            }
            return lva;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");

        }

        @Override
        protected void onPostExecute(ListViewAdapter output) {
            if (output.isEmpty()) {
                mOutputText.setText("No results returned.");
            } else {
                mListView.setAdapter(output);
                mOutputText.setText("Data retrieved using the Drive API:");
                mListView.setOnItemClickListener(this);
                mListView.setOnItemLongClickListener(this);
            }
        }

        /**
         * 리스트에서 파일을 터치 했을 때 발생하는 메소드
         * 터치하면 파일의 위치 값을 받아서 파일 이름을 토스트로 출력
         */

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListViewItem lvt = (ListViewItem) parent.getItemAtPosition(position);
            Toast.makeText(getBaseContext(),"Selected File = "+lvt.getFileName(),Toast.LENGTH_SHORT).show();
        }

        /**
         * 길게 터치하면 파일 Id를 받아와서 executeMediaAndDownloadTo 메소드 실행
         */
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListViewItem lvt = (ListViewItem) parent.getItemAtPosition(position);
            String FileId= lvt.getFileId();
            String FileName= lvt.getFileName();
            new DownLoadFileTask(getBaseContext(),mCredential,FileId,FileName).execute();
            Toast.makeText(getBaseContext()
                    ,"DownLoad File = "+lvt.getFileName(),Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            DownLaodListUI.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}