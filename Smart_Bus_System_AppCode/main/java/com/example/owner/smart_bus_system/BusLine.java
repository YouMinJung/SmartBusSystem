package com.example.owner.smart_bus_system;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by OWNER on 2018-04-02.
 */
public class BusLine extends Activity {

    public static Activity BusLine;

    int i=0; // for loop
    int final_num = 0; // if thread finish flag = 1 -> end thread
    int busStop_num = 0; // the number of total stations
    int next_layout_flag = 0; // bus draw next layout -> 1
    LinearLayout background;
    int alarm_num = 0; // layout number
    int alarm_flag = 0; // if alarm already ring -> 1
    int bus_move_refresh = 0;
    int now_bus_layoutPosition = 0;

    boolean inRouteID = false, inRouteNm = false, inEdStationNm = false;
    boolean inStationID = false, inStationNm = false, inStationNo = false, inBeginTm = false, inLastTm = false, inTrnstnid = false;;
    boolean inPlainNo = false, inPosX = false, inPosY = false, inStId = false, inStopFlag = false, inVehId = false;

    String BusRouteList = "http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?";
    String ServiceKey = "ServiceKey=qcNUh%2BkNUSDsfFLbMNXVVAhDViLgLxYBBQUATBuuulRT6kfjq4%2FaW1bTMsOCMRfdF1HFY5OYGG8buLLyHfHqDw%3D%3D&&";
    String LineNum = "strSrch=";
    String BusStationsByRouteList = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";
    String BusRouteId = "&busRouteId=";
    String BusPosByVehIdItem = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByVehId?";
    String BusVehId = "&vehId=";

    // find routeID using bus route number info
    String RouteNm = null, RouteID = null, EdStationNm = null;

    // Bus Route station Info Array
    List<String> StationNm = new ArrayList<String>();
    List<String> StationNo = new ArrayList<String>();
    List<String> StationID = new ArrayList<String>();
    List<String> BeginTm = new ArrayList<String>();
    List<String> LastTm = new ArrayList<String>();
    List<String> Trnstnid = new ArrayList<String>();

    // get Bus Pos using VehId
    String BusID = null;
    String PlainNo = null, PosX = null, PosY = null, StId = null, StopFlag = null, VehId = null;

    // raspberrypi ip
    String BeaconIP = null;
    String ip1= null, ip2= null, ip3= null, ip4= null;

    // layout list (Draw)
    LinearLayout layout_block[];
    TextView bus_numT[];
    TextView bus_stopT[];
    ImageView car_image[];

    // click layout number - alarm
    int ID_num = 0;

    // thread
    BackThread thread;

    // past boarding bus list
    ArrayList<String> START_station = new ArrayList<String>();
    ArrayList<String> END_station = new ArrayList<String>();
    ArrayList<String> GETOFF_time = new ArrayList<String>();
    ArrayList<String> BOARD_num = new ArrayList<String>();

    // start station
    int START_st_flag = 0;

    int get_out_flag = 0;

