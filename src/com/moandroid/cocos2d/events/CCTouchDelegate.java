package com.moandroid.cocos2d.events;

import android.view.MotionEvent;

public interface CCTouchDelegate {

	boolean touchesBegan(MotionEvent event);

	boolean touchesMoved(MotionEvent event);

	boolean touchesEnded(MotionEvent event);

	boolean touchesCancelled(MotionEvent event);

}
