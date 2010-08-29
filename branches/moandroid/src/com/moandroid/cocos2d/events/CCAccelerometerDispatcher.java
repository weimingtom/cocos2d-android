package com.moandroid.cocos2d.events;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.moandroid.cocos2d.renderers.CCDirector;

public class CCAccelerometerDispatcher {
	public static final String LOG_TAG = CCAccelerometerDispatcher.class.getSimpleName();
	private static CCAccelerometerDispatcher _sharedDispatcher;
	public static CCAccelerometerDispatcher sharedDispatcher() {
		if(_sharedDispatcher == null)
			_sharedDispatcher = new CCAccelerometerDispatcher();
		return _sharedDispatcher;
	}
	
	protected CCAccelerometerDispatcher(){
		
	}
	
	public static void purgeSharedDispatcher() {
		if(_sharedDispatcher != null){
			if(_sharedDispatcher._isAccelerometerEnabled){
				Context context = CCDirector.sharedDirector().context();
				if(context != null){
					SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
					sm.unregisterListener(_sharedDispatcher._listener);
					_sharedDispatcher._isAccelerometerEnabled = false;
					_sharedDispatcher._delegates.clear();
				}							
			}
			_sharedDispatcher = null;
		}
	}


	
	class accelerometerListener implements SensorEventListener{

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//if(_sharedDispatcher==null || !_sharedDispatcher._isAccelerometerEnabled)
				//return;
				_locked = true;
	            for( CCAccelerometerDelegate delegate : _delegates ) {
	                if( delegate.accelerometerAccuracyChanged(accuracy))
	                    break;
	            }
	            updateDelegate();
	            _locked = false;
			}
			
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(_sharedDispatcher==null || !_sharedDispatcher._isAccelerometerEnabled)
				return;
			_locked = true;
            for( CCAccelerometerDelegate delegate : _delegates ) {
                if( delegate.accelerometerChanged(event.values[0],event.values[1],event.values[2]))
                    break;
            }
            updateDelegate();
            _locked = false;
		}		
	}
	
	accelerometerListener _listener = new accelerometerListener();
	boolean _isAccelerometerEnabled = false;
	public boolean isAccelerometerEnabled(){
		return _isAccelerometerEnabled;
	}
	
	protected void setIsAccelerometerEnabled(boolean enable){
		if(_isAccelerometerEnabled == enable)
			return;
		Context context = CCDirector.sharedDirector().context();
		if(context!=null){
			SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if(sensor!=null){
				if(enable){
					sm.registerListener(_listener, sensor, SensorManager.SENSOR_DELAY_GAME);
					_isAccelerometerEnabled = true;
				}else{
					sm.unregisterListener(_listener);
					_isAccelerometerEnabled = false;
				}
			}		
		}
	}
	private ArrayList<CCAccelerometerDelegate> _delegatesToAdd = new ArrayList<CCAccelerometerDelegate>(8);
	private ArrayList<CCAccelerometerDelegate> _delegatesToRemove = new ArrayList<CCAccelerometerDelegate>(8);
	
	private boolean _toAdd = false;
	private boolean _toRemove = false;
	private boolean _toQuit = false;
	private boolean _locked = false;
	private ArrayList<CCAccelerometerDelegate> _delegates = new ArrayList<CCAccelerometerDelegate>(8);
	
	public void addDelegate(CCAccelerometerDelegate delegate){
		if(!_locked){
			setIsAccelerometerEnabled(true);
			if(!_delegates.contains(delegate)){
				_delegates.add(delegate);
			}			
		}else{
			_delegatesToAdd.add(delegate);
			_toAdd = true;
		}
	}
	
	public void removeDelegate(CCAccelerometerDelegate delegate){
		if(!_locked){
			_delegates.remove(delegate);
			if(_delegates.isEmpty()){
				_isAccelerometerEnabled = false;
			}
		}else{
			_delegatesToRemove .add(delegate);
			_toRemove = true;
		}
	}
	
	public void removeAllDelegate(){
		if(!_locked){
			_delegates.clear();
			_isAccelerometerEnabled = false;
		}else{
			_toQuit = true;
		}
	}
	
	protected void updateDelegate(){
		if(_toRemove){
			_toRemove = false;
			for(CCAccelerometerDelegate delegate:_delegatesToRemove){
				removeDelegate(delegate);
			}
			_delegatesToRemove.clear();
		}
		if(_toAdd){
			_toAdd = false;
			for(CCAccelerometerDelegate delegate:_delegatesToAdd){
				addDelegate(delegate);
			}
			_delegatesToAdd.clear();
		}
		if(_toQuit){
			_toQuit = false;
			removeAllDelegate();
		}
	}
}
