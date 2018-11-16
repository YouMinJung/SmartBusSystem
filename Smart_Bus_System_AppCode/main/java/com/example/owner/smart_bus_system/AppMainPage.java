package com.example.owner.smart_bus_system;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Region;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by OWNER on 2018-03-27.
 */
public class AppMainPage extends Activity implements BeaconConsumer {

    public static Activity AppMainPage;

    ImageButton credit, face;
    TextView user_name;

    SQLiteDatabase SQLiteDB;

    LinearLayout buslist;
    TextView bus[];
    TextView bus2[];

    // beacon
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();

    Long BUSID, IP1, IP2, IP3, IP4;
    String BUSNUM, BUSTYPE;

    //Bluetooth
    private BluetoothAdapter mBluetoothAdapter=null;

    //internet status check
    public static final String WIFE_STATE = "WIFE";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";

    // send image siganl
    ArrayList img_signal = new ArrayList();

    // nearest Beacon
    Double Near_distance = 99999.0;
    String IPnumber = null;

    // payment signal
    int payment = 0;

    // payment Alert num
    int pay_num = 0;

    // past boarding bus list
    ArrayList<String> START_station = new ArrayList<String>();
    ArrayList<String> END_station = new ArrayList<String>();
    ArrayList<String> GETOFF_time = new ArrayList<String>();
    ArrayList<String> BOARD_num = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_mainpage);

        AppMainPage = AppMainPage.this;

        Intent endIntent = getIntent();
        int intent_value = 0;
        intent_value = endIntent.getExtras().getInt("activityNum");

        if(intent_value == 4){
            // get past boarding list
            START_station = getIntent().getStringArrayListExtra("START_STATION");
            END_station = getIntent().getStringArrayListExtra("END_STATION");
            GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
            BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");

            AppEndPage appEndPage = (AppEndPage) AppEndPage.AppEndPage;
            appEndPage.finish();
        }
        else if(intent_value == 1) {
            // get past boarding list
            START_station = getIntent().getStringArrayListExtra("START_STATION");
            END_station = getIntent().getStringArrayListExtra("END_STATION");
            GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
            BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");

            MainActivity mainActivity = (MainActivity) MainActivity.MainActivity;
            mainActivity.finish();
        }

        //bluetooth connection
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null) {
            Toast.makeText(this, "Can't use Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        else {
            if(!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            else {
                mBluetoothAdapter.enable();
            }
        }

        // check current internet connection
        String getNetwork = getWhatKindOfNetwork(getApplication());
        if(getNetwork.equals("NONE")){
            AlertDialog.Builder dlg = new AlertDialog.Builder(AppMainPage.this);
            dlg.setTitle("Set Alarm");
            dlg.setMessage("Check your Internet connection... It's OFF").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            dlg.show();
        }

        // credit card button
        credit = (ImageButton) findViewById(R.id.credit_card);

        // button event -> store card info
        credit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handler.removeMessages(0);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("activityNum", 2);

                intent.putStringArrayListExtra("START_STATION", START_station);
                intent.putStringArrayListExtra("END_STATION", END_station);
                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                startActivity(intent);
                finish();
            }
        });

        // face photo button
        face = (ImageButton) findViewById(R.id.face_photo);

        // button event -> store card info
        face.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handler.removeMessages(0);
                Intent intent = new Intent(getApplicationContext(), PhotoPage.class);
                intent.putExtra("activityNum", 2);

                intent.putStringArrayListExtra("START_STATION", START_station);
                intent.putStringArrayListExtra("END_STATION", END_station);
                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                startActivity(intent);
                finish();
            }
        });

        // user name info
        user_name = (TextView) findViewById(R.id.user_name);

        SQLiteDB = init_database();
        // bring user name from DB
        String sqlQueryTbl = "SELECT NAME FROM CREDIT_CARD";
        Cursor cursor = null;

        cursor = SQLiteDB.rawQuery(sqlQueryTbl, null);
        // record exist
        if (cursor.moveToNext()) {
            String cardName = cursor.getString(0);
            user_name.setText(cardName);
        }

        // get BUS info from Beacon
        // init beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // start to find beacon
        beaconManager.bind(this);


        // print Bus List info to text view
        buslist = (LinearLayout) findViewById(R.id.BusList);
        bus = new TextView[6];
        bus2 = new TextView[6];

        for (int i = 0; i < 6; i++) {
            // create TextView
            bus[i] = new TextView(this);
            bus[i].setText("");
            bus[i].setTextColor(Color.RED);
            bus[i].setTextSize(20);

            // create TextView
            bus2[i] = new TextView(this);
            bus2[i].setText("");
            bus2[i].setTextColor(Color.BLACK);
            bus2[i].setTextSize(15);

            // set layout_width, layout_height, gravity
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            bus[i].setLayoutParams(lp);
            buslist.addView(bus[i]);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
            lp2.gravity = Gravity.CENTER_HORIZONTAL;
            bus2[i].setLayoutParams(lp2);
            buslist.addView(bus2[i]);

            // blank text - new line
            TextView blank = new TextView(this);
            blank.setText("\n\n\n");
            LinearLayout.LayoutParams blank_lp = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
            blank.setLayoutParams(blank_lp);
            buslist.addView(blank);
        }

        // start handler
        handler.sendEmptyMessage(0);
    }

    private SQLiteDatabase init_database() {
        SQLiteDatabase db = null;

        File file = new File(getFilesDir(), "smartBus_card.db");

        System.out.println("PATH : " + file.toString());
        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null);
        }
        catch (SQLiteException e) {
            e.printStackTrace() ;
        }

        if (db == null) {
            System.out.println("DB creation failed. " + file.getAbsolutePath()) ;
        }

        return db ;
    }

    // check internet access available
    public static String getWhatKindOfNetwork(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return WIFE_STATE;
            }
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_STATE;
            }
        }
        return NONE_STATE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    // if beacon find
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, org.altbeacon.beacon.Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new org.altbeacon.beacon.Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            int i=0;
            // print beacon's info
            for(Beacon beacon : beaconList){

                Identifier id_info1 = beacon.getId1();
                Identifier id_info2 = beacon.getId2();
                Identifier id_info3 = beacon.getId3();

                String uuid_string = id_info1.toString();
                String IDnum[] = uuid_string.split("-");
                BUSNUM = id_info2.toString();
                BUSTYPE = id_info3.toString();

                BUSID = Long.parseLong(IDnum[0], 16);
                IP1 = Long.parseLong(IDnum[1], 16);
                IP2 = Long.parseLong(IDnum[2], 16);
                IP3 = Long.parseLong(IDnum[3], 16);
                IP4 = Long.parseLong(IDnum[4], 16);

                if(BUSTYPE.equals("1")== true) {
                    bus[i].setText("[Airport Bus]\t\tBus Number : "+BUSNUM);
                }
                else if(BUSTYPE.equals("2")== true) {
                    bus[i].setText("[Village Bus]\t\tBus Number : "+BUSNUM);
                }
                else if(BUSTYPE.equals("3")== true) {
                    bus[i].setText("[Green Bus]\t\tBus Number : "+BUSNUM);
                }
                else if(BUSTYPE.equals("4")== true) {
                    bus[i].setText("[Blue Bus]\t\tBus Number : "+BUSNUM);
                }
                else if(BUSTYPE.equals("4")== true) {
                    bus[i].setText("[Yellow Bus]\t\tBus Number : "+BUSNUM);
                }
                else if(BUSTYPE.equals("6")== true) {
                    bus[i].setText("[Red Bus]\t\tBus Number : "+BUSNUM);
                }

                bus2[i].setText("Distance : "+Double.parseDouble(String.format("%.3f", beacon.getDistance())));

                if(Double.parseDouble(String.format("%.3f", beacon.getDistance())) < Near_distance) {
                    Near_distance = Double.parseDouble(String.format("%.3f", beacon.getDistance()));

                    IPnumber = Long.toString(IP1);
                    IPnumber = IPnumber.concat(".");
                    IPnumber = IPnumber.concat(Long.toString(IP2));
                    IPnumber = IPnumber.concat(".");
                    IPnumber = IPnumber.concat(Long.toString(IP3));
                    IPnumber = IPnumber.concat(".");
                    IPnumber = IPnumber.concat(Long.toString(IP4));

                    if(img_signal.contains(IPnumber) == false) {
                        img_signal.add(new String(IPnumber));

                        // send img
                        TCPclient1 sendIMG_thread = new TCPclient1();
                        Thread IMG_thread = new Thread(sendIMG_thread);
                        IMG_thread.start();

                    }
                    // already send img
                    // so, now just listen 'payOK' message
                    else {
                        TCPclient2 receiveMSG_thread = new TCPclient2();
                        Thread MEG_thread = new Thread(receiveMSG_thread);
                        MEG_thread.start();
                    }
                }
                i++;
            }

            Near_distance = 99999.0;
            IPnumber = null;

            if(payment == 1 && pay_num == 0) {
                pay_num = 1;
                // pay complete so, go to BusLine Page
                AlertDialog.Builder dlg = new AlertDialog.Builder(AppMainPage.this);
                dlg.setTitle("Payment [IN]");
                dlg.setMessage("Pay : 1200 won Complete.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), BusLine.class);
                        intent.putExtra("BusID_Num", BUSID);
                        intent.putExtra("BusNumber_Num", BUSNUM);
                        intent.putExtra("IP_Num1", IP1);
                        intent.putExtra("IP_Num2", IP2);
                        intent.putExtra("IP_Num3", IP3);
                        intent.putExtra("IP_Num4", IP4);

                        intent.putStringArrayListExtra("START_STATION", START_station);
                        intent.putStringArrayListExtra("END_STATION", END_station);
                        intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                        intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                        intent.putExtra("ST_Price", "1200");

                        payment=0;
                        pay_num=0;
                        startActivity(intent);
                    }
                });
                dlg.show();
            }
            // term is 1sec
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    // send image to RaspberryPi
    private class TCPclient1 implements Runnable {
        String serverIP = IPnumber;
        int serverPort = 9999;
        Socket inetSocket = null;

        public void run() {
            try {
                inetSocket = new Socket(serverIP, serverPort);

                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(inetSocket.getOutputStream())), true);
                    out.println(getLocalIpAddress()); // send ip address
                    out.flush();

                    DataInputStream dis = new DataInputStream(new FileInputStream(new File("/storage/emulated/0/Pictures/SmartBus_Picture/IMG"+".jpg")));
                    DataOutputStream dos = new DataOutputStream(inetSocket.getOutputStream());

                    byte[] buf = new byte[4000000];

                    int readBytes;
                    while((readBytes = dis.read(buf)) > 0)
                    {
                        dos.write(buf, 0, readBytes);
                        dos.flush();
                    }
                    dos.close();
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

    // get 'payOK' message and then send payment price
    private class TCPclient2 implements Runnable {
        int serverPort = 8888;
        ServerSocket serverSocket = null;
        Socket client = null;

        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);
                client = serverSocket.accept();

                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String str = in.readLine();

                    if(str.equals("PayOK") == true) {
                        // pay bus fee
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                        out.println("1200");

                        // set payment signal -> payment complete
                        payment = 1;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    client.close();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // get current IP address
    public static String getLocalIpAddress() {
        String ip = null;

        ConnectivityManager manager = (ConnectivityManager) AppMainPage.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wificon = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

        if(wificon == true){
            // get WIFI info
            WifiManager wifimanager = (WifiManager) AppMainPage.getSystemService(Context.WIFI_SERVICE);

            DhcpInfo dhcpInfo = wifimanager.getDhcpInfo();
            int wIp = dhcpInfo.ipAddress;

            ip = String.format("%d.%d.%d.%d", (wIp & 0xff), (wIp >> 8 & 0xff), (wIp >> 16 & 0xff), (wIp >> 24 & 0xff));
        }
        else {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            ip = inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }

        return ip;
    }

}


