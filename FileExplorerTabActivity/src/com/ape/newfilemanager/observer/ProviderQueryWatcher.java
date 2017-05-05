package com.ape.newfilemanager.observer;

import java.util.Observable;
import java.util.Observer;

import com.ape.utils.MyLog;

public class ProviderQueryWatcher implements Observer {

	
	@Override
	public void update(Observable observable, Object data) {
		
		MyLog.i("ProviderQueryWatcher", " mCategoryAdapter.reflesh() ");
	}

}
