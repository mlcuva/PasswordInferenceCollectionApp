package org.mlcuva.savefile;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private boolean updating = false;
    private OutputStreamWriter outWriter;
    private FileOutputStream fOutStream;
    private int count = 0;
    private ArrayList<String> data_1000 = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    outWriter.close();
                    fOutStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button button_toggle = (Button) findViewById(R.id.button3);
        button_toggle.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    writeFile("data.csv");
                    update_sensor(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.addTextChangedListener(new TextWatcher() {
            int prevLength = -1;
            int currentIndex;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentIndex = start+count-1;
                if (prevLength > currentIndex){
                    System.out.println("BACK");
                } else {
                    System.out.println(s.charAt(start + count - 1));
                }
                prevLength = currentIndex;
                System.out.println("Start: " + start);
                System.out.println("Before: " + before);
                System.out.println("Count: " + count);

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                /*
                if (s.length() > prevLength) {
                    System.out.println(s.charAt(s.length() - 1));
                    prevLength = s.length();
                }
                */
            }
        });
    }

    public void writeFile(String filename) {
        try {

            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, filename);
            TextView label1 = (TextView) findViewById(R.id.label1);
            label1.setText(dir.getAbsolutePath() + filename);

            fOutStream = new FileOutputStream(file);
            outWriter = new OutputStreamWriter(fOutStream);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update_sensor(View v){
        updating = !updating;
    }

    public String collectAccelData(){
        int x_accel;
        int y_accel;
        int z_accel;
        return "";
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (updating) {
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 20) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
            TextView xyz = (TextView) findViewById(R.id.xyz);
            xyz.setText(last_x + " " + last_y + " " + last_z);
            data_1000.add(System.currentTimeMillis() + "," + last_x + "," + last_y + "," + last_z + "\n");
            count += 1;
            try {
                if (count > 10){
                        for (int i = 0; i < data_1000.size(); i++) {
                            outWriter.write(data_1000.get(i));
                        }
                        //Log.d("INFO","Saving... ");
                }
            }catch (IOException e) {
                System.out.println("Closed file...");
                //e.printStackTrace();
            }
        }
    }
    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println(keyCode);
        return true;
    }
    */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
