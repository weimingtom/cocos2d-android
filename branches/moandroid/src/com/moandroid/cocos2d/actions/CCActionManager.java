package com.moandroid.cocos2d.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import com.moandroid.cocos2d.actions.interval.CCTimer;
import com.moandroid.cocos2d.nodes.CCNode;

public class CCActionManager {
	public static final String LOG_TAG = CCActionManager.class.getSimpleName();

	private static CCActionManager _shareManager;
	public static CCActionManager sharedManager() {
		if(_shareManager == null)
			_shareManager = new CCActionManager();
		return _shareManager;
	}

	public CCActionManager(){
		CCScheduler.sharedScheduler().schedule(CCTimer.timer(this, "tick"));
	}
	public static void purgeSharedManager() {
		if(_shareManager != null){
			_shareManager.removeAllActions();
			_shareManager = null;
		}
	}
	
	private HashMap<CCNode, ArrayList<CCAction>> actionsMap = new HashMap<CCNode, ArrayList<CCAction>>(131);

	public void addAction(CCAction action, CCNode target) {
		Assert.assertTrue("Argument action must be non-null", action != null);
		Assert.assertTrue("Argument target must be non-null", target != null);
		ArrayList<CCAction> actionList;
		actionList = actionsMap.get(target);
		if(actionList == null){
			actionList = new ArrayList<CCAction>(4);
			actionsMap.put(target, actionList);
		}
		Assert.assertTrue("runAction: Action already running",!actionList.contains(action));
		actionList.add(action);
		action.start(target);
	}

	public void resumeAllActions(CCNode node) {
		node.setIsRunning(true);
	}

	public void pauseAllActions(CCNode node) {
		node.setIsRunning(false);
	}

	public void removeAllActions(){
		Collection<ArrayList<CCAction>> values = actionsMap.values();
		for(ArrayList<CCAction> list : values){
			list.clear();
		}
		actionsMap.clear();
	}
	
	public void removeAllActions(CCNode target) {
		if(actionsMap.containsKey(target)){
			actionsMap.remove(target);
		}
	}
	
	public void removeAction(CCAction action) {
		CCNode target = action.originalTarget();
		if(actionsMap.containsKey(target)){
			ArrayList<CCAction> actionList = actionsMap.get(target);
			actionList.remove(action);
		}
	}
	
	public void removeAction(int tag, CCNode target) {
		Assert.assertTrue("Invalid tag", tag != CCAction.INVALID_TAG);
		if(actionsMap.containsKey(target)){
			ArrayList<CCAction> actionList = actionsMap.get(target);
			for(CCAction action : actionList){
				if(action.tag() == tag){
					actionList.remove(action);
				}
			}
		}	
	}	
	
	public CCAction getAction(int tag, CCNode target) {
		Assert.assertTrue("Invalid tag", tag != CCAction.INVALID_TAG);
		if(actionsMap.containsKey(target)){
			ArrayList<CCAction> actionList = actionsMap.get(target);
			for(CCAction action : actionList){
				if(action.tag() == tag){
					return action;
				}
			}
		}
		return null;
	}

	public int numberOfRunningActions(CCNode target) {
		if(actionsMap.containsKey(target)){
			ArrayList<CCAction> actionList = actionsMap.get(target);
			return actionList.size();
		}
		return 0;
	}
	
    public void tick(float dt) {
    	ArrayList<CCNode> dropList = new ArrayList<CCNode>(5);
    	Iterator<Entry<CCNode,ArrayList<CCAction>>> it = actionsMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<CCNode,ArrayList<CCAction>> entry = it.next();
			CCNode key = entry.getKey();
			if(key.isRunning()){
				ArrayList<CCAction> list = entry.getValue();
				for(int i = 0; i<list.size(); ++i){
					CCAction action = list.get(i);
					action.step(dt);
					if(action.isDone()){
						action.stop();
						list.remove(i);
						--i;
					}
				}
				if(list.isEmpty()){
					dropList.add(key);
					//actionsMap.remove(key);
				}			
			}
		}
		for(CCNode node : dropList){
			actionsMap.remove(node);
		}
    }
}
