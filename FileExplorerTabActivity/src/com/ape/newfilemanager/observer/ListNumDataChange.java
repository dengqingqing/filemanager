package com.ape.newfilemanager.observer;

import java.util.Observable;

public class ListNumDataChange extends Observable {

	private static ListNumDataChange instance = null;

	public static ListNumDataChange getInstance() {
		if (null == instance) {

			synchronized (ListNumDataChange.class) {

				if (null == instance) {
					instance = new ListNumDataChange();
				}
			}

		}
		return instance;
	}

	public void notifydatachange() {

	    setChanged();
		notifyObservers();
	}

	public void notifydatachange(int data) {

	    setChanged();
		notifyObservers(data);;
	}
}
