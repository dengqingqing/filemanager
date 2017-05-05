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

package com.ape.newfilemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ape.filemanager.FileExplorerPreferenceActivity;
import com.ape.filemanager.R;
import com.ape.newfilemanager.FileExplorerMainPagerActiviy.IBackPressedListener;
import com.ape.newfilemanager.adapter.FileListAdapter;
import com.ape.newfilemanager.helper.FileCategoryHelper;
import com.ape.newfilemanager.helper.FileIconHelper;
import com.ape.newfilemanager.helper.FileSortHelper;
import com.ape.newfilemanager.helper.FileViewInteractionHub;
import com.ape.newfilemanager.helper.FileViewInteractionHub.Mode;
import com.ape.newfilemanager.helper.GlobalConsts;
import com.ape.newfilemanager.helper.MountPointManager;
import com.ape.newfilemanager.helper.Util;
import com.ape.newfilemanager.interfaces.IFileInteractionListener;
import com.ape.newfilemanager.model.FileInfo;
import com.ape.newfilemanager.model.Settings;
import com.ape.newfilemanager.view.CircleProgressDialog;
import com.ape.newfilemanager.view.FileManagerBottomView;
import com.ape.newfilemanager.view.FileManagerBottomView.onClickWatcher;
import com.ape.newfilemanager.view.FileManagerHeaderView;
import com.ape.newfilemanager.view.FileManagerHeaderView.onClickListenr;
import com.ape.newfilemanager.view.FileManagerOperationView.OperateOnClickWatcher;
import com.ape.utils.MyLog;
import com.ape.utils.ToastUtil;