    // payment price
    String st_PRICE = "";
    String ed_PRICE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busline_page);

        BusLine = BusLine.this;
        AppMainPage appMainPage = (AppMainPage) AppMainPage.AppMainPage;
        appMainPage.finish();

        Intent endIntent = getIntent();
        BusID = Long.toString(endIntent.getExtras().getLong("BusID_Num"));
        RouteNm = endIntent.getExtras().getString("BusNumber_Num");
        ip1 = Long.toString(endIntent.getExtras().getLong("IP_Num1"));
        ip2 = Long.toString(endIntent.getExtras().getLong("IP_Num2"));
        ip3 = Long.toString(endIntent.getExtras().getLong("IP_Num3"));
        ip4 = Long.toString(endIntent.getExtras().getLong("IP_Num4"));

        BeaconIP = ip1;
        BeaconIP = BeaconIP.concat(".");
        BeaconIP = BeaconIP.concat(ip2);
        BeaconIP = BeaconIP.concat(".");
        BeaconIP = BeaconIP.concat(ip3);
        BeaconIP = BeaconIP.concat(".");
        BeaconIP = BeaconIP.concat(ip4);

        // get past boarding list
        START_station = getIntent().getStringArrayListExtra("START_STATION");
        END_station = getIntent().getStringArrayListExtra("END_STATION");
        GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
        BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");

        // get in_price
        st_PRICE = endIntent.getExtras().getString("ST_Price");

        // thread default flag
        final_num = 0;

        // get bus Info from openAPI - busID, bus num, car num, bus line
        StrictMode.enableDefaults();

        TextView Top_text = (TextView) findViewById(R.id.bus_numText);
        Top_text.setText(RouteNm);

        try {
            String URLResult1 = BusRouteList.concat(ServiceKey).concat(LineNum).concat(RouteNm);

            // getBusRouteList function
            URL url1 = new URL(URLResult1);

            // get RouteID
            XmlPullParserFactory parserCreator1 = XmlPullParserFactory.newInstance();
            XmlPullParser parser1 = parserCreator1.newPullParser();
            parser1.setInput(url1.openStream(), null);
            int parserEvent1 = parser1.getEventType();

            while (parserEvent1 != XmlPullParser.END_DOCUMENT) {

                switch (parserEvent1) {
                    // if parser meets start tag -> execute
                    case XmlPullParser.START_TAG:
                        if (parser1.getName().equals("busRouteId")) {
                            inRouteID = true;
                        }
                        if (parser1.getName().equals("busRouteNm")) {
                            inRouteNm = true;
                        }
                        if (parser1.getName().equals("edStationNm")) {
                            inEdStationNm = true;
                        }
                        break;

                    // parser closes to contents
                    case XmlPullParser.TEXT:

                        if (inRouteID) {
                            RouteID = parser1.getText();
                            inRouteID = false;
                        }
                        if (inRouteNm) {
                            RouteNm = parser1.getText();
                            inRouteNm = false;
                        }
                        if (inEdStationNm) {
                            EdStationNm = parser1.getText();
                            inEdStationNm = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser1.getName().equals("itemList")) {
                        }
                        break;
                }
                parserEvent1 = parser1.next();
            }
        } catch (Exception e) {
        }

        try {
            String URLResult2 = BusStationsByRouteList.concat(ServiceKey).concat(BusRouteId).concat(RouteID);
            // getBusRouteList function
            URL url2 = new URL(URLResult2);

            // get RouteID
            XmlPullParserFactory parserCreator2 = XmlPullParserFactory.newInstance();
            XmlPullParser parser2 = parserCreator2.newPullParser();
            parser2.setInput(url2.openStream(), null);
            int parserEvent2 = parser2.getEventType();

            while (parserEvent2 != XmlPullParser.END_DOCUMENT) {

                switch (parserEvent2) {
                    case XmlPullParser.START_TAG:
                        if (parser2.getName().equals("beginTm")) {
                            inBeginTm = true;
                        }
                        if (parser2.getName().equals("busRouteId")) {
                            inRouteID = true;
                        }
                        if (parser2.getName().equals("lastTm")) {
                            inLastTm = true;
                        }
                        if (parser2.getName().equals("station")) {
                            inStationID = true;
                        }
                        if (parser2.getName().equals("stationNm")) {
                            inStationNm = true;
                        }
                        if (parser2.getName().equals("stationNo")) {
                            inStationNo = true;
                        }
                        if (parser2.getName().equals("trnstnid")) {
                            inTrnstnid = true;
                        }
                        break;

                    case XmlPullParser.TEXT:

                        if (inBeginTm) {
                            BeginTm.add(parser2.getText());
                            inBeginTm = false;
                        }
                        if (inRouteID) {
                            RouteID = parser2.getText();
                            inRouteID = false;
                        }
                        if (inLastTm) {
                            LastTm.add(parser2.getText());
                            inLastTm = false;
                        }
                        if (inStationID) {
                            StationID.add(parser2.getText());
                            inStationID = false;
                        }
                        if (inStationNm) {
                            StationNm.add(parser2.getText());
                            inStationNm = false;
                        }
                        if (inStationNo) {
                            StationNo.add(parser2.getText());
                            inStationNo = false;
                        }
                        if (inTrnstnid) {
                            Trnstnid.add(parser2.getText());
                            inTrnstnid = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser2.getName().equals("itemList")) {
                            busStop_num++;
                        }
                        break;
                }
                parserEvent2 = parser2.next();
            }
        } catch (Exception e) {
        }

        try {
            // getBusPosByVehIdItem
            String URLResult3 = BusPosByVehIdItem.concat(ServiceKey).concat(BusVehId).concat(BusID);
            // getBusRouteList function
            URL url3 = new URL(URLResult3);

            // get Bus Now Position Info
            XmlPullParserFactory parserCreator3 = XmlPullParserFactory.newInstance();
            XmlPullParser parser3 = parserCreator3.newPullParser();
            parser3.setInput(url3.openStream(), null);
            int parserEvent3 = parser3.getEventType();

            while (parserEvent3 != XmlPullParser.END_DOCUMENT) {

                switch (parserEvent3) {
                    case XmlPullParser.START_TAG:
                        if (parser3.getName().equals("plainNo")) {
                            inPlainNo = true;
                        }
                        if (parser3.getName().equals("posX")) {
                            inPosX = true;
                        }
                        if (parser3.getName().equals("posY")) {
                            inPosY = true;
                        }
                        if (parser3.getName().equals("stId")) {
                            inStId = true;
                        }
                        if (parser3.getName().equals("stopFlag")) {
                            inStopFlag = true;
                        }
                        if (parser3.getName().equals("vehId")) {
                            inVehId = true;
                        }
                        break;

                    case XmlPullParser.TEXT:

                        if (inPlainNo) {
                            PlainNo = parser3.getText();
                            inPlainNo = false;
                        }
                        if (inPosX) {
                            PosX = parser3.getText();
                            inPosX = false;
                        }
                        if (inPosY) {
                            PosY = parser3.getText();
                            inPosY = false;
                        }
                        if (inStId) {
                            StId = parser3.getText();
                            inStId = false;
                        }
                        if (inStopFlag) {
                            StopFlag = parser3.getText();
                            inStopFlag = false;
                        }
                        if (inVehId) {
                            VehId = parser3.getText();
                            inVehId = false;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser3.getName().equals("itemList")) {
                        }
                        break;
                }
                parserEvent3 = parser3.next();
            }
        } catch (Exception e) {
        }

        // draw bus line
        background = (LinearLayout) findViewById(R.id.background);
        bus_numT = new TextView[busStop_num];
        car_image = new ImageView[busStop_num];
        layout_block = new LinearLayout[busStop_num];
        bus_stopT = new TextView[busStop_num];

        for (i = 0; i < busStop_num; i++) {

            if (i != 0) {
                TextView middle_line = new TextView(this);
                middle_line.setText("--------------------------------------------------------------------");
                middle_line.setTextColor(Color.parseColor("#E6E6E6"));
                middle_line.setTextSize(15);
                background.addView(middle_line);
            }
            layout_block[i] = new LinearLayout(this);
            layout_block[i].setOrientation(LinearLayout.HORIZONTAL);
            layout_block[i].setId(i);
            layout_block[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // click layout number
                    ID_num = v.getId();

                    AlertDialog.Builder dlg = new AlertDialog.Builder(BusLine.this);
                    dlg.setTitle("Set Alarm");
                    dlg.setMessage("Complete to set alarm. [ " + StationNm.get(ID_num) +" ]").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // set alarm number
                            alarm_num = ID_num;

                            for(int q=0; q<busStop_num; q++) {
                                if(q != ID_num) {
                                    bus_stopT[q].setTextColor(Color.BLACK);
                                }
                                else {
                                    bus_stopT[ID_num].setTextColor(Color.parseColor("#8000FF"));
                                }
                            }
                        }
                    });
                    dlg.show();
                }
            });
            background.addView(layout_block[i]);

            // bus view
            LinearLayout bus_move = new LinearLayout(this);
            bus_move.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            param.leftMargin = 200;
            param.rightMargin = 75;
            bus_move.setLayoutParams(param);

            bus_move.setWeightSum(0.4f);
            if (i == 10) {

                bus_numT[i] = new TextView(this);
                bus_numT[i].setText(PlainNo);
                bus_numT[i].setTextColor(Color.RED);
                bus_numT[i].setTextSize(9);
                bus_move.addView(bus_numT[i]);

                car_image[i] = new ImageView(this);
                car_image[i].setLayoutParams(new LinearLayout.LayoutParams(160, 80));
                car_image[i].setBackgroundResource(R.drawable.car);
                bus_move.addView(car_image[i]);

            } else {
                bus_numT[i] = new TextView(this);
                bus_numT[i].setText("");
                bus_numT[i].setTextColor(Color.BLUE);
                bus_numT[i].setTextSize(9);
                bus_move.addView(bus_numT[i]);

                car_image[i] = new ImageView(this);
                car_image[i].setLayoutParams(new LinearLayout.LayoutParams(160, 80));
                car_image[i].setBackgroundResource(R.drawable.car_background);
                bus_move.addView(car_image[i]);
            }
            layout_block[i].addView(bus_move);

            // bus stop line view
            LinearLayout bus_line = new LinearLayout(this);
            bus_line.setOrientation(LinearLayout.VERTICAL);
            bus_line.setWeightSum(0.2f);
            if (i != 0) {
                ImageView line_image1 = new ImageView(this);
                line_image1.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                line_image1.setBackgroundResource(R.drawable.line);
                bus_line.addView(line_image1);
            } else {
                ImageView line_image2 = new ImageView(this);
                line_image2.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                line_image2.setBackgroundResource(R.drawable.line_background);
                bus_line.addView(line_image2);
            }

            // station image (if turn station = circle2)
            if((StationID.get(i)).equals(Trnstnid.get(i)) == true) {
                ImageView circle_image = new ImageView(this);
                circle_image.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                circle_image.setBackgroundResource(R.drawable.circle2);
                bus_line.addView(circle_image);
            }
            else {
                ImageView circle_image = new ImageView(this);
                circle_image.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                circle_image.setBackgroundResource(R.drawable.circle);
                bus_line.addView(circle_image);
            }

            if (i != busStop_num - 1) {
                ImageView line_image2 = new ImageView(this);
                line_image2.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                line_image2.setBackgroundResource(R.drawable.line);
                bus_line.addView(line_image2);
            } else {
                ImageView line_image2 = new ImageView(this);
                line_image2.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
                line_image2.setBackgroundResource(R.drawable.line_background);
                bus_line.addView(line_image2);
            }
            layout_block[i].addView(bus_line);

            // bus stop view
            LinearLayout bus_stop = new LinearLayout(this);
            bus_stop.setOrientation(LinearLayout.VERTICAL);
            param.leftMargin = 75;
            param.rightMargin = 0;
            bus_stop.setLayoutParams(param);
            bus_stop.setWeightSum(0.4f);

            bus_stopT[i] = new TextView(this);
            bus_stopT[i].setText("\n" + "[" + StationNo.get(i) + "]" + StationNm.get(i) + "\n  Begin = " + BeginTm.get(i) + " , Last = " + LastTm.get(i));
            bus_stopT[i].setTextColor(Color.BLACK);
            bus_stopT[i].setTextSize(18);
            bus_stop.addView(bus_stopT[i]);

            layout_block[i].addView(bus_stop);
        }

        // F5 function - get bus position and draw
        thread = new BackThread();
        thread.setDaemon(true);
        thread.start();
    }

    class BackThread extends Thread{
        @Override
        public void run() {
            while(!(this.isInterrupted())){
                // get current bus position
                try {
                    // getBusPosByVehIdItem
                    String URLResult3 = BusPosByVehIdItem.concat(ServiceKey).concat(BusVehId).concat(BusID);
                    // getBusRouteList function
                    URL url3 = new URL(URLResult3);

                    // get Bus Now Position Info
                    XmlPullParserFactory parserCreator3 = XmlPullParserFactory.newInstance();
                    XmlPullParser parser3 = parserCreator3.newPullParser();
                    parser3.setInput(url3.openStream(), null);
                    int parserEvent3 = parser3.getEventType();

                    while (parserEvent3 != XmlPullParser.END_DOCUMENT) {

                        switch (parserEvent3) {
                            case XmlPullParser.START_TAG:
                                if (parser3.getName().equals("plainNo")) {
                                    inPlainNo = true;
                                }
                                if (parser3.getName().equals("posX")) {
                                    inPosX = true;
                                }
                                if (parser3.getName().equals("posY")) {
                                    inPosY = true;
                                }
                                if (parser3.getName().equals("stId")) {
                                    inStId = true;
                                }
                                if (parser3.getName().equals("stopFlag")) {
                                    inStopFlag = true;
                                }
                                if (parser3.getName().equals("vehId")) {
                                    inVehId = true;
                                }
                                break;

                            case XmlPullParser.TEXT:

                                if (inPlainNo) {
                                    PlainNo = parser3.getText();
                                    inPlainNo = false;
                                }
                                if (inPosX) {
                                    PosX = parser3.getText();
                                    inPosX = false;
                                }
                                if (inPosY) {
                                    PosY = parser3.getText();
                                    inPosY = false;
                                }
                                if (inStId) {
                                    StId = parser3.getText();
                                    inStId = false;
                                }
                                if (inStopFlag) {
                                    StopFlag = parser3.getText();
                                    inStopFlag = false;
                                }
                                if (inVehId) {
                                    VehId = parser3.getText();
                                    inVehId = false;
                                }
                                break;

                            case XmlPullParser.END_TAG:
                                if (parser3.getName().equals("itemList")) {
                                    bus_move_refresh++; // bus refresh number
                                }
                                break;
                        }
                        parserEvent3 = parser3.next();
                    }
                } catch (Exception e) {
                }
                // send message
                handler.sendEmptyMessage(0);

                // 5sec -> position refresh
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if(final_num == 1) {
                        thread.interrupt();
                    }
                }
            }
        }
    }

    // draw bus position
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){   // Message id 가 0 이면
                // draw bus
                for(int k=0; k<busStop_num; k++) {

                    // bus position = openAPI's bus station
                    if((StationID.get(k)).equals(StId)) {

                        // current bus's layout position
                        now_bus_layoutPosition = k;

                        // store start station
                        if(START_st_flag == 0) {
                            String tmp = StationNm.get(now_bus_layoutPosition);
                            START_station.add(tmp);
                            START_st_flag++;
                        }


                        if(StopFlag.equals("1") == true) {
                            // set image and text
                            car_image[k].setBackgroundResource(R.drawable.car);
                            bus_numT[k].setText(PlainNo);

                            // draw bus normal position
                            ViewGroup.MarginLayoutParams margin3 = new ViewGroup.MarginLayoutParams(car_image[k].getLayoutParams());
                            margin3.setMargins(0, 60, 0, 0);
                            car_image[k].setLayoutParams(new LinearLayout.LayoutParams(margin3));

                            // draw bus text position
                            ViewGroup.MarginLayoutParams margin4 = new ViewGroup.MarginLayoutParams(bus_numT[k].getLayoutParams());
                            margin4.setMargins(0, 0, 0, 0);
                            bus_numT[k].setLayoutParams(new LinearLayout.LayoutParams(margin4));
                            bus_numT[k].setTextColor(Color.RED);

                            bus_move_refresh = 0;
                        }
                        else if(StopFlag.equals("0") == true && bus_move_refresh > 3) {

                            // draw bus under position
                            car_image[k+1].setBackgroundResource(R.drawable.car);
                            ViewGroup.MarginLayoutParams margin3 = new ViewGroup.MarginLayoutParams(car_image[k+1].getLayoutParams());
                            margin3.setMargins(0, 0, 0, 0);
                            car_image[k+1].setLayoutParams(new LinearLayout.LayoutParams(margin3));

                            bus_numT[k+1].setText(PlainNo);
                            ViewGroup.MarginLayoutParams margin4 = new ViewGroup.MarginLayoutParams(bus_numT[k].getLayoutParams());
                            margin4.setMargins(0, 0, 0, 0);
                            bus_numT[k+1].setLayoutParams(new LinearLayout.LayoutParams(margin4));
                            bus_numT[k+1].setTextColor(Color.BLUE);

                            // upper layout -> default
                            car_image[k].setBackgroundResource(R.drawable.car_background);
                            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(car_image[k].getLayoutParams());
                            margin.setMargins(0, 0, 0, 0);
                            car_image[k].setLayoutParams(new LinearLayout.LayoutParams(margin));

                            bus_numT[k].setText("");
                            ViewGroup.MarginLayoutParams margin2 = new ViewGroup.MarginLayoutParams(bus_numT[k].getLayoutParams());
                            margin2.setMargins(0, 0, 0, 0);
                            bus_numT[k].setLayoutParams(new LinearLayout.LayoutParams(margin2));

                            // set flag
                            next_layout_flag = 1;

                        }
                        else if(StopFlag.equals("0") == true && bus_move_refresh <= 3) {
                            // set image and text
                            car_image[k].setBackgroundResource(R.drawable.car);
                            bus_numT[k].setText(PlainNo);

                            // draw bus upper position
                            ViewGroup.MarginLayoutParams margin3 = new ViewGroup.MarginLayoutParams(car_image[k].getLayoutParams());
                            margin3.setMargins(0, 140, 0, 0);
                            car_image[k].setLayoutParams(new LinearLayout.LayoutParams(margin3));

                            ViewGroup.MarginLayoutParams margin4 = new ViewGroup.MarginLayoutParams(bus_numT[k].getLayoutParams());
                            margin4.setMargins(0, 0, 0, 0);
                            bus_numT[k].setLayoutParams(new LinearLayout.LayoutParams(margin4));
                            bus_numT[k].setTextColor(Color.BLUE);
                        }

                        if(k == (busStop_num-1) && StopFlag.equals("1") == true) {
                            AlertDialog.Builder dlg2 = new AlertDialog.Builder(BusLine.this);
                            dlg2.setTitle("Final Station");
                            dlg2.setMessage("The bus arrived at the final station.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // set alarm flag number
                                    final_num = 1;

                                    // move to payment page
                                    Intent intent = new Intent(getApplicationContext(), AppEndPage.class);
                                    startActivity(intent);
                                }
                            });
                            dlg2.show();
                        }
                    }
                    else if(next_layout_flag != 1) {
                        car_image[k].setBackgroundResource(R.drawable.car_background);
                        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(car_image[k].getLayoutParams());
                        margin.setMargins(0, 0, 0, 0);
                        car_image[k].setLayoutParams(new LinearLayout.LayoutParams(margin));

                        bus_numT[k].setText("");
                        ViewGroup.MarginLayoutParams margin2 = new ViewGroup.MarginLayoutParams(bus_numT[k].getLayoutParams());
                        margin2.setMargins(0, 0, 0, 0);
                        bus_numT[k].setLayoutParams(new LinearLayout.LayoutParams(margin2));

                        // set flag
                        next_layout_flag = 0;
                    }
                }

                // alarm ring check box
                if(alarm_num == (now_bus_layoutPosition+1) && StopFlag.equals("0") == true && alarm_flag == 0) {
                    // Alarm
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(5000); // 5sec

                    // show alarm message
                    AlertDialog.Builder dlg1 = new AlertDialog.Builder(BusLine.this);
                    dlg1.setTitle("End alarm");
                    dlg1.setMessage("The alarm has sounded. Get ready to get off.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // set alarm flag number
                            alarm_flag = 1;
                            
                            // get current time
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            // nowDate 변수에 값을 저장한다.
                            String formatDate = sdfNow.format(date);

                            // store last station, time and bus number
                            END_station.add(StationNm.get(now_bus_layoutPosition+1));
                            GETOFF_time.add(formatDate);
                            BOARD_num.add(RouteNm);

                            // set bus bell
                            TCPclient send_thread = new TCPclient();
                            Thread BELL_thread = new Thread(send_thread);
                            BELL_thread.start();
                        }
                    });
                    dlg1.show();
                }

                if(get_out_flag == 1) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(BusLine.this);
                    dlg.setTitle("Payment [OUT]");
                    dlg.setMessage("Pay : 0 won Complete.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final_num = 1; // thread finish flag
                            Intent intent = new Intent(getApplicationContext(), AppEndPage.class);

                            // past boarding list
                            intent.putStringArrayListExtra("START_STATION", START_station);
                            intent.putStringArrayListExtra("END_STATION", END_station);
                            intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                            intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                            // send in_price and out_price
                            ed_PRICE = "0";
                            intent.putExtra("ST_Price", st_PRICE);
                            intent.putExtra("ED_Price", ed_PRICE);

                            alarm_flag=0;
                            get_out_flag=0;
                            startActivity(intent);
                        }
                    });
                    dlg.show();
                }
            }
        }
    };

    // send bell signal to RaspberryPi
    private class TCPclient implements Runnable {
        String serverIP = BeaconIP;
        int serverPort = 7777;
        Socket inetSocket = null;

        public void run() {
            try {
                inetSocket = new Socket(serverIP, serverPort);

                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(inetSocket.getOutputStream())), true);
                    out.println("bell");

                    BufferedReader in = new BufferedReader(new InputStreamReader(inetSocket.getInputStream()));
                    String return_msg = in.readLine();

                    // payment start - get out of bus
                    // if user get out of the bus -> timer stop and go to final page
                    if(return_msg.contains("OUT")) {
                        get_out_flag = 1;
                    }

                }
                catch (Exception e) {
                }
                finally {
                    inetSocket.close();
                }
            }
            catch (Exception e) {
            }
        }
    }

}
