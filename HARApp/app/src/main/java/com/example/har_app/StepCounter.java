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

public class StepCounter extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, TextToSpeech.OnInitListener{

    private TextToSpeech textToSpeech;

    private TextView tv_steps;

    public  void  onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stepcounter);
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);
        tv_steps = findViewById(R.id.steps_count);
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
        System.out.println(MainActivity.steps_count);
        Log.i("WAKALA", "WAKALA");
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        setSteps();
                    }
                });

            }
        }, 1000, 3000);

    }

    private void setSteps(){
        Log.i("ON STEPPING: ", MainActivity.steps_count);
        tv_steps.setText(MainActivity.steps_count);
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
            Intent intent = new Intent(StepCounter.this, MainActivity.class);
            startActivity(intent);
            return  true;
        } else if (id == R.id.item2) {
                Intent intent = new Intent(StepCounter.this, Records.class);
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
