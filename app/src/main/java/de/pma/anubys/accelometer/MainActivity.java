package de.pma.anubys.accelometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int NINETYDEGREES = 90;
    private static final int OHNHUNDREDANDEIGHTYDEGREES = 180;
    private static final int TWOHUNDREDSEVENTYDEGREES = 270;
    private static final float MICROTOSECUNDE = 1000000000L;

    private float[] accelerometerValue = new float[3];
    private float[] magneticValue = new float[3];
    private float[] values = new float[3];

    private float pitch;

    private float originalPitch = 0;
    private float currentPitch = 0;
    private float originalRoll = 0;
    private float timestampDiff = 0;

    private Vibrator vibrator = null;
    private SensorManager sensorManager;


    //* ************************************************ *
    //*               L I F E - C Y C L E                *
    //* ************************************************ *
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSystemService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    //* ************************************************ *
    //*           I N I T I A L I Z A T I O N            *
    //* ************************************************ *
    private void initSystemService() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void registerListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }


    //* ************************************************* *
    //*                    S E N S O R                    *
    //* ************************************************* *
    public void onSensorChanged(SensorEvent event) {
        float[] gravity = new float[9]; // Gravity rotational data
        float[] magnetic = new float[9];    // Magnetic rotational data
        float[] outGravity = new float[9];

        getSensorType(event);

        if (magneticValue != null && accelerometerValue != null) {
            SensorManager.getRotationMatrix(gravity, magnetic, accelerometerValue, magneticValue);
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);

            if ((event.timestamp - timestampDiff) >= MICROTOSECUNDE) {
                timestampDiff = event.timestamp;

                //azimuth = values[0] * 57.2957795f;  //Azimuth z-Achse
                pitch = values[1] * 57.2957795f;    //Pitch x-Achse
                //roll = values[2] * 57.2957795f;     //Roll y-Achse
                magneticValue = null;
                accelerometerValue = null;

                setCurrentPitchValue();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //* ************************************************ *
    //*         H E L P E R  -  M E T H O D S            *
    //* ************************************************ *
    private void getSensorType(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValue = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValue = event.values.clone();
                originalPitch = event.values[1];
                originalRoll = event.values[2];
                break;
        }
    }

    private void setCurrentPitchValue() {
        //Landscape-Modus   ->  [180° >= Pitch >= -180°]
        //Quadrant I + IV
        if (originalRoll > 0) {
            if (originalPitch > 0) {
                Log.v("TAG", "Quadrand I");

                currentPitch = NINETYDEGREES - pitch;
                Log.d("TAG", "Pitch: " + pitch);
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            } else {
                Log.v("TAG", "Quadrand IV");

                currentPitch = (NINETYDEGREES - pitch) * (-1);
                Log.d("TAG", "Pitch: " + pitch);
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            }
        }

        //Quadrant II + III
        if (originalRoll < 0) {
            if (originalPitch > 0) {
                Log.v("TAG", "Quadrand II");

                currentPitch = NINETYDEGREES + pitch * (-1);

                if (currentPitch > 120) {
                    setVibration();
                }

                Log.d("TAG", "Pitch: " + pitch);
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            } else {
                Log.v("TAG", "Quadrand III");

                if (currentPitch < -120) {
                    setVibration();
                }

                currentPitch = (NINETYDEGREES - pitch) * (-1);
                Log.d("TAG", "Pitch: " + pitch);
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            }
        }
    }

    private void setVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else { // für ältere Android-Versionen
            vibrator.vibrate(1000);
        }
    }

    private void getCircleValue() {
        float tempQuadrat2;
        float tempQuadrat3;

        /*
         * Landscape-Modus
         */
        //Quadrant I + IV
        if (originalRoll > 0) {
            if (originalPitch > 0) {
                Log.v("TAG", "Quadrand I");

                currentPitch = NINETYDEGREES - pitch;
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            } else {
                Log.v("TAG", "Quadrand IV");

                currentPitch = TWOHUNDREDSEVENTYDEGREES + pitch;
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            }
        }

        //Quadrant II + III
        if (originalRoll < 0) {
            if (originalPitch > 0) {
                Log.v("TAG", "Quadrand II");

                tempQuadrat2 = pitch * (-1);
                currentPitch = NINETYDEGREES + tempQuadrat2;

                if (currentPitch > 120 && currentPitch < 180) {
                    setVibration();
                }
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            } else {
                Log.v("TAG", "Quadrand III");

                tempQuadrat3 = NINETYDEGREES - (pitch * (-1));
                currentPitch = OHNHUNDREDANDEIGHTYDEGREES + tempQuadrat3;
                Log.d("TAG", "CurrentPitch: " + currentPitch);
            }
        }
    }

}
