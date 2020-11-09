package com.example.har_app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Records extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, TextToSpeech.OnInitListener{

    boolean init = false;
    private TextToSpeech textToSpeech;

    private  TextView downstairsTimeTextView;
    private  TextView joggingTimeTextView;
    private  TextView sittingTimeTextView;
    private  TextView standingTimeTextView;
    private  TextView upstairsTimeTextView;
    private  TextView walkingTimeTextView;
    private  TextView bikingTimeTextView;

    public  void  onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records);
        downstairsTimeTextView = findViewById(R.id.downstairs_time);
        joggingTimeTextView = findViewById(R.id.jogging_time);
        sittingTimeTextView = findViewById(R.id.sitting_time);
        standingTimeTextView = findViewById(R.id.standing_time);
        upstairsTimeTextView = findViewById(R.id.upstairs_time);
        walkingTimeTextView = findViewById(R.id.walking_time);
        bikingTimeTextView = findViewById((R.id.biking_time));
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);
    }

    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy(){
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onInit(int status) {


        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        Log.i("Times: ", Arrays.toString(MainActivity.timeArray));

                        if (MainActivity.timeArray == null || MainActivity.timeArray.length == 0) {
                            return;
                        }
                        setTime();
                    }
                });

            }
        }, 1000, 3000);

    }

    private  void setTime(){
        bikingTimeTextView.setText(Float.toString((round(MainActivity.timeArray[0], 2))) + 's');
        downstairsTimeTextView.setText(Float.toString(round(MainActivity.timeArray[1], 2))+ 's');
        joggingTimeTextView.setText(Float.toString(round(MainActivity.timeArray[2], 2))+ 's');
        sittingTimeTextView.setText(Float.toString(round(MainActivity.timeArray[3], 2))+ 's');
        standingTimeTextView.setText(Float.toString(round(MainActivity.timeArray[4], 2))+ 's');
        upstairsTimeTextView.setText(Float.toString(round(MainActivity.timeArray[5], 2))+ 's');
        walkingTimeTextView.setText(Float.toString(round(MainActivity.timeArray[6], 2))+ 's');
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.actions);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item1){
            Intent intent = new Intent(Records.this, MainActivity.class);
            startActivity(intent);
            return  true;
        } else if (id == R.id.item3) {
            Intent intent = new Intent(Records.this, StepCounter.class);
            startActivity(intent);
            return  true;
        } else {
            return false;
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    // Round a float with input decimal places
    private static float round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
