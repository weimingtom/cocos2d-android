package com.moandroid.cocos2d.events;

import java.util.ArrayList;

import android.view.MotionEvent;

public class CCTouchDispatcher {
	public static final String LOG_TAG = CCTouchDispatcher.class.getSimpleName();
	private static CCTouchDispatcher _sharedDispatcher;
	public static CCTouchDispatcher sharedDispatcher() {
		if(_sharedDispatcher == null)
			_sharedDispatcher = new CCTouchDispatcher();
		return _sharedDispatcher;
	}
	
	public static void purgeSharedDispatcher() {
		if(_sharedDispatcher != null){
			_sharedDispatcher.removeAllDelegates();
			_sharedDispatcher = null;
		}
	}
	
	public CCTouchDispatcher(){
	}
	
	private boolean _dispatchEvents = true;
	public boolean _dispatchEvents(){
		return _dispatchEvents;
	}
	public void set_dispatchEvents(boolean b){
		_dispatchEvents = b;
	}

	private ArrayList<CCTouchHandler> _targetedHandlers = new ArrayList<CCTouchHandler>(8);
	private ArrayList<CCTouchHandler> _standardHandlers = new ArrayList<CCTouchHandler>(4);
	private ArrayList<CCTouchHandler> _handlersToAdd = new ArrayList<CCTouchHandler>(8);
	private ArrayList<CCTouchDelegate> _handlersToRemove = new ArrayList<CCTouchDelegate>(8);
	
	private boolean _toAdd = false;
	private boolean _toRemove = false;
	private boolean _toQuit = false;
	private boolean _locked = false;
		
	private void forceAddHandler(CCTouchHandler handler, ArrayList<CCTouchHandler> array){
		int i = 0;
		for(CCTouchHandler h : array){
			if(h.priority() < handler.priority())
				++i;
			if( h.delegate() == handler.delegate() )
                return;
		}
		array.add(i, handler);
	}

	public void addDelegate(CCStandardTouchDelegate delegate, int priority){
		CCTouchHandler handler = CCStandardTouchHandler.handler(delegate, priority);
		if(!_locked){
			forceAddHandler(handler, _standardHandlers);
		}else{
			_handlersToAdd.add(handler);
			_toAdd = true;
		}
	}
	
	public void addDeleget(CCTargetedTouchDelegate delegate, int priority){
		CCTouchHandler handler = CCTargetedTouchHandler.handler(delegate, priority);
		if(!_locked){
			forceAddHandler(handler, _targetedHandlers);
		}else{
			_handlersToAdd.add(handler);
			_toAdd = true;
		}
	}	
	
	private void forceRemoveDelegate(CCTouchDelegate delegate){
		for(CCTouchHandler handler : _targetedHandlers){
			if(handler.delegate() == delegate){
				_targetedHandlers.remove(handler);
				break;
			}
		}
		for(CCTouchHandler handler : _standardHandlers){
			if(handler.delegate() == delegate){
				_standardHandlers.remove(handler);
				break;
			}
		}
	}

	public void removeDelegate(CCTouchDelegate delegate){
		if(delegate == null)
			return;
		if(!_locked){
			forceRemoveDelegate(delegate);
		}else{
			_handlersToRemove.add(delegate);
			_toRemove = true;
		}
	}
	
	private void forceRemoveAllDelegates(){
		_standardHandlers.clear();
		_targetedHandlers.clear();
	}
	
	public void removeAllDelegates(){
		if(!_locked)
			forceRemoveAllDelegates();
		else
			_toQuit = true;
	}
	
//	public void setPriority(int priority, CCTouchDelegate delegate){
//		
//	}
	
	private void updateHandlers(){
		if(_toRemove){
			_toRemove = false;
			for(CCTouchDelegate delegate : _handlersToRemove){
				forceRemoveDelegate(delegate);
			}
			_handlersToRemove.clear();
		}
		if(_toAdd){
			_toAdd = false;
			for(CCTouchHandler handler : _handlersToAdd){
				if(handler instanceof CCTargetedTouchHandler){
					forceAddHandler(handler, _targetedHandlers);
				}else if(handler instanceof CCStandardTouchHandler){
					forceAddHandler(handler, _standardHandlers);
				}
			}
			_handlersToAdd.clear();
		}
		if(_toQuit){
			_toQuit = false;
			forceRemoveAllDelegates();
		}	
	}
	
	public void touchesBegan(MotionEvent event) {
		//Log.v(LOG_TAG, "touchesBegan b");
		_locked = true;
		if(_dispatchEvents){
            for( CCTouchHandler handler : _targetedHandlers ) {
                if( handler.delegate().touchesBegan(event))
                    break;
            }
            
            for( CCTouchHandler handler : _standardHandlers){
                if( handler.delegate().touchesBegan(event))
                    break;
            }
		}
		updateHandlers();
		_locked = false;
		//Log.v(LOG_TAG, "touchesBegan e");
	}
	
	public void touchesMoved(MotionEvent event) {
		//Log.v(LOG_TAG, "touchesMoved b");
		_locked = true;
		if(_dispatchEvents){
            for( CCTouchHandler handler : _targetedHandlers ) {
                if( handler.delegate().touchesMoved(event))
                    break;
            }
            
            for( CCTouchHandler handler : _standardHandlers){
                if( handler.delegate().touchesMoved(event))
                    break;
            }
		}	
		updateHandlers();
		_locked = false;
		//Log.v(LOG_TAG, "touchesMoved e");
	}

	public void touchesEnded(MotionEvent event) {
		//Log.v(LOG_TAG, "touchesEnded b");
		_locked = true;
		if(_dispatchEvents){
            for( CCTouchHandler handler : _targetedHandlers ) {
                if( handler.delegate().touchesEnded(event))
                    break;
            }
            
            for( CCTouchHandler handler : _standardHandlers){
                if( handler.delegate().touchesEnded(event))
                    break;
            }
		}	
		updateHandlers();
		_locked = false;
		//Log.v(LOG_TAG, "touchesEnded e");
	}
	
	public void touchesCancelled(MotionEvent event) {
		//Log.v(LOG_TAG, "touchesCancelled b");
		_locked = true;
		if(_dispatchEvents){
            for( CCTouchHandler handler : _targetedHandlers ) {
                if( handler.delegate().touchesCancelled(event))
                    break;
            }
            
            for( CCTouchHandler handler : _standardHandlers){
                if( handler.delegate().touchesCancelled(event))
                    break;
            }
		}	
		updateHandlers();
		_locked = false;
		//Log.v(LOG_TAG, "touchesCancelled e");
	}
}
