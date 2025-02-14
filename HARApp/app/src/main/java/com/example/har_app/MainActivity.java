package com.example.har_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener, PopupMenu.OnMenuItemClickListener {

    private static final int N_SAMPLES = 100;
    private static int prevIdx = -1;

    private static List<Float> ax;
    private static List<Float> ay;
    private static List<Float> az;

    private static List<Float> lx;
    private static List<Float> ly;
    private static List<Float> lz;

    private static List<Float> gx;
    private static List<Float> gy;
    private static List<Float> gz;

    private static List<Float> ma;
    private static List<Float> ml;
    private static List<Float> mg;

    private List<String> actList;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mLinearAcceleration;
    private Sensor mSteps;

    public static float[] timeArray;
    public static float t0, tf, el;
    public static boolean started = false;
    private int currentActivityID;

    public static  String steps_count = "";

    private TextView downstairsTextView;
    private TextView joggingTextView;
    private TextView sittingTextView;
    private TextView standingTextView;
    private TextView upstairsTextView;
    private TextView walkingTextView;
    private TextView bikingTextView;
    private ImageView currentActivityImageView;



    private TableRow bikingTableRow;
    private TableRow downstairsTableRow;
    private TableRow joggingTableRow;
    private TableRow sittingTableRow;
    private TableRow standingTableRow;
    private TableRow upstairsTableRow;
    private TableRow walkingTableRow;



    Button button;
    Boolean buttons_visibility;

    private MediaPlayer mediaPlayer;

    private TextToSpeech textToSpeech;
    private float[] results;
    private Classifier classifier;

    private String[] labels = {"Biking","Downstairs", "Jogging", "Sitting", "Standing", "Upstairs", "Walking"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ax = new ArrayList<>(); ay = new ArrayList<>(); az = new ArrayList<>();
        lx = new ArrayList<>(); ly = new ArrayList<>(); lz = new ArrayList<>();
        gx = new ArrayList<>(); gy = new ArrayList<>(); gz = new ArrayList<>();
        ma = new ArrayList<>(); ml = new ArrayList<>(); mg = new ArrayList<>();

        downstairsTextView = findViewById(R.id.downstairs_prob);
        joggingTextView = findViewById(R.id.jogging_prob);
        sittingTextView = findViewById(R.id.sitting_prob);
        standingTextView = findViewById(R.id.standing_prob);
        upstairsTextView = findViewById(R.id.upstairs_prob);
        walkingTextView = findViewById(R.id.walking_prob);
        bikingTextView = findViewById((R.id.biking_prob));
        currentActivityImageView = findViewById(R.id.current_activity_image);

        bikingTableRow = (TableRow) findViewById(R.id.biking_row);
        downstairsTableRow = (TableRow) findViewById(R.id.downstairs_row);
        joggingTableRow = (TableRow) findViewById(R.id.jogging_row);
        sittingTableRow = (TableRow) findViewById(R.id.sitting_row);
        standingTableRow = (TableRow) findViewById(R.id.standing_row);
        upstairsTableRow = (TableRow) findViewById(R.id.upstairs_row);
        walkingTableRow = (TableRow) findViewById(R.id.walking_row);




        actList = new ArrayList<>();

        buttons_visibility = true;

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (buttons_visibility){
                    bikingTableRow.setVisibility(View.INVISIBLE);
                    downstairsTableRow.setVisibility(View.INVISIBLE);
                    joggingTableRow.setVisibility(View.INVISIBLE);
                    sittingTableRow.setVisibility(View.INVISIBLE);
                    standingTableRow.setVisibility(View.INVISIBLE);
                    upstairsTableRow.setVisibility(View.INVISIBLE);
                    walkingTableRow.setVisibility(View.INVISIBLE);
                    buttons_visibility = false;
                    button.setText("SHOW PROBABILITIES");
                } else {
                    bikingTableRow.setVisibility(View.VISIBLE);
                    downstairsTableRow.setVisibility(View.VISIBLE);
                    joggingTableRow.setVisibility(View.VISIBLE);
                    sittingTableRow.setVisibility(View.VISIBLE);
                    standingTableRow.setVisibility(View.VISIBLE);
                    upstairsTableRow.setVisibility(View.VISIBLE);
                    walkingTableRow.setVisibility(View.VISIBLE);
                    buttons_visibility = true;
                    button.setText("HIDDEN PROBABILITIES");
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mLinearAcceleration , SensorManager.SENSOR_DELAY_FASTEST);

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope , SensorManager.SENSOR_DELAY_FASTEST);

        mSteps = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mSteps , SensorManager.SENSOR_DELAY_FASTEST);

        classifier = new Classifier(getApplicationContext());

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        mediaPlayer = MediaPlayer.create(this, R.raw.rocky);

    }

    // Initializes the app sensor manager
    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    // App closed
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        //mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    // Search for the max probability and if activity has changed say it on speakers.
    @Override
    public void onInit(int status) {
        Timer timer = new Timer();



        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {

                        if (results == null || results.length == 0) {
                            return;
                        }
                        float max = -1;
                        int idx = -1;
                        for (int i = 0; i < results.length; i++) {
                            if (results[i] > max) {
                                idx = i;
                                max = results[i];
                            }
                        }

                        if (max > 0.75 && idx != prevIdx) {
                            Log.i("ON INIT", "SPEAKING");
                            if (idx == 3) {
                                Log.i("BEFORE CLEARING", "BEFORE CLEARING");
                                actList.clear();
                            } else {
                                actList.add(labels[idx]);
                            }
                            currentActivityID = idx;
                            setCurrentActivity(idx);
                            textToSpeech.speak(labels[idx], TextToSpeech.QUEUE_ADD, null,
                                    Integer.toString(new Random().nextInt()));
                            if (started) {
                                tf = SystemClock.elapsedRealtime();
                                //Log.i("T0", String.valueOf(t0));
                                //Log.i("Tf", String.valueOf(tf));
                                el = (tf - t0) / 1000.0f;
                                //Log.i("El", String.valueOf(el));
                                timeArray[idx] = timeArray[idx] + el;
                            } else {
                                timeArray = new float[8];
                                for (int i = 0; i < 8; ++i) {
                                    timeArray[i] = 0.0f;
                                }
                                t0 = 0.0f;
                                tf = 0.0f;
                                el = 0.0f;
                                started = true;
                            }
                            prevIdx = idx;
                            t0 = SystemClock.elapsedRealtime();
                        }
                    }
                });
            }
        }, 1000, 3000);

    }

    // When sensor changes, update the probabilities and get new data
    @Override
    public void onSensorChanged(SensorEvent event) {

        activityPrediction();

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax.add(event.values[0]);
            ay.add(event.values[1]);
            az.add(event.values[2]);
        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lx.add(event.values[0]);
            ly.add(event.values[1]);
            lz.add(event.values[2]);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0]);
            gy.add(event.values[1]);
            gz.add(event.values[2]);
        } else if (sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if (currentActivityID == 1 || currentActivityID == 2 || currentActivityID == 5 || currentActivityID == 6){
                steps_count = String.valueOf(event.values[3]);
                System.out.println(steps_count);
            }
        }
    }

    // Necessary to exist due to the implemented class
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Calculate probability of each activity (with classifier) and puts it on textView
    private void activityPrediction() {

        List<Float> data = new ArrayList<>();

        if (ax.size() >= N_SAMPLES && ay.size() >= N_SAMPLES && az.size() >= N_SAMPLES
                && lx.size() >= N_SAMPLES && ly.size() >= N_SAMPLES && lz.size() >= N_SAMPLES
                && gx.size() >= N_SAMPLES && gy.size() >= N_SAMPLES && gz.size() >= N_SAMPLES
        ) {
            double maValue; double mgValue; double mlValue;

            for( int i = 0; i < N_SAMPLES ; i++ ) {
                maValue = Math.sqrt(Math.pow(ax.get(i), 2) + Math.pow(ay.get(i), 2) + Math.pow(az.get(i), 2));
                mlValue = Math.sqrt(Math.pow(lx.get(i), 2) + Math.pow(ly.get(i), 2) + Math.pow(lz.get(i), 2));
                mgValue = Math.sqrt(Math.pow(gx.get(i), 2) + Math.pow(gy.get(i), 2) + Math.pow(gz.get(i), 2));

                ma.add((float)maValue);
                ml.add((float)mlValue);
                mg.add((float)mgValue);
            }

            data.addAll(ax.subList(0, N_SAMPLES));
            data.addAll(ay.subList(0, N_SAMPLES));
            data.addAll(az.subList(0, N_SAMPLES));

            data.addAll(lx.subList(0, N_SAMPLES));
            data.addAll(ly.subList(0, N_SAMPLES));
            data.addAll(lz.subList(0, N_SAMPLES));

            data.addAll(gx.subList(0, N_SAMPLES));
            data.addAll(gy.subList(0, N_SAMPLES));
            data.addAll(gz.subList(0, N_SAMPLES));

            data.addAll(ma.subList(0, N_SAMPLES));
            data.addAll(ml.subList(0, N_SAMPLES));
            data.addAll(mg.subList(0, N_SAMPLES));

            results = classifier.predictProbabilities(toFloatArray(data));

            float max = -1;
            int idx = -1;
            for (int i = 0; i < results.length; i++) {
                if (results[i] > max) {
                    idx = i;
                    max = results[i];
                }
            }

            //Log.i("Probabilities: ", Arrays.toString(results));
            setProbabilities();
            //if(results[idx] > 0.75 && idx != prevIdx) {
            //   setCurrentActivity(idx);
            //}
/*            if(results[idx] > 0.50 && idx != prevIdx){
                Log.i("Probabilities: ", "IN");
                setCurrentActivity(idx);
                textToSpeech.speak(labels[idx], TextToSpeech.QUEUE_ADD, null,
                        Integer.toString(new Random().nextInt()));
                prevIdx = idx;
            }*/




            data.clear();
            ax.clear(); ay.clear(); az.clear();
            lx.clear(); ly.clear(); lz.clear();
            gx.clear(); gy.clear(); gz.clear();
            ma.clear(); ml.clear(); mg.clear();

        }
    }

    // Writes measured probabilities in app textView
    private void setProbabilities() {
        bikingTextView.setText(Float.toString((round(results[0], 2))));
        downstairsTextView.setText(Float.toString(round(results[1], 2)));
        joggingTextView.setText(Float.toString(round(results[2], 2)));
        sittingTextView.setText(Float.toString(round(results[3], 2)));
        standingTextView.setText(Float.toString(round(results[4], 2)));
        upstairsTextView.setText(Float.toString(round(results[5], 2)));
        walkingTextView.setText(Float.toString(round(results[6], 2)));
    }

    // Set current activity in App View
    private void setCurrentActivity(int idx) {


        if (mediaPlayer.isPlaying() && idx != 2) {
            mediaPlayer.pause();
        }
        if (idx == 0){
            Drawable drawable = getDrawable(R.drawable.biking);
            currentActivityImageView.setImageDrawable(drawable);
        }
        else if (idx == 1){
            Drawable drawable = getDrawable(R.drawable.downstairs);
            currentActivityImageView.setImageDrawable(drawable);
        }
        else if (idx == 2){
            Drawable drawable = getDrawable(R.drawable.jogging);
            currentActivityImageView.setImageDrawable(drawable);
            mediaPlayer.start();
        }
        else if (idx == 3){
            actList.add("Sitting");
            Drawable drawable = getDrawable(R.drawable.sitting);
            currentActivityImageView.setImageDrawable(drawable);
            //Log.i("WAKALA", String.valueOf(listToString()));
            //Log.i("WAKALA", String.valueOf(actList.size()));
            if (actList.size() > 1 && actList.get(actList.size()-2) != "Sitting")
                showModal();
        }
        else if (idx == 4){
            Drawable drawable = getDrawable(R.drawable.standing);
            currentActivityImageView.setImageDrawable(drawable);
        }
        else if (idx == 5){
            Drawable drawable = getDrawable(R.drawable.upstairs);
            currentActivityImageView.setImageDrawable(drawable);
        }
        else if (idx == 6){
            Drawable drawable = getDrawable(R.drawable.walking);
            currentActivityImageView.setImageDrawable(drawable);
        }
    }

    private String listToString(){
        String r = "";
        for (String s : actList)
            r = r + s +",";
        return  r;
    }

    private void   showModal(){
// Instanciamos un nuevo AlertDialog Builder y le asociamos titulo y mensaje

        String mssg = listToString();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(mssg).setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

        AlertDialog title = alertDialogBuilder.create();
        title.setTitle("Lista de actividades");
        title.show();


        /*// Creamos un nuevo OnClickListener para el boton Cancelar
        DialogInterface.OnClickListener listenerCancelar = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };

        // Asignamos los botones positivo y negativo a sus respectivos listeners
        alertDialogBuilder.setNegativeButton(R.string.ok, listenerCancelar);

        return alertDialogBuilder.create();*/
    }

    // Looks for null values in float array
    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    // Round a float with input decimal places
    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    // Returns sensor manager
    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item1){
            Intent intent = new Intent(MainActivity.this, Records.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, Records.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

     */

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
        if (id == R.id.item2){
            Intent intent = new Intent(MainActivity.this, Records.class);
            startActivity(intent);
            return  true;
        } else if (id == R.id.item3) {
            Intent intent = new Intent(MainActivity.this, StepCounter.class);
            startActivity(intent);
            return  true;
        } else {
            return false;
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}