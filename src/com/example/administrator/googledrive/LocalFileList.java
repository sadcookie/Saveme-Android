package com.example.administrator.googledrive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.FileContent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFileList extends Activity implements AdapterView.OnItemLongClickListener{

    private static com.google.api.services.drive.model.File fileMetadata=null;
    private static FileContent fileContent=null;
    private String mFileName;
    private ListView lvFileControl;
    private Context mContext = this;

    private List<String> lItem = null;
    private List<String> lPath = null;
    private String mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
    private TextView mPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.localfilelist);
        mPath = (TextView) findViewById(R.id.tvPath);
        lvFileControl = (ListView)findViewById(R.id.lvFileControl);
        getDir(mRoot);
        lvFileControl.setOnItemLongClickListener(this);
        lvFileControl.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                File file = new File(lPath.get(position));

                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(lPath.get(position));
                    else {
                        Toast.makeText(mContext, "No files in this folder.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mFileName = file.getName();
                    Log.i("Test", "ext:" + mFileName.substring(mFileName.lastIndexOf('.') + 1, mFileName.length()));
                }
            }
        });
    }

    private void getDir(String dirPath) {
        mPath.setText("Location: " + dirPath);

        lItem = new ArrayList<String>();
        lPath = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(mRoot)) {
            lItem.add("../");
            lPath.add(f.getParent());
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            lPath.add(file.getAbsolutePath());

            if (file.isDirectory())
                lItem.add(file.getName() + "/");
            else
                lItem.add(file.getName());
        }

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItem);
        lvFileControl.setAdapter(fileList);
    }

    public com.google.api.services.drive.model.File getFileMetdata(){
        return fileMetadata;
    }

    public FileContent getFileContent(){
        return fileContent;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        PopupMenu mPopupMenu = new PopupMenu(LocalFileList.this, view);
        getMenuInflater().inflate(R.menu.locallist_menu,mPopupMenu.getMenu());
        final int index= position;
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            File file = new File(lPath.get(index));
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.upload:
                        fileMetadata =new com.google.api.services.drive.model.File();

                        fileMetadata.setName(file.getName());
                        fileMetadata.setMimeType("application/vnd.google-apps.file");

                        fileContent = new FileContent("application/vnd.google-apps.file",file);
                        UploadFileTask uft = new UploadFileTask();
                        uft.setFileContent(fileContent);
                        uft.setFileMetadata(fileMetadata);
                        uft.execute();
                        break;
                    case R.id.divide:
                        Log.d("test","분리작업 실행");
                        DivideFile div = new DivideFile(file);
                        div.chunkFile();
                        div.uploadChunkFileNDeleteFile();
                        break;
                }
                return false;

            }
        });
        mPopupMenu.show();
        return false;
    }
}