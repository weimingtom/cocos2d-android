package com.moandroid.cocos2d.events;

import android.hardware.SensorEvent;

public interface CCAccelerometerDelegate {
	
	boolean accelerometerChanged(SensorEvent event);
	boolean accelerometerAccuracyChanged(int accuracy);
	public boolean accelerometerChanged(float accelX, float accelY, float accelZ);
}