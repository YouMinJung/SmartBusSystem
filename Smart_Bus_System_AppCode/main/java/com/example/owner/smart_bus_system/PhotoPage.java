package com.example.owner.smart_bus_system;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by OWNER on 2018-04-22.
 */
public class PhotoPage extends Activity {

    public static Activity PhotoPage;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;

    TextView text;

    // past boarding bus list
    ArrayList<String> START_station = new ArrayList<String>();
    ArrayList<String> END_station = new ArrayList<String>();
    ArrayList<String> GETOFF_time = new ArrayList<String>();
    ArrayList<String> BOARD_num = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_page);

        PhotoPage = PhotoPage.this;

        Intent endIntent = getIntent();
        int intent_value = 0;
        intent_value = endIntent.getExtras().getInt("activityNum");

        if(intent_value == 2) {
            // get past boarding list
            START_station = getIntent().getStringArrayListExtra("START_STATION");
            END_station = getIntent().getStringArrayListExtra("END_STATION");
            GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
            BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SmartBus_Picture");
        File files = new File(mediaStorageDir.getPath()+"/IMG.jpg");
        // image file exist check
        if(files.exists() == true && intent_value==2) {
            text = (TextView) findViewById(R.id.storage);
            text.setText(mediaStorageDir.getPath()+"/IMG.jpg");
        }
        else if(files.exists() == true) {
            text = (TextView) findViewById(R.id.storage);
            text.setText(mediaStorageDir.getPath()+"/IMG.jpg");

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("activityNum", 0);

            intent.putStringArrayListExtra("START_STATION", START_station);
            intent.putStringArrayListExtra("END_STATION", END_station);
            intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
            intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

            startActivity(intent);
            finish();
        }

        // click 'take a picture' button
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        // click 'complete' button
        findViewById(R.id.com_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("activityNum", 0);

                intent.putStringArrayListExtra("START_STATION", START_station);
                intent.putStringArrayListExtra("END_STATION", END_station);
                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                startActivity(intent);
                finish();
            }
        });
    }

    /** Create a file Uri for saving an image */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image */
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SmartBus_Picture");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("SmartBus_Picture", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +"IMG"+".jpg");
        }
        else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Complete to save", Toast.LENGTH_SHORT).show();

                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SmartBus_Picture");
                text = (TextView) findViewById(R.id.storage);
                text.setText(mediaStorageDir.getPath()+"/IMG.jpg");
            }
            else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
