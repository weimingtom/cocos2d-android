package com.moandroid.sound;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.media.MediaPlayer;

import android.util.Log;

public class SoundManager {
	public class Media implements MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener
	{
		private static final String  TAG = "Media";
		private int nID = -1;
		public Context context = null;
		public  MediaPlayer mp = null;
		public  boolean bLoop = false;;
		public  boolean bMutex = false;
		public boolean isPause = false;
		
		public Media(Context _context,int _nID, boolean _bLoop,boolean _bMutex)
		{
			nID = _nID;
			bLoop = _bLoop;
			bMutex = _bMutex;
			context = _context;
			
			//mp = new MediaPlayer();
			
			mp = MediaPlayer.create(context, nID);
			mp.setLooping(bLoop);
			mp.setOnCompletionListener(this);
	        mp.setOnErrorListener(this);
		}
		
		public boolean Pause()
		{
			try
			{
				if(mp!=null && mp.isPlaying())
				{
					mp.pause();
					isPause = true;
				}
			}
			catch (IllegalStateException e)
			{
				e.printStackTrace();
			}
			return true;			
		}
		
		public boolean Resume()
		{
			try
			{
				if(mp!=null && isPause==true)
				{
					if(!mp.isPlaying())
					{
						mp.start();
					}
					isPause = false;
				}
			}
			catch (IllegalStateException e)
			{
				e.printStackTrace();
			}
			return true;			
		}		
		public boolean Prepare()
		{
			try
			{
				mp.prepare();
				mp.seekTo(0);
			}
			catch (IllegalStateException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return true;
		}
		
		//Called when the end of a media source is reached during playback.
		public void onCompletion(MediaPlayer mp)
		{
			Log.v(TAG, "onCompletion()!");
			if(!bLoop)
			{
				mp.stop();
				Prepare();
				onNotify(nID,bMutex);
			}
		}
	
		//Called to indicate an error.
		//True if the method handled the error, false if it didn't.
		public boolean  onError(MediaPlayer mp, int what, int extra)
		{
			if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN)
			   Log.e(TAG,"Unspecified media player error.");
			else
				Log.e(TAG,"Media server died.");
			mp.reset();
			return false;
		}
	}
	
	private boolean onNotify(int nID,boolean bMutex)
	{
		if(bMutex)
		{
			Iterator<Entry<Integer, Media>> it = sounds.entrySet().iterator();
	    	while (it.hasNext()) {
	    		Entry<Integer, Media> entry = it.next();
	    		if(entry.getKey()==nID)
	    		{
	    			continue;
	    		}
	    		Media _m = entry.getValue();

	    		if(_m.bLoop && !_m.mp.isPlaying())
	    		{
	    			_m.mp.start();
	    		}
	    	}
		}
		return true;
	}

	private static SoundManager _sharedSoundManager;
	
	public static SoundManager sharedSoundManager()
	{
//		synchronized(SoundManager.class)
//		{
			if(_sharedSoundManager == null)
			{
				_sharedSoundManager = new SoundManager();
			}
			return _sharedSoundManager;
//		}
	}
	
	public static void purgeSharedManager(){
		_sharedSoundManager = null;
	}
	
	private final HashMap<Integer,Media> sounds;
	
	protected SoundManager()
	{
		synchronized(SoundManager.class)
		{
			sounds = new HashMap<Integer,Media>();
		}
	}
	
	public boolean loadSound(Context context, int ID, boolean loop, boolean mutex)
	{
		if(!sounds.containsKey(ID))
		{
			Media m;
			m = new Media( context, ID,
					loop,
					mutex);
			sounds.put(ID, m);
			return true;
		}
		return true;		
	}
	
	public boolean playSound(int ID)
	{
		Media m = sounds.get(ID);
		if(m != null)
		{
		  if(m.bMutex)
		  {
			   Collection<Media> values = sounds.values();
			   for(Iterator<Media> it = values.iterator();it.hasNext();)
			   {
				  Media _m  = it.next();
				  if(_m.mp.isPlaying())
					    _m.mp.pause();
			  }
		  }
		  m.mp.start();			
		}
		return m!=null;
	}

	public void stopSound(int ID)
	{
		Media m = sounds.get(ID);
		if(m != null)
		{
			m.mp.stop();
			m.Prepare();
			onNotify(ID, m.bMutex);
		}		
	}
	
	public void unloadSound(int ID)
	{
	}
	
	public void unLoadAll()
	{
	   Collection<Media> values = sounds.values();
	   for(Iterator<Media> it = values.iterator(); it.hasNext();)
	   {
		  Media _m  = it.next();
		  _m.context=null;
		  _m.mp.release();
	   }
	   sounds.clear();
	}
	
	public void pauseAll()
	{
		Collection<Media> values = sounds.values();
	   for(Iterator<Media> it = values.iterator();it.hasNext();)
	   {
		  Media _m  = it.next();
		  _m.Pause();
	  }		
	}
	
	public void resumeAll()
	{
		Collection<Media> values = sounds.values();
	   for(Iterator<Media> it = values.iterator();it.hasNext();)
	   {
		  Media _m  = it.next();
		  _m.Resume();
	  }		
	}
	
	public void end()
	{
		unLoadAll();
		_sharedSoundManager = null;
	}
}
