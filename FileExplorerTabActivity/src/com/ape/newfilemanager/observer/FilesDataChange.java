package com.ape.newfilemanager.observer;

import java.util.Observable;

public class FilesDataChange extends Observable {

	private static FilesDataChange instance = null;

	public static FilesDataChange getInstance() {
		if (null == instance) {

			synchronized (FilesDataChange.class) {

				if (null == instance) {
					instance = new FilesDataChange();
				}
			}

		}
		return instance;
	}

	public void notifydatachange() {

	    setChanged();
		notifyObservers();
	}

}
