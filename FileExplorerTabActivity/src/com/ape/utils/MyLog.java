/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.utils;

import android.util.Log;

public class MyLog {

	public static boolean debug = true;

	private static void l(int level, String tag, String str) {
		synchronized (MyLog.class) {
			str = str.trim();

			// when the apk is ok ,only print Log.ERROR log.
			if (debug == true || level > Log.WARN) {
				Log.println(level, tag, str);
			}

		}
	}

	public static void e(String tag, String s) {
		l(Log.ERROR, tag, s);
	}

	public static void w(String tag, String s) {
		l(Log.WARN, tag, s);
	}

	public static void i(String tag, String s) {
		l(Log.INFO, tag, s);
	}

	public static void d(String tag, String s) {
		l(Log.DEBUG, tag, s);
	}
}
