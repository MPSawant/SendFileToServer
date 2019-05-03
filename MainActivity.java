package com.example.hp.sendfiletoserver;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b=(Button)findViewById(R.id.btn);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
        }
        enable_button();
    }

    private void enable_button(){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialFilePicker().withActivity(MainActivity.this).withRequestCode(10).start();

            }
        });
    }

    ProgressDialog progress;
    public File f;
    public String content_type;
    public String file_path;

    protected void onActivityResult(int requestCode,int resultCode,final Intent data){
        if(requestCode==10 && resultCode==RESULT_OK)
        {
            progress=new ProgressDialog(MainActivity.this);
            progress.setTitle("Uploadiing");
            progress.setMessage("Please Wait ....");
            progress.show();



            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    f=new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    Log.i("f is",f.toString());
                     content_type=getMimeType(f.getPath());

                    file_path=f.getAbsolutePath();

                   Log.i("by another mothid", Environment.getExternalStorageDirectory().getAbsolutePath() + "/X/M/" + "AudioRecording.3gp");

                    OkHttpClient client = new OkHttpClient();

                    RequestBody file_body=RequestBody.create(MediaType.parse(content_type),f);
                    RequestBody requestBody=new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1),file_body)
                            .build();



                    Request request=new Request.Builder()
                            .url("http://d54d2028.ngrok.io/save.php")
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            throw new IOException("error:" + response);
                        }
                        progress.dismiss();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }



                }
            });


            t.start();
        }
    }

    private String getMimeType(String path)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==100 && (grantResults[0]==PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }else{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }
}
