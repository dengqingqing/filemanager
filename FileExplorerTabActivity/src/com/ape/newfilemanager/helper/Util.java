/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.newfilemanager.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.TextView;

import com.ape.filemanager.MountPointManager;
import com.ape.filemanager.R;
import com.ape.newfilemanager.model.FavoriteItem;
import com.ape.newfilemanager.model.FileInfo;
import com.ape.newfilemanager.model.Settings;

public class Util {
	private static String ANDROID_SECURE = getTinnoRootPath()
			+ "/.android_secure";

	private static final String LOG_TAG = "Util";

	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	// if path1 contains path2
	public static boolean containsPath(String path1, String path2) {
		String path = path2;
		while (path != null) {
			if (path.equalsIgnoreCase(path1))
				return true;

			if (path.equals(GlobalConsts.ROOT_PATH))
				break;
			path = new File(path).getParent();
		}

		return false;
	}

	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	private static String mInternalStoragePath = null;
	private static final String INTERNAL_PATH_TAG = "[internal_sd_path]";

	public static String getInternalStorageDirectory() {
		if (mInternalStoragePath == null) {
			try {
				Runtime runtime = Runtime.getRuntime();
				Process proc = runtime.exec("getprop");
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				String line;
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					if (line.startsWith(INTERNAL_PATH_TAG)) {
						int start = line.lastIndexOf("[");
						int end = line.lastIndexOf("]");
						if (start != -1 && end != -1 && start < end) {
							mInternalStoragePath = line.substring(start + 1,
									end);
							break;
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mInternalStoragePath;
	}

	private static String mTinnoRootPathStr = null;

	public static String getTinnoRootPath() {
		if (mTinnoRootPathStr == null) {
			String sdPath = Environment.getExternalStorageDirectory().getPath();
			int offset = sdPath.lastIndexOf(File.separator);
			if (offset > 1) {
				mTinnoRootPathStr = sdPath.substring(0, offset);
			}
		}
		return mTinnoRootPathStr;
		// return sdPath;
	}

	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(ANDROID_SECURE);
	}

	public static FileInfo GetFileInfo(String filePath) {
		File lFile = new File(filePath);
		if (!lFile.exists())
			return null;

		FileInfo lFileInfo = new FileInfo();
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = Util.getNameFromFilepath(filePath);
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		return lFileInfo;
	}

	public static FileInfo GetFileInfo(File f, FilenameFilter filter,
			boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		if (lFileInfo.IsDir) {
			int lCount = 0;
			File[] files = lFile.listFiles(filter);

			// null means we cannot access this dir
			if (files == null) {
				return null;
			}

			for (File child : files) {
				if ((!child.isHidden() || showHidden)
						&& Util.isNormalFile(child.getAbsolutePath())) {
					lCount++;
				}
			}
			lFileInfo.Count = lCount;

		}

		lFileInfo.fileSize = lFile.length();

		return lFileInfo;
	}

	/*
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
	 * apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Drawable getApkIcon(Context context, String apkPath) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (info != null) {
				ApplicationInfo appInfo = info.applicationInfo;
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;
				try {
					return appInfo.loadIcon(pm);
				} catch (OutOfMemoryError e) {
					Log.e(LOG_TAG, e.toString());
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "getApkIcon error, e: " + e);
		}
		return null;
	}

	public static String getExtFromFilename(String filename) {
		if (filename != null) {
			int dotPosition = filename.lastIndexOf('.');
			if (dotPosition != -1) {
				return filename.substring(dotPosition + 1, filename.length());
			}
		}
		return "";
	}

	public static String getNameFromFilename(String filename) {
		if (filename != null) {
			int dotPosition = filename.lastIndexOf('.');
			if (dotPosition != -1) {
				return filename.substring(0, dotPosition);
			}
		}
		return "";
	}

	public static String getPathFromFilepath(String filepath) {
		if (filepath != null) {
			int pos = filepath.lastIndexOf('/');
			if (pos != -1) {
				return filepath.substring(0, pos);
			}
		}
		return "";
	}

	public static String getNameFromFilepath(String filepath) {
		if (filepath != null) {
			int pos = filepath.lastIndexOf('/');
			if (pos != -1) {
				return filepath.substring(pos + 1);
			}
		}
		return "";
	}

	// return new file path if successful, or return null
	public static String copyFile(String src, String dest) {
		File file = new File(src);
		if (!file.exists() || file.isDirectory()) {
			Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
			return null;
		}
		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			fi = new FileInputStream(file);
			File destPlace = new File(dest);
			if (!destPlace.exists()) {
				if (!destPlace.mkdirs())
					return null;
			}

			String destPath = Util.makePath(dest, file.getName());
			File destFile = new File(destPath);
			int i = 1;
			while (destFile.exists()) {
				String destName = Util.getNameFromFilename(file.getName())
						+ " " + i++ + "."
						+ Util.getExtFromFilename(file.getName());
				destPath = Util.makePath(dest, destName);
				destFile = new File(destPath);
			}

			if (!destFile.createNewFile())
				return null;

			fo = new FileOutputStream(destFile);
			int count = 2048 * 1024;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}

			// TODO: set access privilege

			return destPath;
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "copyFile: file not found, " + src);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "copyFile: " + e.toString());
		} finally {
			try {
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	// does not include sd card folder
	private static String[] SysFileDirs = new String[] { "miren_browser/imagecaches" };

	public static boolean shouldShowFile(String path) {
		return shouldShowFile(new File(path));
	}

	public static boolean shouldShowFile(File file) {
		boolean show = Settings.instance().getShowDotAndHiddenFiles();
		if (show)
			return true;

		if (file.isHidden())
			return false;

		if (file.getName().startsWith("."))
			return false;

		String sdFolder = getSdDirectory();
		for (String s : SysFileDirs) {
			if (file.getPath().startsWith(makePath(sdFolder, s)))
				return false;
		}

		return true;
	}

	public static ArrayList<FavoriteItem> getDefaultFavorites(Context context) {
		ArrayList<FavoriteItem> list = new ArrayList<FavoriteItem>();
		list.add(new FavoriteItem(context.getString(R.string.favorite_photo),
				makePath(getSdDirectory(), "DCIM/Camera")));
		list.add(new FavoriteItem(context.getString(R.string.favorite_sdcard),
				getSdDirectory()));
		// list.add(new FavoriteItem(context.getString(R.string.favorite_root),
		// getSdDirectory()));
		// list.add(new
		// FavoriteItem(context.getString(R.string.favorite_screen_cap),
		// makePath(getSdDirectory(), "MIUI/screen_cap")));
		list.add(new FavoriteItem(
				context.getString(R.string.favorite_ringtone), makePath(
						getSdDirectory(), "MIUI/ringtone")));
		return list;
	}

	public static boolean setText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	public static boolean setText(View view, int id, int text) {
		TextView textView = (TextView) view.findViewById(id);
		if (textView == null)
			return false;

		textView.setText(text);
		return true;
	}

	// comma separated number
	public static String convertNumber(long number) {
		return String.format("%,d", number);
	}

	// storage, G M K B
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	public static class SDCardInfo {
		public long total;

		public long free;
	}

	public static SDCardInfo getSDCardInfo() {
		String sDcString = android.os.Environment.getExternalStorageState();

		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File pathFile = android.os.Environment
					.getExternalStorageDirectory();

			return getSDCardInfoByPath(pathFile.getPath());
		}

		return null;
	}

	public static SDCardInfo getSDCardInfoByPath(String path) {

		if (path != null) {
			try {
				android.os.StatFs statfs = new android.os.StatFs(path);

				// 获取SDCard上BLOCK总数
				long nTotalBlocks = statfs.getBlockCount();

				// 获取SDCard上每个block的SIZE
				long nBlocSize = statfs.getBlockSize();

				// 获取可供程序使用的Block的数量
				long nAvailaBlock = statfs.getAvailableBlocks();

				// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
				long nFreeBlock = statfs.getFreeBlocks();

				SDCardInfo info = new SDCardInfo();
				// 计算SDCard 总容量大小MB
				info.total = nTotalBlocks * nBlocSize;

				// 计算 SDCard 剩余大小MB
				info.free = nAvailaBlock * nBlocSize;

				return info;
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, e.toString());
			}
		}
		return null;
	}

	public static SDCardInfo getSDCardInfoFromPaths(Context context,
			List<String> paths) {
		SDCardInfo totalInfo = new SDCardInfo();
		SDCardInfo info;

		// todo
		/*
		 * StorageManager mStorageManager =
		 * context.getSystemService(StorageManager.class); for
		 * (android.os.storage.VolumeInfo vol : mStorageManager.getVolumes()) {
		 * if(vol.path != null && vol.path.contains("/mnt/expand/")){
		 * paths.add(vol.path); } }
		 */

		for (String path : paths) {
			info = getSDCardInfoByPath(path);
			if (info != null) {
				totalInfo.free += info.free;
				totalInfo.total += info.total;
			}
		}

		return totalInfo;
	}

	public static void showNotification(Context context, Intent intent,
			String title, String body, int drawableId) {
		/*
		 * NotificationManager manager = (NotificationManager)
		 * context.getSystemService(Context.NOTIFICATION_SERVICE); Notification
		 * notification = new Notification(drawableId, body,
		 * System.currentTimeMillis()); notification.flags =
		 * Notification.FLAG_AUTO_CANCEL; notification.defaults =
		 * Notification.DEFAULT_SOUND; if (intent == null) { // FIXEME: category
		 * tab is disabled intent = new Intent(context, FileViewActivity.class);
		 * } PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		 * intent, PendingIntent.FLAG_ONE_SHOT);
		 * notification.setLatestEventInfo(context, title, body, contentIntent);
		 * manager.notify(drawableId, notification);
		 */

		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(context);
		if (intent == null) {
			// FIXEME: category tab is disabled

			// todo, need modify
			// intent = new Intent(context, FileViewActivity.class);
			return;
		}
		PendingIntent pintent = PendingIntent
				.getActivity(context, 0, intent, 0);
		// PendingIntent click to notification page.
		builder.setContentTitle("Bmob Test");
		builder.setContentText("message");
		builder.setSmallIcon(drawableId);
		builder.setContentIntent(pintent);
		Notification notification = builder.getNotification();// builder ->
																// notification
		notification.flags |= Notification.FLAG_AUTO_CANCEL;// 点击通知后通知消失
		notification.defaults = Notification.DEFAULT_SOUND;
		manager.notify(1, notification);// run notification

	}

	public static String formatDateString(Context context, long time) {
		DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat
				.getTimeFormat(context);
		Date date = new Date(time);
		return dateFormat.format(date) + " " + timeFormat.format(date);
	}

	public static void updateActionModeTitle(ActionMode mode, Context context,
			int selectedNum) {
		if (mode != null) {
			// mode.setTitle(context.getString(R.string.multi_select_title,selectedNum));
			mode.setTitle(context.getResources().getQuantityString(
					R.plurals.multi_select_title, selectedNum, selectedNum));
			if (selectedNum == 0) {
				mode.finish();
			}
		}
	}

	public static String getRootPathFromFilePath(String filePath) {
		List<String> mountPaths = MountPointManager.getInstance()
				.getMountPointPaths();

		for (String mount : mountPaths) {
			if (filePath.startsWith(mount)) {
				return mount;
			}
		}

		return "";
	}

	public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
		{
			add("text/plain");
			add("text/plain");
			add("application/pdf");
			add("application/msword");
			add("application/vnd.ms-excel");
			add("application/vnd.ms-excel");
		}
	};

	public static final String[] DOC_SUPPORTED_SUFFIX = { ".txt", ".log", /*
																		 * ".xml",
																		 */
			".ini", ".lrc" };

	public static final String[] SUPPORTED_ARCHIVES = { ".zip", ".rar" };

	public static int CATEGORY_TAB_INDEX = 0;
	public static int SDCARD_TAB_INDEX = 1;

	public static int SDK_VERSION = android.os.Build.VERSION.SDK_INT;

	// reflect get SystemProperties
	public static String getProperty(String key, String defaultValue) {
		String value = defaultValue;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class, String.class);
			value = (String) (get.invoke(c, key, "unknown"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return value;
		}
	}

	// reflect set SystemProperties
	public static void setProperty(String key, String value) {
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method set = c.getMethod("set", String.class, String.class);
			set.invoke(c, key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
