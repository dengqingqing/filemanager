package com.ape.newfilemanager;

import android.app.Application;
import android.content.Context;

public class FileManagerApplication extends Application {

	private Context mAppContext;

	public FileManagerApplication() {

	}

	@Override
	public void onCreate() {
		super.onCreate();

		mAppContext = getApplicationContext();

	}

	@Override
	public void onTerminate() {

		super.onTerminate();
	}

	@Override
	public void onLowMemory() {

		super.onLowMemory();
	}

}
