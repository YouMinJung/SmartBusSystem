package com.example.owner.smart_bus_system;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity {

    public static Activity MainActivity;

    EditText text1, text2, text3, text4, name, month, year, cvc, pass;
    Button complete;

    SQLiteDatabase SQLiteDB;

    // past boarding bus list
    ArrayList<String> START_station = new ArrayList<String>();
    ArrayList<String> END_station = new ArrayList<String>();
    ArrayList<String> GETOFF_time = new ArrayList<String>();
    ArrayList<String> BOARD_num = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity = MainActivity.this;

        Intent endIntent = getIntent();
        int intent_value = 0;
        intent_value = endIntent.getExtras().getInt("activityNum");

        if(intent_value == 2 || intent_value == 0) {
            // get past boarding list
            START_station = getIntent().getStringArrayListExtra("START_STATION");
            END_station = getIntent().getStringArrayListExtra("END_STATION");
            GETOFF_time = getIntent().getStringArrayListExtra("TIME_STATION");
            BOARD_num = getIntent().getStringArrayListExtra("BOARD_NUM");
        }

        SQLiteDB = init_database();
        // create new table
        init_tables();
        // load credit card information from DB
        load_values() ;

        // card number
        text1 = (EditText) findViewById(R.id.editText1);
        text2 = (EditText) findViewById(R.id.editText2);
        text3 = (EditText) findViewById(R.id.editText3);
        text4 = (EditText) findViewById(R.id.editText4);

        // user_name
        name = (EditText) findViewById(R.id.card_name);

        // card year, month and cvc num , password info
        month = (EditText) findViewById(R.id.month_card);
        year = (EditText) findViewById(R.id.year_card);
        cvc = (EditText) findViewById(R.id.cvc_card);
        pass = (EditText) findViewById(R.id.card_password);

        // complete button
        complete = (Button) findViewById(R.id.card_button);

        // button event -> store card info
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_values() ; // save card info

                Intent intent = new Intent(getApplicationContext(), AppMainPage.class);
                intent.putExtra("activityNum", 1);

                intent.putStringArrayListExtra("START_STATION", START_station);
                intent.putStringArrayListExtra("END_STATION", END_station);
                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                startActivity(intent);
            }
        });
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

    private void init_tables() {
        if (SQLiteDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CREDIT_CARD ("
                    + "NO1 " + "INTEGER NOT NULL,"
                    + "NO2 " + "INTEGER NOT NULL,"
                    + "NO3 " + "INTEGER NOT NULL,"
                    + "NO4 " + "INTEGER NOT NULL,"
                    + "NAME " + "TEXT NOT NULL,"
                    + "YEAR " + "INTEGER NOT NULL,"
                    + "MONTH " + "INTEGER NOT NULL,"
                    + "CVC " + "INTEGER NOT NULL,"
                    + "PASS " + "INTEGER NOT NULL" + ")" ;

            System.out.println(sqlCreateTbl) ;
            SQLiteDB.execSQL(sqlCreateTbl) ;
        }
    }

    private void save_values() {
        if (SQLiteDB == null) {

            String sqlInsert = "INSERT INTO CREDIT_CARD " +
                    "(NO1, NO2, NO3, NO4, NAME, YEAR, MONTH, CVC, PASS) VALUES (" +
                    text1.getText() + "," +
                    text2.getText() + "," +
                    text3.getText() + "," +
                    text4.getText() + "," +
                    "'" + name.getText() + "'," +
                    year.getText() + "," +
                    month.getText() + "," +
                    cvc.getText() +  "," +
                    pass.getText() + ")" ;

            System.out.println(sqlInsert) ;

            SQLiteDB.execSQL(sqlInsert) ;

            Toast.makeText(this, "Success Store Card information.", Toast.LENGTH_SHORT).show();
        }
        else {
            // delete
            SQLiteDB.execSQL("DELETE FROM CREDIT_CARD");

            String sqlInsert = "INSERT INTO CREDIT_CARD " +
                    "(NO1, NO2, NO3, NO4, NAME, YEAR, MONTH, CVC, PASS) VALUES (" +
                    text1.getText() + "," +
                    text2.getText() + "," +
                    text3.getText() + "," +
                    text4.getText() + "," +
                    "'" + name.getText() + "'," +
                    year.getText() + "," +
                    month.getText() + "," +
                    cvc.getText() +  "," +
                    pass.getText() + ")" ;

            System.out.println(sqlInsert) ;
            SQLiteDB.execSQL(sqlInsert) ;
        }
    }

    private void load_values() {

        String sqlQueryTbl = "SELECT * FROM CREDIT_CARD" ;
        Cursor cursor = null ;

        // execute query
        cursor = SQLiteDB.rawQuery(sqlQueryTbl, null) ;
        // record exist
        if (cursor.moveToNext()) {

                // load credit card number
                EditText TEXT1 = (EditText) findViewById(R.id.editText1);
                int num1 = cursor.getInt(0);
                TEXT1.setText(Integer.toString(num1));

                EditText TEXT2 = (EditText) findViewById(R.id.editText2);
                int num2 = cursor.getInt(1);
                TEXT2.setText(Integer.toString(num2));

                EditText TEXT3 = (EditText) findViewById(R.id.editText3);
                int num3 = cursor.getInt(2);
                TEXT3.setText(Integer.toString(num3));

                EditText TEXT4 = (EditText) findViewById(R.id.editText4);
                int num4 = cursor.getInt(3);
                TEXT4.setText(Integer.toString(num4));

                // load name
                EditText NAME = (EditText) findViewById(R.id.card_name);
                String cardName = cursor.getString(4);
                NAME.setText(cardName);

                // load year, month, cvc info
                EditText YEAR = (EditText) findViewById(R.id.year_card);
                int num5 = cursor.getInt(5);
                YEAR.setText(Integer.toString(num5));

                EditText MONTH = (EditText) findViewById(R.id.month_card);
                int num6 = cursor.getInt(6);
                MONTH.setText(Integer.toString(num6));

                EditText CVC = (EditText) findViewById(R.id.cvc_card);
                int num7 = cursor.getInt(7);
                CVC.setText(Integer.toString(num7));

                EditText PASS = (EditText) findViewById(R.id.card_password);
                int num8 = cursor.getInt(8);
                PASS.setText(Integer.toString(num8));

            Intent endIntent = getIntent();
            int intent_value = 0;
            intent_value = endIntent.getExtras().getInt("activityNum");

            if(cardName != null && intent_value == 2) {

            }
            else if (cardName != null) {
                Intent intent = new Intent(getApplicationContext(), AppMainPage.class);
                intent.putExtra("activityNum", 1);

                intent.putStringArrayListExtra("START_STATION", START_station);
                intent.putStringArrayListExtra("END_STATION", END_station);
                intent.putStringArrayListExtra("TIME_STATION", GETOFF_time);
                intent.putStringArrayListExtra("BOARD_NUM", BOARD_num);

                startActivity(intent);
            }
        }
    }
}
