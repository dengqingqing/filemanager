package com.ape.newfilemanager.observer;

import java.util.Observable;

public class ProviderQueryDataChange extends Observable {

	private static ProviderQueryDataChange instance = null;

	public static ProviderQueryDataChange getInstance() {
		if (null == instance) {

			synchronized (ProviderQueryDataChange.class) {

				if (null == instance) {
					instance = new ProviderQueryDataChange();
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
