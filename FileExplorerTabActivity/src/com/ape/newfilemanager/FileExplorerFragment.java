package com.ape.newfilemanager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Observable;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ape.filemanager.R;
import com.ape.newfilemanager.FileExplorerMainPagerActiviy.IBackPressedListener;
import com.ape.newfilemanager.adapter.FileListCursorAdapter;
import com.ape.newfilemanager.helper.FileCategoryHelper;
import com.ape.newfilemanager.helper.FileCategoryHelper.FileCategory;
import com.ape.newfilemanager.helper.FileIconHelper;
import com.ape.newfilemanager.helper.FileSortHelper;
import com.ape.newfilemanager.helper.FileViewInteractionHub;
import com.ape.newfilemanager.helper.FileViewInteractionHub.Mode;
import com.ape.newfilemanager.helper.MountPointManager;
import com.ape.newfilemanager.helper.Util;
import com.ape.newfilemanager.interfaces.IFileInteractionListener;
import com.ape.newfilemanager.model.FileInfo;
import com.ape.newfilemanager.observer.FilesDataChange;
import com.ape.newfilemanager.observer.FilesWatcher;
import com.ape.newfilemanager.observer.ListNumDataChange;
import com.ape.newfilemanager.observer.ListNumWatcher;
import com.ape.newfilemanager.view.CircleProgressDialog;
import com.ape.newfilemanager.view.FileManagerBottomView;
import com.ape.newfilemanager.view.FileManagerBottomView.onClickWatcher;
import com.ape.newfilemanager.view.FileManagerHeaderView;
import com.ape.newfilemanager.view.FileManagerHeaderView.onClickListenr;
import com.ape.newfilemanager.view.FileManagerOperationView.OperateOnClickWatcher;
import com.ape.utils.MyLog;
import com.ape.utils.ToastUtil;

public class FileExplorerFragment extends Fragment {

	private static final String TAG = "FileExplorerFragment";

	private Context mContext;
	private FileExplorerMainPagerActiviy mActivity;

	private View mRootView;
	private ListView explorer_list;
	private LinearLayout mSearchView;

	private FileListCursorAdapter mAdapter;
	private FileViewInteractionHub mFileViewInteractionHub;
	private FileIconHelper mFileIconHelper;

	private MountPointManager mMountPointManager;
	FileCategoryHelper fileCategoryHelper;

	FileSortHelper mFileSortHelper;

	private Handler mHandler;
	private CircleProgressDialog progressDialog = null;

	private Cursor mCursor;

	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