public class DirectoryExplorerFragment extends Fragment implements
		IFileInteractionListener {

	private static final String LOG_TAG = "DirectoryExplorerFragment";

	public static final String ROOT_DIRECTORY = "root_directory";

	private ListView mFileListView;

	private ArrayAdapter<FileInfo> mAdapter;

	private FileViewInteractionHub mFileViewInteractionHub;

	private FileCategoryHelper mFileCagetoryHelper;

	private FileIconHelper mFileIconHelper;

	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

	private FileExplorerMainPagerActiviy mActivity;

	private View mRootView;
	private LinearLayout mSearchView;

	private static final String sdDir = Util.getSdDirectory();

	private MountPointManager mMountPointManager;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			Log.v(LOG_TAG, "received broadcast:" + intent.toString());
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {

				if (mMountPointManager != null) {
					mMountPointManager.init(mActivity);
				}
				if (mFileViewInteractionHub != null
						&& action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					Uri path = intent.getData();
					String ummountPath = path.getPath();
					if (mFileViewInteractionHub.getCurrentPath().startsWith(
							ummountPath)) {
						setPath(Util.getTinnoRootPath());
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (FileExplorerMainPagerActiviy) getActivity();
		mMountPointManager = ((FileExplorerMainPagerActiviy) mActivity)
				.getMountManager();

		mRootView = inflater.inflate(
				R.layout.filemanager_directoryexplorer_fragment, container,
				false);

		mFileCagetoryHelper = FileCategoryHelper.getInstance(mActivity);
		mFileViewInteractionHub = new FileViewInteractionHub(this);

		mFileViewInteractionHub.setMode(Mode.View);

		mFileListView = (ListView) mRootView.findViewById(R.id.explorer_list);
		mFileIconHelper = new FileIconHelper(mActivity);
		mAdapter = new FileListAdapter(mActivity,
				R.layout.file_browser_newitem, mFileNameList,
				mFileViewInteractionHub, mFileIconHelper);

		Intent intent = mActivity.getIntent();
		boolean baseSd = intent.getBooleanExtra(GlobalConsts.KEY_BASE_SD,
				!FileExplorerPreferenceActivity.isReadRoot(mActivity));
		Log.i(LOG_TAG, "baseSd = " + baseSd);

		String rootDir = intent.getStringExtra(ROOT_DIRECTORY);
		if (!TextUtils.isEmpty(rootDir)) {
			if (baseSd && this.sdDir.startsWith(rootDir)) {
				rootDir = this.sdDir;
			}
		} else {
			rootDir = baseSd ? this.sdDir : Util.getTinnoRootPath(); // GlobalConsts.ROOT_PATH;
		}
		mFileViewInteractionHub.setRootPath(rootDir);

		String currentDir = FileExplorerPreferenceActivity
				.getPrimaryFolder(mActivity);
		Uri uri = intent.getData();
		if (uri != null) {
			if (baseSd && this.sdDir.startsWith(uri.getPath())) {
				currentDir = this.sdDir;
			} else {
				currentDir = uri.getPath();
			}
		}
		mFileViewInteractionHub.setCurrentPath(currentDir);
		Log.i(LOG_TAG, "CurrentDir = " + currentDir);

		mFileListView.setAdapter(mAdapter);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addDataScheme("file");
		mActivity.registerReceiver(mReceiver, intentFilter);

		setSearchUI();

		updateUI();

		// setHasOptionsMenu(true);

		ViewOnclickInit();

		return mRootView;
	}

	private void setSearchUI() {
		mSearchView = (LinearLayout) mRootView
				.findViewById(R.id.search_view_frame);
		mSearchView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.search_view_frame && Util.isSDCardReady()) {
					FileExplorerMainPagerActiviy activity = (FileExplorerMainPagerActiviy) getActivity();
					activity.startSearchActivity(mFileViewInteractionHub
							.getCurrentPath());
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFileViewInteractionHub != null) {
			mFileViewInteractionHub.refreshCheckFileList();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mActivity.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {

		MyLog.i(LOG_TAG, "  refresh  path=" + path);

		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}

		if (!mFileViewInteractionHub.isInProgress() && !mIsLoading) {
			ListFilesTask listTask = new ListFilesTask(path, sort);
			listTask.execute();
		}

		return true;
	}

	private boolean mIsLoading = false;

	class ListFilesTask extends AsyncTask<Void, Object, Object> {
		private boolean needInvalidOptionMenu;
		private String mListPath;
		private FileSortHelper mListSort;
		Runnable mDelayDisplay;
		CircleProgressDialog progressDialog;
		ArrayList<FileInfo> mListFiles;

		public ListFilesTask(String path, FileSortHelper sort) {
			needInvalidOptionMenu = false;
			mIsLoading = true;
			mListPath = path;
			mListSort = sort;
			progressDialog = null;
			mDelayDisplay = new Runnable() {
				@Override
				public void run() {
					if (mIsLoading) {
						progressDialog = new CircleProgressDialog(mActivity);
						progressDialog.setCancelable(false);
						progressDialog.setText(mActivity
								.getString(R.string.files_loading));
						progressDialog.showDialog();
					}
				}
			};
		}

		@Override
		protected void onPreExecute() {
			mFileListView.postDelayed(mDelayDisplay, 500);
		}

		@Override
		protected void onPostExecute(Object result) {
			if (mListFiles != null) {
				mFileNameList.clear();
				mFileNameList.addAll(mListFiles);
				mListFiles.clear();
				mListFiles = null;
			}

			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			mFileListView.removeCallbacks(mDelayDisplay);

			mAdapter.notifyDataSetChanged();

			showEmptyView(mFileNameList.size() == 0 && Util.isSDCardReady());

			if (needInvalidOptionMenu) {
				mActivity.invalidateOptionsMenu();
			}

			mIsLoading = false;

			MyLog.i(LOG_TAG, "   mFileNameList = " + mFileNameList.size());

			if (mFileNameList.size() == 0
					|| mFileViewInteractionHub.getRootPath().equals(
							mFileViewInteractionHub.getCurrentPath())) {
				mActivity.SetHeadViewMode(FileManagerHeaderView.RETURN_MODE);
				mActivity.bottomView.setVisibility(View.GONE);
				mFileViewInteractionHub.setMode(Mode.View);

			} else {
				if (mFileViewInteractionHub.getMode() == Mode.View) {
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.bottomView.setVisibility(View.VISIBLE);
					mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NEW);
				}
			}

		}

		@SuppressWarnings("unchecked")
		@Override
		protected Object doInBackground(Void... params) {
			File file = new File(mListPath);
			mListFiles = new ArrayList<FileInfo>();
			mListFiles.clear();

			if (Util.getTinnoRootPath().equals(mListPath)) {
				mListFiles.addAll(mMountPointManager.getMountPointFileInfo());
			} else {
				FilenameFilter filter = mFileCagetoryHelper.getFilter();
				File[] listFiles = file.listFiles(filter);
				if (listFiles == null)
					return true;

				boolean isOnlyMoveState = mFileViewInteractionHub
						.isOnlyMoveState();
				boolean isShowHidden = Settings.instance()
						.getShowDotAndHiddenFiles();
				for (File child : listFiles) {
					// do not show selected file if in move state
					if (isOnlyMoveState
							&& mFileViewInteractionHub.isFileSelected(child
									.getPath()))
						continue;

					String absolutePath = child.getAbsolutePath();
					if (Util.isNormalFile(absolutePath)
							&& Util.shouldShowFile(absolutePath)) {
						FileInfo lFileInfo = Util.GetFileInfo(child, filter,
								isShowHidden);

						if (lFileInfo != null) {
							mListFiles.add(lFileInfo);
						}
					}
				}
			}

			Collections.sort(mListFiles, mListSort.getComparator());
			return null;
		}
	}

	private void updateUI() {
		boolean sdCardReady = Util.isSDCardReady();
		View noSdView = mRootView.findViewById(R.id.sd_not_available_page);
		noSdView.setVisibility(sdCardReady ? View.GONE : View.VISIBLE);

		mFileListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

		if (sdCardReady) {
			mFileViewInteractionHub.refreshFileList();
		}

		mSearchView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
	}

	private void showEmptyView(boolean show) {
		View emptyView = mRootView.findViewById(R.id.empty_view);
		if (emptyView != null)
			emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public View getViewById(int id) {
		return mRootView.findViewById(id);
	}

	public Context getmContext() {
		return mActivity;
	}

	@Override
	public void onDataChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!mIsLoading)
					mAdapter.notifyDataSetChanged();
			}

		});
	}

	@Override
	public void onPick(FileInfo f) {
		try {
			Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath))
					.toString(), 0);
			mActivity.setResult(Activity.RESULT_OK, intent);
			mActivity.finish();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOperation(int id) {
		return false;
	}

	// 支持显示真实路径
	@Override
	public String getDisplayPath(String path) {
		if (path.startsWith(this.sdDir)
				&& !FileExplorerPreferenceActivity.showRealPath(mActivity)) {
			return getString(R.string.sd_folder)
					+ path.substring(this.sdDir.length());
		} else {
			return path;
		}
	}

	@Override
	public String getRealPath(String displayPath) {
		final String perfixName = getString(R.string.sd_folder);
		if (displayPath.startsWith(perfixName)) {
			return this.sdDir + displayPath.substring(perfixName.length());
		} else {
			return displayPath;
		}
	}

	public void copyFile(ArrayList<FileInfo> files) {
		if (mFileViewInteractionHub != null) {
			mFileViewInteractionHub.onOperationCopy(files);
		}
	}

	public void refresh() {
		if (mFileViewInteractionHub != null) {
			mFileViewInteractionHub.refreshCheckFileList();
		}
	}

	public void moveToFile(ArrayList<FileInfo> files) {
		if (mFileViewInteractionHub != null) {
			mFileViewInteractionHub.moveFileFrom(files);
		}
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		return mFileIconHelper;
	}

	public boolean setPath(String location) {
		// if (!location.startsWith(mFileViewInteractionHub.getRootPath())) {
		// return false;
		// }
		mFileViewInteractionHub.setCurrentPath(location);
		mFileViewInteractionHub.refreshFileList();
		return true;
	}

	@Override
	public FileInfo getItem(int pos) {
		if (pos < 0 || pos > mFileNameList.size() - 1)
			return null;

		return mFileNameList.get(pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sortCurrentList(FileSortHelper sort) {
		Collections.sort(mFileNameList, sort.getComparator());
		onDataChanged();
	}

	@Override
	public ArrayList<FileInfo> getAllFiles() {
		return mFileNameList;
	}

	@Override
	public void addSingleFile(FileInfo file) {
		mFileNameList.add(file);
		onDataChanged();
		showEmptyView(false);
		// mFileViewInteractionHub.refreshCheckFileList();
	}

	@Override
	public int getItemCount() {
		return mFileNameList.size();
	}

	@Override
	public void runOnUiThread(Runnable r) {
		mActivity.runOnUiThread(r);
	}

	private void ViewOnclickInit() {

		mActivity.SetHeadViewMode(FileManagerHeaderView.RETURN_MODE);
		mActivity.headerView.setOnClickListener(new onClickListenr() {

			@Override
			public void HeadViewSelectAllOnclick(View v) {

				mFileViewInteractionHub.onOperationSelectAllOrCancel();

			}

			@Override
			public void HeadViewReturnOnclick(View v) {
				if (!mFileViewInteractionHub.onBackPressed()) {

					mActivity
							.fragmentSwtich(FileExplorerMainPagerActiviy.CategoryFragment);

				}
			}

			@Override
			public void HeadViewEditOnclick(View v) {
				mActivity.operationView.setVisibility(View.GONE);
				mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_EIDT);
				mActivity.SetBottomViewMode(FileManagerBottomView.FILE_EIDT);
				mFileViewInteractionHub.setMode(Mode.Edit);
				mFileViewInteractionHub.refreshFileList();
			}

			@Override
			public void HeadViewCancelOnclick(View v) {
				mActivity.operationView.setVisibility(View.GONE);
				mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
				mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NEW);
				mFileViewInteractionHub.setMode(Mode.View);
				mFileViewInteractionHub.refreshFileList();
			}
		});

		mActivity.bottomView.setOnClickWatcher(new onClickWatcher() {

			@Override
			public void sortOnclick(View v) {

				mFileViewInteractionHub.onOperationButtonSort();

			}

			@Override
			public void shearOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {
					mFileViewInteractionHub.onOperationMove();
					mActivity.operationView.setVisibility(View.VISIBLE);
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NEW);
					mFileViewInteractionHub.setMode(Mode.View);
					mFileViewInteractionHub.refreshFileList();

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}

			}

			@Override
			public void deleteOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {
					mFileViewInteractionHub.onOperationDelete();
					mActivity.operationView.setVisibility(View.GONE);
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NEW);
					mFileViewInteractionHub.setMode(Mode.View);
					mFileViewInteractionHub.refreshFileList();

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}
			}

			@Override
			public void copyOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {
					mFileViewInteractionHub.onOperationCopy();
					mActivity.operationView.setVisibility(View.VISIBLE);
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NEW);
					mFileViewInteractionHub.setMode(Mode.View);
					mFileViewInteractionHub.refreshFileList();

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}
			}

			@Override
			public void refleshOnclick(View v) {
				mIsLoading = false;
				refresh();
			};

			@Override
			public void newFoldOnclick(View v) {

				mFileViewInteractionHub.onOperationCreateFolder();

			}

			@Override
			public void moreOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {
					mFileViewInteractionHub.onOperationButtonMore();

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}

			}

		});

		mActivity.operationView
				.setOperateOnClickWatcher(new OperateOnClickWatcher() {

					@Override
					public void pasterOnclick(View v) {

						if (mFileViewInteractionHub.getRootPath().equals(
								mFileViewInteractionHub.getCurrentPath())) {

						} else {

							mActivity.operationView.setVisibility(View.GONE);
							mFileViewInteractionHub.onOperationButtonConfirm();
						}

					}

					@Override
					public void cancelOnclick(View v) {
						
						mActivity.operationView.setVisibility(View.GONE);
						mFileViewInteractionHub.onOperationButtonCancel();
					}
				});

		// set onbackpress keyenvent
		mActivity.setOnbackWatcher(new IBackPressedListener() {

			@Override
			public boolean onBack() {
				if (!Util.isSDCardReady() || mFileViewInteractionHub == null) {
					return false;
				}
				if (!mFileViewInteractionHub.onBackPressed()) {

					mActivity
							.fragmentSwtich(FileExplorerMainPagerActiviy.CategoryFragment);

				}
				return false;
			}
		});

	}

}
