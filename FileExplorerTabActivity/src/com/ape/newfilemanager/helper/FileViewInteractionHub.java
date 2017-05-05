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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R.drawable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

/*import com.ape.filemanager.FileExplorerPreferenceActivity;
 import com.ape.filemanager.FileExplorerTabActivity;
 import com.ape.filemanager.FileViewActivity.SelectFilesCallback;*/
import com.ape.filemanager.OptionsUtils;
import com.ape.filemanager.R;
import com.ape.newfilemanager.BaseAsyncTask.OperationEventListener;
import com.ape.newfilemanager.helper.FileOperationHelper.IOperationProgressListener;
import com.ape.newfilemanager.helper.FileSortHelper.SortMethod;
import com.ape.newfilemanager.interfaces.IFileInteractionListener;
import com.ape.newfilemanager.model.FileInfo;
import com.ape.newfilemanager.model.Settings;
import com.ape.newfilemanager.observer.FilesDataChange;
import com.ape.newfilemanager.view.FileMoreDialog;
import com.ape.newfilemanager.view.FileMoreDialog.MoreWatcher;
import com.ape.newfilemanager.view.FileSort2Dialog;
import com.ape.newfilemanager.view.FileSort2Dialog.SortOnClickWatcher;
import com.ape.newfilemanager.view.InformationDialog;
import com.ape.newfilemanager.view.InputDialog;
import com.ape.newfilemanager.view.TextInputDialog;
import com.ape.newfilemanager.view.TextInputDialog.OnFinishListener;
import com.ape.newfilemanager.view.Win8ProgressDialog;
import com.ape.utils.FileUtil;
import com.ape.utils.MyLog;
import com.ape.utils.ToastUtil;
import com.ape.utils.ZipUtil;
import com.ape.utils.ZipUtil.ZipOperationListener;

public class FileViewInteractionHub implements IOperationProgressListener {
	private static final String LOG_TAG = "FileViewInteractionHub";

	private IFileInteractionListener mFileViewListener;

	private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<FileInfo>();

	private FileOperationHelper mFileOperationHelper;

	private FileSortHelper mFileSortHelper;

	static private ProgressDialog progressDialog;

	private Context mContext;

	public enum Mode {
		View, Edit
	};

	public FileViewInteractionHub(IFileInteractionListener fileViewListener) {
		assert (fileViewListener != null);
		mFileViewListener = fileViewListener;
		setup();
		mContext = mFileViewListener.getmContext();
		mFileSortHelper = FileSortHelper.getInstance(mContext);
		mFileOperationHelper = new FileOperationHelper(this);
	}

