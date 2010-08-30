package com.moandroid.cocos2d.actions;

import java.util.ArrayList;


public class CCScheduler {
    private static CCScheduler _sharedScheduler = null;

    public static CCScheduler sharedScheduler() {
            if (_sharedScheduler == null) {
            	_sharedScheduler = new CCScheduler();
            }
            return _sharedScheduler;
    }

	public static void purgeSharedScheduler() {
		if(_sharedScheduler != null){
			_sharedScheduler.removeAllSchedul();
			_sharedScheduler = null;
		}
	}
	
    protected CCScheduler() {
    	synchronized(CCScheduler.class){
    		
    	}
    }
    
    protected void removeAllSchedul(){
    	_scheduledMethods.clear();
    	_methodsToRemove.clear();
    	_methodsToAdd.clear();
    }
    
   	ArrayList<CCTimer> _scheduledMethods = new ArrayList<CCTimer>(50);
    ArrayList<CCTimer> _methodsToRemove = new ArrayList<CCTimer>(20);
    ArrayList<CCTimer> _methodsToAdd = new ArrayList<CCTimer>(20);

    private float _timeScale = 1.0f;

    public float timeScale() {
        return _timeScale;
    }

    public void setTimeScale(float ts) {
        _timeScale = ts;
    }

    public void schedule(CCTimer t) {
        if (_methodsToRemove.contains(t)) {
            _methodsToRemove.remove(t);
            return;
        }

        if (_scheduledMethods.contains(t) || _methodsToAdd.contains(t)) {
            return;
        }

        _methodsToAdd.add(t);
    }

    public void unschedule(CCTimer t) {
    	if (_methodsToAdd.contains(t)) {
            _methodsToAdd.remove(t);
            return;
        }

        if (!_scheduledMethods.contains(t) || _methodsToRemove.contains(t)) {
        	return;
        }

	    _methodsToRemove.add(t);
	 }

    public void tick(float dt) {
        if (_timeScale != 1.0f)
	            dt *= _timeScale;

        for (CCTimer k : _methodsToRemove)
            _scheduledMethods.remove(k);
        _methodsToRemove.clear();

        for (CCTimer k : _methodsToAdd)
            _scheduledMethods.add(k);
        _methodsToAdd.clear();

        for (CCTimer t : _scheduledMethods) {
            t.fire(dt);
        }
    }
}
