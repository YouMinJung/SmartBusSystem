package com.example.owner.smart_bus_system;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by OWNER on 2018-03-27.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), PhotoPage.class);
        intent.putExtra("activityNum", 0);
        startActivity(intent);
        finish();
    }
}
