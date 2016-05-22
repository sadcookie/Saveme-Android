package com.example.administrator.googledrive;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

/**
 * An activity illustrates how to list file results and infinitely
 * populate the results list view with data if there are more results.
 */
public class ListFile extends BaseClientAuth {

    private ListView mListView;
    private DataBufferAdapter<Metadata> mResultsAdapter;
    private String mNextPageToken;
    private boolean mHasMore;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_listfiles);
        mHasMore = true; // initial request assumes there are files results.

        mListView = (ListView) findViewById(R.id.listViewResults);
        mResultsAdapter = new ResultAdapter(this);
        mListView.setAdapter(mResultsAdapter);
        mListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            /**
             * Handles onScroll to retrieve next pages of results
             * if there are more results items to display.
             */
            @Override
            public void onScroll(AbsListView view, int first, int visible, int total) {
                if (mNextPageToken != null && first + visible + 5 < total) {
                    retrieveNextPage();
                }
            }
        });
    }

    /**
     * Clears the result buffer to avoid memory leaks as soon
     * as the activity is no longer visible by the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mResultsAdapter.clear();
    }

    /**
     * Handles the Drive service connection initialization
     * and inits the first listing request.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        retrieveNextPage();
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        if (!mHasMore) {
            return;
        }
        Drive.DriveApi.requestSync(getGoogleApiClient()).;
        String h = Drive.DriveApi.getAppFolder(getGoogleApiClient()).getDriveId().getResourceId().toString();
        showMessage(h);
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), h).setResultCallback(idCallback);

    }

    /**
     * Appends the retrieved results to the result buffer.
     */

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            DriveId driveId = result.getDriveId();
            showMessage(driveId.toString());
            DriveFolder folder = driveId.asDriveFolder();
            folder.listChildren(getGoogleApiClient())
                    .setResultCallback(metadataResult);
        }
    };


    final private ResultCallback<MetadataBufferResult> metadataResult = new
            ResultCallback<MetadataBufferResult>() {
                @Override
                public void onResult(MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving files");
                        return;
                    }
                    mResultsAdapter.clear();
                    mResultsAdapter.append(result.getMetadataBuffer());
                    showMessage("Successfully listed files.");
                }
            };
}