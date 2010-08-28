package com.moandroid.cocos2d.renderers;

import javax.microedition.khronos.opengles.GL10;

public class CCDirector3D extends CCDirector {

	@Override
	protected void initGLDefaultValues(GL10 gl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void  beginRender(GL10 gl){
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void  endRender(GL10 gl){
		
	}

}