	public FileExplorerFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (FileExplorerMainPagerActiviy) getActivity();
		mContext = mActivity;
		mHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {

				progressDialog = new CircleProgressDialog(mActivity);
				progressDialog.setCancelable(false);
				progressDialog.setText(mActivity
						.getString(R.string.files_loading));
				progressDialog.showDialog();

				return false;
			}
		});

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(
				R.layout.filemanager_explorer_list_fragment, null);

		initSearchView();

		explorer_list = (ListView) mRootView.findViewById(R.id.explorer_list);

		mFileViewInteractionHub = new FileViewInteractionHub(
				mIFileInteractionListener);
		mFileViewInteractionHub.setRootPath("/");

		mFileIconHelper = new FileIconHelper(mContext);
		mMountPointManager = mActivity.getMountManager();
		fileCategoryHelper = FileCategoryHelper.getInstance(mContext);
		mFileSortHelper = FileSortHelper.getInstance(mContext);

		initView();

		return mRootView;
	}

	private void initSearchView() {

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
		ListNumDataChange.getInstance().addObserver(new ListNumWatcher() {

			@Override
			public void update(Observable observable, Object data) {
				super.update(observable, data);
				// true listnum > 0
				int listnum = (Integer) data;

				MyLog.i(TAG, "  listnum =" + listnum);
				if (listnum > 0) {

					mActivity.ShowMessage(" ", false);
					if (mAdapter.getCount() > 0) {
						mActivity
								.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
						mActivity
								.SetBottomViewMode(FileManagerBottomView.FILE_NOMAL);
					} else {
						mActivity
								.SetHeadViewMode(FileManagerHeaderView.RETURN_MODE);
						mActivity
								.SetBottomViewMode(FileManagerBottomView.NO_FILES);
					}
				} else {

					if (fileCategoryHelper != null) {

						switch (fileCategoryHelper.getCurCategory()) {
						case Music:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_music_tips),
									true);
							break;
						case Video:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_video_tips),
									true);
							break;
						case Picture:

							mActivity.ShowMessage(mContext
									.getString(R.string.no_picture_tips), true);
							break;
						case Apk:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_apk_tips),
									true);
							break;
						case Doc:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_doc_tips),
									true);
							break;
						case Zip:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_zip_tips),
									true);
							break;

						default:

							mActivity.ShowMessage(
									mContext.getString(R.string.no_file_tips),
									true);
							break;
						}

					}

				}
			}
		});
	}

	private void initView() {

		/*
		 * mCursor =
		 * fileCategoryHelper.query(fileCategoryHelper.getCurCategory(),
		 * mFileSortHelper.getSortMethod());
		 */

		refleshListView(300);

		mAdapter = new FileListCursorAdapter(mActivity, null,
				mFileViewInteractionHub, mFileIconHelper, false);

		explorer_list.setAdapter(mAdapter);

		if (mAdapter.getCount() > 0) {
			mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
			mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NOMAL);
		} else {
			mActivity.SetHeadViewMode(FileManagerHeaderView.RETURN_MODE);
			mActivity.SetBottomViewMode(FileManagerBottomView.NO_FILES);
		}

		mActivity.headerView.setOnClickListener(new onClickListenr() {

			@Override
			public void HeadViewSelectAllOnclick(View v) {

				mFileViewInteractionHub.onOperationSelectAllOrCancel();
			}

			@Override
			public void HeadViewReturnOnclick(View v) {

				mActivity
						.fragmentSwtich(FileExplorerMainPagerActiviy.CategoryFragment);
			}

			@Override
			public void HeadViewEditOnclick(View v) {

				mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_EIDT);
				mActivity.SetBottomViewMode(FileManagerBottomView.FILE_EIDT);
				mFileViewInteractionHub.setMode(Mode.Edit);
				mFileViewInteractionHub.refreshFileList();
			}

			@Override
			public void HeadViewCancelOnclick(View v) {

				mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
				mActivity.SetBottomViewMode(FileManagerBottomView.FILE_NOMAL);
				mFileViewInteractionHub.setMode(Mode.View);
				mFileViewInteractionHub.refreshFileList();
			}
		});

		mActivity.bottomView.setOnClickWatcher(new onClickWatcher() {

			@Override
			public void sortOnclick(View v) {

				MyLog.i(TAG, " diag show   ");

				mFileViewInteractionHub.onOperationButtonSort();

			}

			@Override
			public void shearOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {

					mFileViewInteractionHub.onOperationMove();
					mActivity
							.fragmentSwtich(FileExplorerMainPagerActiviy.ALLFileFragment);
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.headerView.headtitle
							.setText(R.string.category_allfiles);
					mActivity.operationView.setVisibility(View.VISIBLE);

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}
			}

			@Override
			public void refleshOnclick(View v) {

				refleshListView(0);
			}

			@Override
			public void newFoldOnclick(View v) {

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

			@Override
			public void deleteOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {

					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_EIDT);
					mFileViewInteractionHub.onOperationDelete();

				} else {

					ToastUtil.showToast(mActivity,
							R.string.please_select_files, Toast.LENGTH_SHORT);
				}

			}

			@Override
			public void copyOnclick(View v) {

				if (mFileViewInteractionHub.getSelectedFileList().size() > 0) {

					mFileViewInteractionHub.onOperationCopy();

					mActivity
							.fragmentSwtich(FileExplorerMainPagerActiviy.ALLFileFragment);
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity.headerView.headtitle
							.setText(R.string.category_allfiles);

					mActivity.operationView.setVisibility(View.VISIBLE);

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
						// TODO Auto-generated method stub

						mFileViewInteractionHub.onOperationButtonConfirm();
						mActivity.operationView.setVisibility(View.GONE);

					}

					@Override
					public void cancelOnclick(View v) {
						// TODO Auto-generated method stub
						mFileViewInteractionHub.onOperationButtonCancel();
						mActivity.operationView.setVisibility(View.GONE);
					}
				});

		FilesDataChange.getInstance().addObserver(new FilesWatcher() {

			@Override
			public void update(Observable observable, Object data) {
				super.update(observable, data);

				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mCursor = fileCategoryHelper.query(
								fileCategoryHelper.getCurCategory(),
								mFileSortHelper.getSortMethod());
						mAdapter.changeCursor(mCursor);
						mFileViewInteractionHub.refreshFileList();

					}
				});
			}

		});

		mActivity.setOnbackWatcher(new IBackPressedListener() {
			@Override
			public boolean onBack() {

				if (mFileViewInteractionHub.getMode() == Mode.Edit) {
					mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
					mActivity
							.SetBottomViewMode(FileManagerBottomView.FILE_NOMAL);
					mFileViewInteractionHub.setMode(Mode.View);
					mFileViewInteractionHub.refreshFileList();
				} else {
					mActivity
							.fragmentSwtich(FileExplorerMainPagerActiviy.CategoryFragment);
				}

				return false;
			}
		});

	}

	private void refleshListView(final long time) {

		new Thread(new Runnable() {

			int mWhat = 10;

			@Override
			public void run() {

				long curtime = System.currentTimeMillis();
				mHandler.sendEmptyMessageDelayed(mWhat, time);

				mCursor = fileCategoryHelper.query(
						fileCategoryHelper.getCurCategory(),
						mFileSortHelper.getSortMethod());
				long curtime2 = System.currentTimeMillis();

				long difftime = curtime2 - curtime;
				if (difftime < 500) {

					try {
						Thread.sleep(1000 - difftime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mHandler.hasMessages(mWhat)) {
							mHandler.removeMessages(mWhat);
						}

						if (progressDialog != null
								&& progressDialog.isShowing()) {

							progressDialog.dismiss();
							progressDialog = null;
						}

						mAdapter.changeCursor(mCursor);
						mAdapter.setWatcherFlag(true);
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}).start();

	}

	private void onCategorySelected(FileCategory f) {
		if (fileCategoryHelper.getCurCategory() != f) {
			fileCategoryHelper.setCurCategory(f);
			mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub
					.getRootPath()
					+ getString(fileCategoryHelper.getCurCategoryNameResId()));
			mFileViewInteractionHub.setMode(Mode.View);

		}

	}

	IFileInteractionListener mIFileInteractionListener = new IFileInteractionListener() {

		@Override
		public void startActivity(Intent intent) {

			mActivity.startActivity(intent);
		}

		@Override
		public Activity getActivity() {

			return mActivity;
		}

		@Override
		public void sortCurrentList(FileSortHelper sort) {

			// todo
			Collections.sort(mFileNameList, sort.getComparator());

			onDataChanged();
		}

		@Override
		public void runOnUiThread(Runnable r) {
			mActivity.runOnUiThread(r);
		}

		@Override
		public boolean onRefreshFileList(String path, FileSortHelper sort) {
			onDataChanged();
			return true;
		}

		@Override
		public void onPick(FileInfo f) {
			try {
				Intent intent = Intent.parseUri(
						Uri.fromFile(new File(f.filePath)).toString(), 0);
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

		@Override
		public void onDataChanged() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mAdapter.notifyDataSetChanged();
				}

			});
		}

		@Override
		public View getViewById(int id) {

			return mRootView.findViewById(id);
		}

		@Override
		public String getRealPath(String displayPath) {

			return "";
		}

		@Override
		public int getItemCount() {

			return mAdapter.getCount();
		}

		@Override
		public FileInfo getItem(int pos) {

			return mAdapter.getFileItem(pos);
		}

		@Override
		public FileIconHelper getFileIconHelper() {

			return mFileIconHelper;
		}

		@Override
		public String getDisplayPath(String path) {
			return getString(R.string.tab_category) + path;
		}

		@Override
		public Context getmContext() {
			return mActivity;
		}

		@Override
		public Collection<FileInfo> getAllFiles() {
			return mAdapter.getAllFiles();
		}

		@Override
		public void addSingleFile(FileInfo file) {
			// TODO Auto-generated method stub

		}

	};
}
