package mlevy94.robiny.gettingwarmer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by lucun_000 on 4/9/2016.
 */
public class DirectionManager implements SensorEventListener{
    MainActivity mainActivity;
    SensorManager sensorManager;
    Sensor accSensor;
    Sensor magSensor;
    float[] accData;
    float[] magData;

    public DirectionManager(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        sensorManager = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void register(){
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unregister(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.equals(accSensor)){
            accData=event.values;
        }
        if(event.sensor.equals(magSensor)){
            magData=event.values;
        }
        if(accData!=null&&magData!=null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, accData, magData)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                double angle = orientation[0] * 360 / (2 * Math.PI);
                angle += 180;
                mainActivity.updateDirection(angle);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