	private void showProgress(String msg) {
		Log.i(LOG_TAG, "In showProgress, msg:" + msg);
		progressDialog = new ProgressDialog(mContext);
		// dialog.setIcon(R.drawable.icon);
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public void sortCurrentList() {
		mFileViewListener.sortCurrentList(mFileSortHelper);
	}

	public boolean canShowCheckBox() {

		return true;

	}

	public void addContextMenuSelectedItem() {
		Log.i(LOG_TAG, "addContextMenuSelectedItem, size:" + mCheckedFileNameList.size());
		if (mCheckedFileNameList.size() == 0) {
			int pos = mListViewContextMenuSelectedItem;
			if (pos != -1) {
				FileInfo fileInfo = mFileViewListener.getItem(pos);
				if (fileInfo != null) {
					mCheckedFileNameList.add(fileInfo);
				}
			}
		}
	}

	public ArrayList<FileInfo> getSelectedFileList() {
		return mCheckedFileNameList;
	}

	public boolean canPaste() {
		return mFileOperationHelper.canPaste();
	}

	// operation finish notification
	@Override
	public void onFinish() {
		Log.i(LOG_TAG, "onFinish, progressDialog:" + progressDialog);
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}

		mFileViewListener.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// showConfirmOperationBar(false);
				clearSelection();
				refreshFileList();
			}
		});
	}

	public boolean isInProgress() {
		return (progressDialog != null && progressDialog.isShowing());
	}

	public FileInfo getItem(int pos) {
		return mFileViewListener.getItem(pos);
	}

	public boolean isInSelection() {
		return mCheckedFileNameList.size() > 0;
	}

	public boolean isMoveState() { // move and copy state
		return mFileOperationHelper.isMoveState() || mFileOperationHelper.canPaste();
	}

	public boolean isOnlyMoveState() { // Only move state
		return mFileOperationHelper.isMoveState();
	}

	private void setup() {

		setupFileListView();

	}

	public void onOperationReferesh() {
		refreshFileList();
		mFileListView.post(new Runnable() {
			@Override
			public void run() {
				mFileListView.setSelection(0);
			}
		});
	}

	private void onOperationFavorite() {
		String path = mCurrentPath;

		if (mListViewContextMenuSelectedItem != -1) {
			path = mFileViewListener.getItem(mListViewContextMenuSelectedItem).filePath;
		}

		onOperationFavorite(path);
	}

	private void onOperationSetting() {

		// todo
		/*
		 * Intent intent = new Intent(mContext,
		 * FileExplorerPreferenceActivity.class); if (intent != null) { try {
		 * mContext.startActivity(intent); } catch (ActivityNotFoundException e)
		 * { Log.e(LOG_TAG, "fail to start setting: " + e.toString()); } }
		 */
	}

	private void onOperationFavorite(String path) {
		FavoriteDatabaseHelper databaseHelper = FavoriteDatabaseHelper.getInstance();
		if (databaseHelper != null) {
			int stringId = 0;
			if (databaseHelper.isFavorite(path)) {
				databaseHelper.delete(path);
				stringId = R.string.removed_favorite;
			} else {
				databaseHelper.insert(Util.getNameFromFilepath(path), path);
				stringId = R.string.added_favorite;
			}

			Toast.makeText(mContext, stringId, Toast.LENGTH_SHORT).show();
		}
	}

	private void onOperationShowSysFiles() {
		Settings.instance().setShowDotAndHiddenFiles(!Settings.instance().getShowDotAndHiddenFiles());
		refreshFileList();
	}

	public void onOperationSelectAllOrCancel() {
		if (!isSelectedAll()) {
			onOperationSelectAll();
		} else {
			clearSelection();
		}
	}

	public void onOperationSelectAll() {
		mCheckedFileNameList.clear();
		for (FileInfo f : mFileViewListener.getAllFiles()) {
			f.Selected = true;
			mCheckedFileNameList.add(f);
		}

		mFileViewListener.onDataChanged();
	}

	public boolean onOperationUpLevel() {

		if (mFileViewListener.onOperation(GlobalConsts.OPERATION_UP_LEVEL)) {
			return true;
		}

		List<String> paths = MountPointManager.getInstance().getMountPointPaths();
		for (String path : paths) {
			if (path.equals(mCurrentPath)) {
				mCurrentPath = Util.getTinnoRootPath();
				refreshFileList();
				return true;
			}
		}

		if (!mRoot.equals(mCurrentPath)) {
			mCurrentPath = new File(mCurrentPath).getParent();
			refreshFileList();
			return true;
		}

		return false;
	}

	public void onOperationCreateFolder() {
		TextInputDialog dialog = new TextInputDialog(mContext, mContext.getString(R.string.operation_create_folder),
				mContext.getString(R.string.operation_create_folder_message),
				mContext.getString(R.string.new_folder_name), new OnFinishListener() {
					@Override
					public boolean onFinish(String text) {
						return doCreateFolder(text);
					}
				});

		dialog.show();
	}

	private boolean doCreateFolder(String text) {
		if (TextUtils.isEmpty(text))
			return false;

		int result = mFileOperationHelper.CreateFolder(mCurrentPath, text);
		if (result == OperationEventListener.ERROR_CODE_SUCCESS) {
			mFileViewListener.addSingleFile(Util.GetFileInfo(Util.makePath(mCurrentPath, text)));
			mFileListView.setSelection(mFileListView.getCount() - 1);
		} else {
			onTaskResult(result, R.string.fail_to_create_folder);
			return false;
		}

		return true;
	}

	public void afterChangedToActionMode(FileInfo fileInfo) {
		final int endPos = mFileViewListener.getItemCount() - 1;
		FileInfo endFileInfo = mFileViewListener.getItem(endPos);
		if (endFileInfo != null && endFileInfo.equals(fileInfo)) {
			mFileListView.post(new Runnable() {
				@Override
				public void run() {
					mFileListView.setSelection(endPos);
				}
			});
		}
	}

	public void onOperationSearch() {

	}

	public void onSortChanged(SortMethod s) {
		if (mFileSortHelper.getSortMethod() != s) {
			mFileSortHelper.setSortMethod(s);
			sortCurrentList();
		}
	}

	public void onOperationCopy() {

		onOperationCopy(getSelectedFileList());
	}

	public void onOperationCopy(ArrayList<FileInfo> files) {
		mFileOperationHelper.Copy(files);
		clearSelection();

		refreshFileList();
	}

	public void onOperationCopyPath() {
		if (getSelectedFileList().size() == 1) {
			copy(getSelectedFileList().get(0).filePath);
		}
		clearSelection();
	}

	private void copy(CharSequence text) {
		ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		cm.setText(text);
	}

	private void onOperationPaste() {

		MyLog.i(LOG_TAG, " in  onOperationPaste  mCurrentPath=" + mCurrentPath);

		if (mFileOperationHelper.Paste(mCurrentPath)) {
			showProgress(mContext.getString(R.string.operation_pasting));
		}
	}

	public void onOperationMove() {
		mFileOperationHelper.StartMove(getSelectedFileList());
		clearSelection();
		refreshFileList();
	}

	public void refreshFileList() {
		clearSelection();

		mFileViewListener.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);
			}
		});

		mLastAccessPath = mCurrentPath;
		mModifiedTime = (new File(mLastAccessPath)).lastModified();
	}

	public void refreshCheckFileList() {

		// onRefreshFileList returns true indicates list has changed
		mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);

	}

	public boolean isCurrentPathModified() {
		if (mCurrentPath != null && !mCurrentPath.equals(mLastAccessPath)) {
			return true;
		}
		if (mLastAccessPath != null && mModifiedTime != (new File(mLastAccessPath)).lastModified()) {
			return true;
		}
		return false;
	}

	public void onOperationSend() {
		ArrayList<FileInfo> selectedFileList = getSelectedFileList();
		for (FileInfo f : selectedFileList) {
			if (f.IsDir) {
				AlertDialog dialog = new AlertDialog.Builder(mContext).setMessage(R.string.error_info_cant_send_folder)
						.setPositiveButton(R.string.confirm, null).create();
				dialog.show();
				clearSelection();
				return;
			}
		}

		Intent intent = IntentBuilder.buildSendFile(selectedFileList, mContext);
		if (intent != null) {
			try {
				Intent intent2 = Intent.createChooser(intent, mContext.getString(R.string.send_file));
				mFileViewListener.startActivity(intent2);
			} catch (ActivityNotFoundException e) {
				Log.e(LOG_TAG, "fail to view file: " + e.toString());
			}
		}

	}

	public void onOperationRename() {
		int pos = mListViewContextMenuSelectedItem;
		if (pos == -1)
			return;

		if (getSelectedFileList().size() == 0)
			return;

		final FileInfo f = getSelectedFileList().get(0);
		clearSelection();

		TextInputDialog dialog = new TextInputDialog(mContext, mContext.getString(R.string.operation_rename),
				mContext.getString(R.string.operation_rename_message), f.fileName, new OnFinishListener() {
					@Override
					public boolean onFinish(String text) {
						return doRename(f, text);
					}

				});

		dialog.show();
	}

	private boolean doRename(final FileInfo f, String text) {
		if (TextUtils.isEmpty(text))
			return false;

		int result = mFileOperationHelper.Rename(f, text);
		if (result == OperationEventListener.ERROR_CODE_SUCCESS) {
			f.fileName = text;
			// mFileViewListener.onDataChanged();
			refreshFileList();
		} else {
			onTaskResult(result, R.string.fail_to_rename);
			return false;
		}

		return true;
	}

	private void notifyFileSystemChanged(String path) {
		if (path == null)
			return;
		final File f = new File(path);
		final Intent intent;
		if (f.isDirectory()) {
			intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
			intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
			Log.v(LOG_TAG, "directory changed, send broadcast:" + intent.toString());
		} else {
			intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intent.setData(Uri.fromFile(new File(path)));
			Log.v(LOG_TAG, "file changed, send broadcast:" + intent.toString());
		}
		mContext.sendBroadcast(intent);
	}

	public void onOperationDelete() {
		doOperationDelete(getSelectedFileList());
	}

	public void onOperationDelete(int position) {
		FileInfo file = mFileViewListener.getItem(position);
		if (file == null)
			return;

		ArrayList<FileInfo> selectedFileList = new ArrayList<FileInfo>();
		selectedFileList.add(file);
		doOperationDelete(selectedFileList);
	}

	private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
		final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>(selectedFileList);
		Dialog dialog = new AlertDialog.Builder(mContext)
				.setMessage(mContext.getString(R.string.operation_delete_confirm_message))
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mFileOperationHelper.Delete(selectedFiles)) {
							showProgress(mContext.getString(R.string.operation_deleting));
						}
						clearSelection();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearSelection();
					}
				}).create();
		dialog.show();
	}

	public void onOperationInfo() {
		if (getSelectedFileList().size() == 0)
			return;

		FileInfo file = getSelectedFileList().get(0);
		if (file == null)
			return;

		InformationDialog dialog = new InformationDialog(mContext, file, mFileViewListener.getFileIconHelper());
		dialog.show();
		clearSelection();
	}

	public void onOperationButtonConfirm() {

		MyLog.i(LOG_TAG, "  onOperationButtonConfirm ");

		if (mFileOperationHelper.isMoveState()) {

			MyLog.i(LOG_TAG, "  isMoveState ");
			if (mFileOperationHelper.EndMove(mCurrentPath)) {
				showProgress(mContext.getString(R.string.operation_moving));
			}
		} else {

			MyLog.i(LOG_TAG, "  onOperationPaste ");
			onOperationPaste();
		}
	}

	public void onOperationButtonCancel() {
		mFileOperationHelper.clear();

		if (mFileOperationHelper.isMoveState()) {
			// refresh to show previously selected hidden files
			mFileOperationHelper.EndMove(null);
			refreshFileList();
		} else {
			refreshFileList();
		}

	}

	public void onOperationButtonSort() {

		final FileSort2Dialog sortdialog = new FileSort2Dialog(mContext);

		sortdialog.selectPosion = mFileSortHelper.getSortMethod().ordinal();

		sortdialog.setCanceledOnTouchOutside(true);

		sortdialog.setSortOnClickWatcher(new SortOnClickWatcher() {

			@Override
			public void onTypeClick() {
				onSortChanged(SortMethod.type);
				FilesDataChange.getInstance().notifydatachange();
				sortdialog.dismiss();
			}

			@Override
			public void onSizeClick() {
				onSortChanged(SortMethod.size);
				FilesDataChange.getInstance().notifydatachange();
				sortdialog.dismiss();
			}

			@Override
			public void onNameClick() {
				onSortChanged(SortMethod.name);
				FilesDataChange.getInstance().notifydatachange();
				sortdialog.dismiss();
			}

			@Override
			public void onDateClick() {
				onSortChanged(SortMethod.date);
				FilesDataChange.getInstance().notifydatachange();
				sortdialog.dismiss();
			}
		});

		sortdialog.show();
	}

	public void onOperationButtonMore() {

		final FileMoreDialog moredialog = new FileMoreDialog(mContext);

		moredialog.setCanceledOnTouchOutside(true);
		moredialog.setMoreWatcher(new MoreWatcher() {

			@Override
			public void zipOnclick(View v) {

				moredialog.dismiss();
				onOperationZip();

			}

			@Override
			public void shareOnclick(View v) {
				moredialog.dismiss();
				onOperationSend();

			}
		});

		moredialog.show();

	}

	// File List view setup
	private ListView mFileListView;

	private int mListViewContextMenuSelectedItem;

	private void setupFileListView() {
		mFileListView = (ListView) mFileViewListener.getViewById(R.id.explorer_list);
		mFileListView.setLongClickable(true);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
	}

	private int mCurrentMode;

	private static String mCurrentPath;

	private String mRoot;

	private String mLastAccessPath = null;
	protected long mModifiedTime = -1;

	public boolean isFileSelected(String filePath) {
		return mFileOperationHelper.isFileSelected(filePath);
	}

	public boolean isCheckedFile(String filePath) {
		synchronized (mCheckedFileNameList) {
			for (FileInfo f : mCheckedFileNameList) {
				if (f.filePath.equalsIgnoreCase(filePath))
					return true;
			}
		}
		return false;
	}

	public void setMode(Mode m) {
		mCurrentMode = m.ordinal();
	}

	public Mode getMode() {
		return Mode.values()[mCurrentMode];
	}

	public void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileInfo lFileInfo = mFileViewListener.getItem(position);
		// showDropdownNavigation(false);

		if (lFileInfo == null) {
			Log.e(LOG_TAG, "file does not exist on position:" + position);
			return;
		}

		if (isInSelection()) {
			boolean selected = lFileInfo.Selected;

			ImageView checkBox = (ImageView) view.findViewById(R.id.file_checkbox);
			if (selected) {
				mCheckedFileNameList.remove(lFileInfo);
				checkBox.setImageResource(R.drawable.new_select_icon_unfocus);
			} else {
				mCheckedFileNameList.add(lFileInfo);
				checkBox.setImageResource(R.drawable.new_select_icon_focus);
			}

			lFileInfo.Selected = !selected;

			return;
		}

		if (!lFileInfo.IsDir) {
			viewFile(lFileInfo);

			if (mCurrentMode == Mode.Edit.ordinal()) {
				mFileViewListener.onPick(lFileInfo);
			} else {
				viewFile(lFileInfo);
			}

			return;
		}

		// mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
		mCurrentPath = lFileInfo.filePath;

		refreshFileList();
	}

	public void setRootPath(String path) {
		mRoot = path;
		mCurrentPath = path;
	}

	public String getRootPath() {
		return mRoot;
	}

	public String getCurrentPath() {
		return mCurrentPath;
	}

	public void setCurrentPath(String path) {
		mCurrentPath = path;
	}

	private String getAbsoluteName(String path, String name) {
		return path.equals(GlobalConsts.ROOT_PATH) ? path + name : path + File.separator + name;
	}

	// check or uncheck
	public boolean onCheckItem(FileInfo f, View v) {
		if (isMoveState())
			return false;

		MyLog.i(LOG_TAG, "  onCheckItem path =" + f.IsDir);
		if (f.IsDir && f.filePath.equals(getRootPath()))
			return false;

		if (f.Selected) {
			mCheckedFileNameList.add(f);
		} else {
			mCheckedFileNameList.remove(f);
		}
		return true;
	}

	public boolean isSelectedAll() {
		return mFileViewListener.getItemCount() != 0 && mCheckedFileNameList.size() == mFileViewListener.getItemCount();
	}

	public boolean isSelected() {
		return mCheckedFileNameList.size() != 0;
	}

	public void clearSelection() {
		if (mCheckedFileNameList.size() > 0) {
			for (FileInfo f : mCheckedFileNameList) {
				if (f == null) {
					continue;
				}
				f.Selected = false;
			}
			mCheckedFileNameList.clear();
			mFileViewListener.onDataChanged();
		}
	}

	private void viewFile(FileInfo lFileInfo) {
		try {
			IntentBuilder.viewFile(mContext, lFileInfo.filePath, true);
		} catch (ActivityNotFoundException e) {
			Log.e(LOG_TAG, "fail to view file: " + e.toString());
		}
	}

	public boolean onBackPressed() {

		if (isInSelection()) {
			clearSelection();
		} else if (!onOperationUpLevel()) {
			MyLog.i(LOG_TAG, " onBackPressed return false ");
			return false;
		}

		MyLog.i(LOG_TAG, " onBackPressed return true ");
		return true;
	}

	public void copyFile(ArrayList<FileInfo> files) {
		mFileOperationHelper.Copy(files);
	}

	public void moveFileFrom(ArrayList<FileInfo> files) {
		mFileOperationHelper.StartMove(files);

		refreshFileList();
	}

	@Override
	public void onFileChanged(String path) {
		notifyFileSystemChanged(path);
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	public void onTaskResult(int errorCode, int operatorStrId) {
		int alertStringId = 0;

		switch (errorCode) {
		case OperationEventListener.ERROR_CODE_FILE_EXIST:
			alertStringId = R.string.file_exist;
			break;

		case OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE:
			alertStringId = R.string.insufficient_memory;
			break;

		case OperationEventListener.ERROR_CODE_NAME_EMPTY:
			alertStringId = R.string.file_name_empty;
			break;

		case OperationEventListener.ERROR_CODE_NO_PERMISSION:
			alertStringId = R.string.no_permission;

		default:
			break;
		}

		if (alertStringId != 0) {
			StringBuffer message = new StringBuffer();
			String prompt = mContext.getString(alertStringId);
			if (operatorStrId == 0) {
				message.append(prompt);
			} else {
				message.append(mContext.getString(operatorStrId, prompt));
			}
			AlertDialog dialog = new AlertDialog.Builder(mContext).setMessage(message)
					.setPositiveButton(R.string.confirm, null).create();
			dialog.show();
		}
	}

	public void onOperationZip() {

		zipFile(mContext);

	}

	private File[] getRealFiles() {
		ArrayList<FileInfo> FileInfos = getSelectedFileList();
		File[] files2 = new File[FileInfos.size()];
		for (int i = 0; i < files2.length; i++) {

			File tempfile = new File(FileInfos.get(i).filePath);

			files2[i] = tempfile;
		}
		return files2;

	}

	public void zipFile(final Context context) {

		final File[] files = getRealFiles();
		if (files.length > 0) {
			if (files.length == 1) {
				File sourceFile = files[0];
				String path = "";

				String suffix = FileUtil.getFileSuffix(sourceFile);
				MyLog.i(LOG_TAG, "  zipFile  getAbsolutePath= " + sourceFile.getAbsolutePath());
				if (suffix.length() > 0) {
					path = sourceFile.getAbsolutePath().replaceAll(suffix + "$", "zip");
				} else {
					path = sourceFile.getAbsolutePath() + ".zip";
				}
				MyLog.i(LOG_TAG, "  zipFile  path= " + path);
				File destFile = new File(path);

				int cnt = 0;
				while (destFile.exists() && cnt < 100) {

					cnt++;
					destFile = new File(FileUtil.getFilepriffix(sourceFile) + String.valueOf(cnt) + ".zip");

				}
				zipWithDialog(context, files, destFile);
			} else {
				new InputDialog.Builder(context).setTitle("输入文件名").setButtonText("确定", "取消").setCancelable(true)
						.setCanceledOnTouchOutside(true).setOnClickListener(new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (arg1 == DialogInterface.BUTTON_POSITIVE) {
									InputDialog dialog = (InputDialog) arg0;
									if (!TextUtils.isEmpty(dialog.InputString)) {
										String fname = dialog.InputString;
										if (!fname.endsWith(".zip")) {
											if (fname.endsWith(".")) {
												fname += "zip";
											} else {
												fname += ".zip";
											}
										}
										MyLog.i(LOG_TAG, "  zip to path =" + getCurrentPath());

										File destFile = new File(getCurrentPath(), fname);
										zipWithDialog(context, files, destFile);
									}
								}
							}
						}).create().show();
			}
		} else {
			// do nothing
		}
	}

	private void zipWithDialog(final Context context, File[] sourceFile, final File destFile) {

		final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(context).setCancelable(false)
				.setCanceledOnTouchOutside(false).create();
		dialog.show();

		ZipUtil.zipAsync(sourceFile, destFile, "", new ZipOperationListener() {

			@Override
			public void onProgress(int progress) {
				// TODO
			}

			@Override
			public void onError(String e) {
				dialog.dismiss();
				refreshFileList();
				ToastUtil.showToast(context, "压缩文件出错");
			}

			@Override
			public void onComplete() {
				dialog.dismiss();
				refreshFileList();
				ToastUtil.showToast(context, "压缩文件成功");
				mFileOperationHelper.zipUpdate(destFile.getAbsolutePath());
			}

			@Override
			public void onCancelled() {
				dialog.dismiss();

				ToastUtil.showToast(context, "压缩文件已取消");
			}
		});
	}
}
