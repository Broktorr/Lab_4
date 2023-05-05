package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    String SENT_SMS = "SENT_SMS";
    String DELIVER_SMS = "DELIVER_SMS";

    Intent sent_intent = new Intent(SENT_SMS);
    Intent deliver_intent = new Intent(DELIVER_SMS);

    PendingIntent sent_pi, deliver_pi;
    EditText text, adress;
    Button button;

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    double azimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sent_pi = PendingIntent.getBroadcast(MainActivity.this, 0, sent_intent, PendingIntent.FLAG_IMMUTABLE);
        deliver_pi = PendingIntent.getBroadcast(MainActivity.this, 0, deliver_intent, PendingIntent.FLAG_IMMUTABLE);

        adress = (EditText) findViewById(R.id.Number);
        text = (EditText) findViewById(R.id.Text);

        button = (Button) findViewById(R.id.Send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                String message = text.getText().toString() + " Azimuth: " + azimuth;
                ArrayList<String> messageParts = smsManager.divideMessage(message);

                for (String part : messageParts) {
                    smsManager.sendTextMessage(adress.getText().toString(), null, part, sent_pi, deliver_pi);
                }
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(sentReceiver,new IntentFilter(SENT_SMS));
        registerReceiver(deliverReceiver,new IntentFilter(DELIVER_SMS));
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliverReceiver);
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(context, "Sented", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        }
    };

    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(context, "Delivered", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        }
    };
    SensorEventListener sensorEventListener = new SensorEventListener() {
        float[] mGravity;
        float[] mGeomagnetic;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float A[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(A, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientationData[] = new float[3];
                    float info [] = SensorManager.getOrientation(A, orientationData);
                    azimuth = Math.toDegrees((double) info[0]);
                    if (azimuth < 0) {
                        azimuth += 360;
                    }
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}