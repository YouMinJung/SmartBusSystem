package com.example.owner.smart_bus_system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by OWNER on 2018-04-07.
 */
public class AppEndPage extends Activity {

    public static Activity AppEndPage;

    LinearLayout List_background[];
    LinearLayout Background;
    TextView InStop, OffStop, InPay, OffPay, TotalPay;
    TextView pastBusList[], pastInList[], pastOffList[], pastTimeList[];

    int past_busnumList = 0;

    // past boarding bus list
    ArrayList<String> START_station = new ArrayList<String>();
    ArrayList<String> END_station = new ArrayList<String>();
    ArrayList<String> GETOFF_time = new ArrayList<String>();
    ArrayList<String> BOARD_num = new ArrayList<String>();

    // payment price
    String st_PRICE = "";
    String ed_PRICE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_endpage);

        AppEndPage = AppEndPage.this;

        // get past boarding list
        START_station = getIntent().getStringArrayListExtra("START_STATION");
        END_station = getIntent().getStringArrayListExtra("END_STATION");
        GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
        BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");

        // payment price
        st_PRICE = getIntent().getExtras().getString("ST_Price");
        ed_PRICE = getIntent().getExtras().getString("ED_Price");

        BusLine busLine = (BusLine) BusLine.BusLine;
        busLine.finish();

        InStop = (TextView) findViewById(R.id.in_stop);
        InStop.setText(START_station.get(START_station.size()-1));
        InPay = (TextView) findViewById(R.id.in_pay);
        InPay.setText(st_PRICE);

        OffStop = (TextView) findViewById(R.id.off_stop);
        OffStop.setText(END_station.get(END_station.size()-1));
        OffPay = (TextView) findViewById(R.id.off_pay);
        OffPay.setText(ed_PRICE);

        TotalPay = (TextView) findViewById(R.id.total_pay);

        // calculate total pay
        int result1 = Integer.parseInt(InPay.getText().toString());
        int result2 = Integer.parseInt(OffPay.getText().toString());
        TotalPay.setText(Integer.toString(result1+result2));


        // draw before bus boarding list
        Background = (LinearLayout) findViewById(R.id.list);
        past_busnumList = START_station.size();

        pastBusList = new TextView[past_busnumList];
        pastInList = new TextView[past_busnumList];
        pastOffList = new TextView[past_busnumList];
        pastTimeList = new TextView[past_busnumList];
        List_background = new LinearLayout[past_busnumList];

        for(int i=0; i<past_busnumList; i++) {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) ;
            params.setMargins(50, 30, 50, 0);

            List_background[i] = new LinearLayout(this);
            List_background[i].setOrientation(LinearLayout.HORIZONTAL);
            List_background[i].setLayoutParams( params );

            pastBusList[i] = new TextView(this);
            pastBusList[i].setText("\t"+BOARD_num.get(i)+"\t"+"\t:\t");
            pastBusList[i].setTextColor(Color.BLUE);
            List_background[i].addView(pastBusList[i]);

            pastInList[i] = new TextView(this);
            pastInList[i].setText("\t"+START_station.get(i)+"\t"+"\t->"+"\t");
            pastInList[i].setTextColor(Color.BLUE);
            List_background[i].addView(pastInList[i]);

            pastOffList[i] = new TextView(this);
            pastOffList[i].setText("\t"+END_station.get(i));
            pastOffList[i].setTextColor(Color.BLUE);
            List_background[i].addView(pastOffList[i]);

            pastTimeList[i] = new TextView(this);
            pastTimeList[i].setText("\t"+" : "+GETOFF_time.get(i));
            pastTimeList[i].setTextColor(Color.BLUE);
            List_background[i].addView(pastTimeList[i]);

            Background.addView(List_background[i]);

        }

        // if click the button -> go to app main page
        Button check_button = (Button) findViewById(R.id.check_button);
        check_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(AppEndPage.this);
                alt_bld.setMessage("Would you take another bus?").setCancelable(
                        false).setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getApplicationContext(), AppMainPage.class);
                                intent.putExtra("activityNum", 4);

                                // past boarding list
                                intent.putStringArrayListExtra("START_STATION", START_station);
                                intent.putStringArrayListExtra("END_STATION", END_station);
                                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                                startActivity(intent);
                            }
                        }).setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.exit(1);
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.setTitle("Transfer / End");
                alert.show();
            }
        });

    }
}
