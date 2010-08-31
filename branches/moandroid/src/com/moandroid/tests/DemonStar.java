package com.moandroid.tests;

import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.opengles.GL10;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactFilter;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import com.moandroid.cocos2d.actions.CCFiniteTimeAction;
import com.moandroid.cocos2d.actions.CCRepeatForever;
import com.moandroid.cocos2d.actions.CCScheduler;
import com.moandroid.cocos2d.actions.CCTimer;
import com.moandroid.cocos2d.actions.instant.CCCallFunc;
import com.moandroid.cocos2d.actions.interval.CCAnimate;
import com.moandroid.cocos2d.actions.interval.CCDelayTime;
import com.moandroid.cocos2d.actions.interval.CCSequence;
import com.moandroid.cocos2d.animation.CCAnimation;
import com.moandroid.cocos2d.nodes.CCLayer;
import com.moandroid.cocos2d.nodes.CCNode;
import com.moandroid.cocos2d.nodes.scenes.CCScene;
import com.moandroid.cocos2d.nodes.sprite.CCSprite;
import com.moandroid.cocos2d.opengles.CCGLSurfaceView;
import com.moandroid.cocos2d.opengles.CCPrimitives;
import com.moandroid.cocos2d.renderers.CCDirector;
import com.moandroid.cocos2d.renderers.CCDirector2D;
import com.moandroid.cocos2d.types.CCPoint;
import com.moandroid.cocos2d.types.CCSize;
import com.moandroid.cocos2d.util.CCUtils;
import com.moandroid.sound.SoundManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class DemonStar extends Activity {
	public static final String LOG_TAG = DemonStar.class.getSimpleName();
	
	private CCGLSurfaceView mGLSurfaceView;
	private GameLayer _gameLayer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(	WindowManager.LayoutParams.FLAG_FULLSCREEN, 
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mGLSurfaceView = new CCGLSurfaceView(this);
		mGLSurfaceView.setDirector(CCDirector2D.sharedDirector(this));
		CCDirector2D.sharedDirector().setDeviceOrientation(CCDirector.kCCDeviceOrientationPortrait);
		setContentView(mGLSurfaceView);
		CCScene scene = CCScene.node();
		_gameLayer = new GameLayer();
		scene.addChild(_gameLayer);
		CCDirector.sharedDirector().runWithScene(scene);
		SoundManager.sharedSoundManager().loadSound(this, "demonstar/sound/lv8bg.mp3", true, false);
		SoundManager.sharedSoundManager().loadSound(this, "demonstar/sound/bullet1.mp3", true, false);
		SoundManager.sharedSoundManager().loadSound(this, "demonstar/sound/crash.mp3", false, false);
		SoundManager.sharedSoundManager().loadSound(this, "demonstar/sound/bomb1.mp3", false, false);
		SoundManager.sharedSoundManager().loadSound(this, "demonstar/sound/bomb2.mp3", false, false);
	}

	@Override
	protected void onDestroy() {
        CCDirector2D.sharedDirector().end();
        SoundManager.purgeSharedManager();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		CCDirector.sharedDirector().pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		CCDirector.sharedDirector().resume();
		super.onResume();
	}
	
	private class GameLayer extends CCLayer{
		protected static final float PTM_RATIO = 24.0f;
		protected static final float BUFFER = 1.0f;
		protected static final int BOX_GROUP_INDEX = -1;
		protected static final int SELF_GROUP_INDEX = -2;
		protected static final int ENEMY_GROUP_INDEX = -3;
		protected final World bxWorld;
		private CCSprite mFighterA;
		Body bxSpriteBody;
		CCAnimation _animA;
		CCAnimation _animB;
		public GameLayer(){
        	setTouchEnabled(true);
        	setAccelerometerEnabled(true);
            CCSize s = CCDirector.sharedDirector().winSize();
            float scaledWidth = s.width/PTM_RATIO;
            float scaledHeight = s.height/PTM_RATIO;
        	Vec2 lower = new Vec2(-BUFFER, -BUFFER);
        	Vec2 upper = new Vec2(	scaledWidth + BUFFER, 
        							scaledHeight + BUFFER);
        	Vec2 gravity = new Vec2(0f, 0f);         
        	bxWorld = new World(new AABB(lower, upper), gravity, false);
        	
//        	BodyDef bxGroundTopDef = new BodyDef();
//        	bxGroundTopDef.position.set(	s.width/(2*PTM_RATIO),
//        									10/(2*PTM_RATIO));
//        	Body bxGroundTop = bxWorld.createBody(bxGroundTopDef);  
//            PolygonDef bxGroundTopEdgeDef = new PolygonDef();
//            bxGroundTopEdgeDef.setAsBox((s.width-10)/(2*PTM_RATIO), 
//            								10/(2*PTM_RATIO));
//            bxGroundTopEdgeDef.filter.groupIndex = GameLayer.BOX_GROUP_INDEX;
//            bxGroundTop.createShape(bxGroundTopEdgeDef);
//            
//         	BodyDef bxGroundLeftDef = new BodyDef();
//         	bxGroundLeftDef.position.set(	10/(2*PTM_RATIO),
//         									s.height/(2*PTM_RATIO));
//        	Body bxGroundLeft = bxWorld.createBody(bxGroundLeftDef);
//        	PolygonDef bxGroundLeftEdgeDef = new PolygonDef();
//        	bxGroundLeftEdgeDef.setAsBox(	10/(2*PTM_RATIO), 
//											(s.height-10)/(2*PTM_RATIO));
//        	bxGroundLeftEdgeDef.filter.groupIndex = GameLayer.BOX_GROUP_INDEX;
//        	bxGroundLeft.createShape(bxGroundLeftEdgeDef);
//
//        	BodyDef bxGroundBottomDef = new BodyDef();
//        	bxGroundBottomDef.position.set(	s.width/(2*PTM_RATIO),
//											(s.height-5)/PTM_RATIO);
//        	PolygonDef bxGroundBottomEdgeDef = new PolygonDef();
//        	bxGroundBottomEdgeDef.setAsBox((s.width-10)/(2*PTM_RATIO), 
//        									10/(2*PTM_RATIO));
//        	bxGroundBottomEdgeDef.filter.groupIndex = GameLayer.BOX_GROUP_INDEX;
//        	Body bxGroundBottom = bxWorld.createBody(bxGroundBottomDef);
//        	bxGroundBottom.createShape(bxGroundBottomEdgeDef);
//        	
//        	BodyDef bxGroundRightDef = new BodyDef();
//        	bxGroundRightDef.position.set(	(s.width-5)/PTM_RATIO,
//											s.height/(2*PTM_RATIO));
//        	Body bxGroundRight = bxWorld.createBody(bxGroundRightDef);
//        	PolygonDef bxGroundRightEdgeDef = new PolygonDef();
//        	bxGroundRightEdgeDef.setAsBox(	10/(2*PTM_RATIO), 
//        									(s.height-10)/(2*PTM_RATIO));
//        	bxGroundRightEdgeDef.filter.groupIndex = GameLayer.BOX_GROUP_INDEX;
//        	bxGroundRight.createShape(bxGroundRightEdgeDef);

            
            mFighterA = CCSprite.sprite("demonstar/image/fighterA_0.png");
            _animA = CCAnimation.animation("fighterA",0.1f,"demonstar/image/fighterA_0.png","demonstar/image/fighterA_1.png");
            _animB = CCAnimation.animation("fighterB",0.1f,"demonstar/image/fighterB_0.png","demonstar/image/fighterB_1.png");
            mFighterA.setPosition(s.width/2,s.height/2);
            mFighterA.runAction(CCRepeatForever.action(CCAnimate.action(_animA)));
            addChild(mFighterA, -1);
            
        	BodyDef bxSpriteBodyDef = new BodyDef();
        	bxSpriteBodyDef.userData = mFighterA;
        	bxSpriteBodyDef.position.set(s.width/(2*PTM_RATIO), s.height/(2*PTM_RATIO));
        	bxSpriteBodyDef.angularDamping = 100f;

        	PolygonDef bxSpritePolygonDef = new PolygonDef();
        	bxSpritePolygonDef.setAsBox(mFighterA.width()/(2*PTM_RATIO), mFighterA.height()/(2*PTM_RATIO));
        	bxSpritePolygonDef.density = 2.0f;
        	bxSpritePolygonDef.restitution = 0;
        	bxSpritePolygonDef.filter.groupIndex = SELF_GROUP_INDEX;
        	bxSpriteBody = bxWorld.createBody(bxSpriteBodyDef);
	    	bxSpriteBody.createShape(bxSpritePolygonDef);
	    	bxSpriteBody.setMassFromShapes();
	    	
	    	bxWorld.setContactListener(new GameContactListener());
	    	bxWorld.setContactFilter(new GameContactFilter());
	    	schedule("createEnemy",10);
 		}

		private ArrayBlockingQueue<Body> _discardeds = new ArrayBlockingQueue<Body>(10,false);
        public void tick(float delta) {
        	Body db;
        	while((db =_discardeds.poll())!=null){
        		bxWorld.destroyBody(db);
        		removeChild((CCNode)db.getUserData(),true);
        	}
        	synchronized (bxWorld) {
        		bxWorld.step(delta, 8);
        	}
        	CCSize s = CCDirector.sharedDirector().winSize();
        	float x = bxSpriteBody.getPosition().x * PTM_RATIO;
        	float y = bxSpriteBody.getPosition().y * PTM_RATIO;
        	boolean change = false;
        	if(x > s.width - mFighterA.width()/2 && bxSpriteBody.m_linearVelocity.x>0){
        		x = s.width - mFighterA.width()/2;
        		change = true;
        	}
        	if(x < mFighterA.width()/2 && bxSpriteBody.m_linearVelocity.x<0){
        		x = mFighterA.width()/2;
        		change = true;
        	}
        	if(y < mFighterA.height()/2 && bxSpriteBody.m_linearVelocity.y<0){
        		y = mFighterA.height()/2;
        		change = true;
        	}
        	if(y>s.height - mFighterA.height()/2 && bxSpriteBody.m_linearVelocity.y>0){
        		y = s.height - mFighterA.height()/2;
        		change = true;
        	}
        	if(change){
        		bxSpriteBody.setXForm(new Vec2(x/PTM_RATIO, y/PTM_RATIO), 0);
        		bxSpriteBody.setLinearVelocity(new Vec2(0,0));
        	}
        	// Iterate over the bodies in the physics world
        	for (Body b = bxWorld.getBodyList(); b != null; b = b.getNext()) {
        		Object userData = b.getUserData();
        		if (userData != null) {
        			if(userData instanceof CCSprite){
        				if(b.isFrozen()){
        					_discardeds.add(b);
        				}else{
        					CCSprite sprite = (CCSprite)userData;
        					sprite.setPosition(b.getPosition().x * PTM_RATIO, b.getPosition().y * PTM_RATIO);
        					sprite.setRotation(-1.0f * CCUtils.CC_RADIANS_TO_DEGREES(b.getAngle()));
        				}
        			}else if(userData instanceof Bullet){
        				if(b.isFrozen()){
        					_discardeds.add(b);
        				}else{
	        				Bullet bullet = (Bullet)userData;
	        				bullet.setPosition(b.getPosition().x * PTM_RATIO, b.getPosition().y * PTM_RATIO);
	        				bullet.setRotation(-1.0f * CCUtils.CC_RADIANS_TO_DEGREES(b.getAngle()));
        				}
        			}
        		}
        	}
        }
        
        public void onEnter(){
        	SoundManager.sharedSoundManager().playSound("demonstar/sound/lv8bg.mp3");
        	schedule("tick");
        	super.onEnter();	
        }
        
		@Override       
		public void onExit() {
			endGame();
			super.onExit();	
		}
		
		@Override
		public void draw(GL10 gl) {
			super.draw(gl);
//			gl.glDisable(GL10.GL_TEXTURE_2D);
//			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
//			gl.glLineWidth(2);
//			gl.glColor4f(1f, 1f, 1f, 1f);
//            CCSize s = CCDirector.sharedDirector().winSize();
//   	     
//            CCRect rc = CCRect.make(5, 0, s.width-10, 10);
//            CCPrimitives.drawRect(gl, rc);
//            
//            rc.setAll(0, 5, 10, s.height-10);
//            CCPrimitives.drawRect(gl, rc);
//            
//            rc.setAll(5, s.height-10, s.width-10, 10);
//            CCPrimitives.drawRect(gl, rc);
//            
//            rc.setAll(s.width-10, 5, 10, s.height-10);
//            CCPrimitives.drawRect(gl, rc);
// 
//			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//            gl.glEnable(GL10.GL_TEXTURE_2D);
		}

		@Override
		public boolean accelerometerChanged(float accelX, float accelY,
				float accelZ) {
			synchronized (bxWorld) {
				//if (bxSpriteBody.isSleeping() == false){
					CCPoint vec = CCDirector.sharedDirector().convertAccelerometer(accelX, accelY);
//					Vec2 impulse = new Vec2(-vec.x, -vec.y);
//					Vec2 point = new Vec2( mFighterA.width()/2, mFighterA.height()/2);
//					bxSpriteBody.applyForce(impulse, point);
					float x,y;
					if(vec.x<-1)
						x = 5;
					else if(vec.x>1)
						x = -5;
					else
						x = 0;
					if(vec.y < -1.5)
						y = 5;
					else if(vec.y > 1.5)
						y = -5;
					else
						y = 0;
					Vec2 velocity = new Vec2(x,y);
					bxSpriteBody.setLinearVelocity(velocity);
				//}
				
			}
			return true;
		}

		private boolean _isShooting = false;
		@Override
		public boolean touchesBegan(MotionEvent event) {
			if(!_isShooting){
				_isShooting = true;
				SoundManager.sharedSoundManager().playSound("demonstar/sound/bullet1.mp3");
				CCFiniteTimeAction ac = CCSequence.actions(CCCallFunc.action(this,"shotBullet"), CCDelayTime.action(0.5f));
				_gameLayer.runAction(CCRepeatForever.action(ac));
			}else{
				_isShooting = false;
				SoundManager.sharedSoundManager().stopSound("demonstar/sound/bullet1.mp3");	
				_gameLayer.stopAllActions();
			}
			return true;
		}

		public void shotBullet(){
			for(int i=0; i<3; ++i){
				Bullet bullet = new Bullet();
				addChild(bullet,3);
				bullet.setPosition(mFighterA.position());
				
	        	BodyDef bxBulletBodyDef = new BodyDef();
	        	bxBulletBodyDef.userData = bullet;
	        	bxBulletBodyDef.position.set(mFighterA.position().x/PTM_RATIO, mFighterA.position().y/PTM_RATIO);
	        	
	        	CircleDef bxBulletCircleDef = new CircleDef();
	        	bxBulletCircleDef.radius = 2/PTM_RATIO;        	
	        	bxBulletCircleDef.density = 1f;
	        	bxBulletCircleDef.restitution = 0;
	        	bxBulletCircleDef.filter.groupIndex = SELF_GROUP_INDEX;
	        	
	        	Body bxBulletBody = bxWorld.createBody(bxBulletBodyDef);
	        	bxBulletBody.createShape(bxBulletCircleDef);
	        	bxBulletBody.setMassFromShapes();
	        	if(i == 0){
	        		bxBulletBody.setLinearVelocity(new Vec2(-10f,10f));
	        	}else if(i == 1){
	        		bxBulletBody.setLinearVelocity(new Vec2(0f,1.414f*10));
	        	}else if(i == 2){
	        		bxBulletBody.setLinearVelocity(new Vec2(10f,10f));
	        	}
			}
		}
		
		private void endGame(){
			unschedule("tick");
			SoundManager.sharedSoundManager().stopSound("demonstar/sound/lv8bg.mp3");
			if(_isShooting)
				SoundManager.sharedSoundManager().playSound("demonstar/sound/bullet1.mp3");	
		}
		
		public void createEnemy(float dt){
			CCSize s = CCDirector.sharedDirector().winSize();
			for(int i=0; i<3; ++i){
				CCSprite enemy = CCSprite.sprite("demonstar/image/fighterB_0.png");        
	            enemy.setPosition(i*106+36, s.height);
	            enemy.setRotation(180);
	            enemy.runAction(CCRepeatForever.action(CCAnimate.action(_animB)));
				addChild(enemy,-1);
				
	        	BodyDef bxEnemyBodyDef = new BodyDef();
	        	bxEnemyBodyDef.userData = enemy;
	        	bxEnemyBodyDef.position.set((i*106+36)/PTM_RATIO, s.height/PTM_RATIO);
	        	bxEnemyBodyDef.angle = CCUtils.CC_DEGREES_TO_RADIANS(180);
	        	PolygonDef bxEnemyPolygonDef = new PolygonDef();
	        	bxEnemyPolygonDef.setAsBox(enemy.width()/(2*PTM_RATIO), enemy.height()/(2*PTM_RATIO));
	        	bxEnemyPolygonDef.density = 2.0f;
	        	bxEnemyPolygonDef.restitution = 0;
	        	bxEnemyPolygonDef.filter.groupIndex = ENEMY_GROUP_INDEX;
	        	
	        	Body bxEnemyBody = bxWorld.createBody(bxEnemyBodyDef);
	        	bxEnemyBody.createShape(bxEnemyPolygonDef);
	        	bxEnemyBody.setMassFromShapes();
	        	
	        	bxEnemyBody.setLinearVelocity(new Vec2(0f,-3f));
		}
	}
	
	
		
	public class Bullet extends CCNode{

		@Override
		public void draw(GL10 gl) {
			super.draw(gl);
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColor4f(1f, 0f, 0f, 1f);
			gl.glPointSize(3f);
            CCPrimitives.drawPoint(gl, 0, 0);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glEnable(GL10.GL_TEXTURE_2D);			
		}	
	}


	
	class GameContactListener implements ContactListener{

		@Override
		public void add(ContactPoint point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void persist(ContactPoint point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void remove(ContactPoint point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void result(ContactResult point) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class GameContactFilter implements ContactFilter{

		@Override
		public boolean shouldCollide(Shape shape1, Shape shape2) {
			if(shape1.m_filter.groupIndex == shape2.m_filter.groupIndex)
				return false;
			if(		shape1.m_body.getUserData() instanceof Bullet && shape2.m_filter.groupIndex == GameLayer.BOX_GROUP_INDEX||
					shape2.m_body.getUserData() instanceof Bullet && shape1.m_filter.groupIndex == GameLayer.BOX_GROUP_INDEX){
				return false;
			}
			return true;
		}
		
	}
	}
}
