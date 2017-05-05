package com.ape.newfilemanager;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ape.filemanager.FileExplorerTabActivity;
import com.ape.filemanager.R;
import com.ape.filemanager.search.FileManagerNewSearchActivity;
import com.ape.newfilemanager.helper.MountPointManager;
import com.ape.newfilemanager.view.FileManagerBottomView;
import com.ape.newfilemanager.view.FileManagerHeaderView;
import com.ape.newfilemanager.view.FileManagerOperationView;
import com.ape.utils.MyLog;

public class FileExplorerMainPagerActiviy extends Activity {

	private static final String TAG = "FileExplorerMainPagerActiviy";

	private Context mContext;

	private LinearLayout header;

	private FrameLayout fragment_container, rootview;

	private Button oldfilemanager;

	private TextView showMessage;

	public FileManagerHeaderView headerView;
	public FileManagerBottomView bottomView;
	public FileManagerOperationView operationView;

	MountPointManager mMountPointManager;

	public FragmentManager mFragmentManager;
	public FragmentTransaction mFragmentTransaction;

	public CategoryFragment mCategoryFragment = null;
	public FileExplorerFragment mFileExplorerFragment = null;
	public RemoteManagerFragment mRemoteManagerFragment = null;
	public DirectoryExplorerFragment mDirectoryExplorerFragment = null;

	public final static int CategoryFragment = 0;
	public final static int FileExplorerFragment = 1;
	public final static int ALLFileFragment = 2;
	public final static int RemoteManagerFragment = 3;

	IBackPressedListener mIBackPressedListener = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		MyLog.i(TAG, "onCreate");
		setContentView(R.layout.filemanager_mainpager_activity);

		mFragmentManager = getFragmentManager();
		initView();

		mMountPointManager = MountPointManager.getInstance();
		mMountPointManager.init(this);
	}

	private void initView() {

		rootview = (FrameLayout) this.findViewById(R.id.rootview);

		header = (LinearLayout) this.findViewById(R.id.header);

		showMessage = (TextView) this.findViewById(R.id.showMessage);
		showMessage.setVisibility(View.GONE);

		fragment_container = (FrameLayout) this.findViewById(R.id.fragment_container);

		oldfilemanager = (Button) this.findViewById(R.id.oldfilemanager);

		oldfilemanager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();

				intent.setClass(mContext, FileExplorerTabActivity.class);

				startActivity(intent);

			}
		});

		initHeader();

		fragmentSwtich(CategoryFragment);
	}

	public View getRootView() {

		return rootview;
	}

	private void initHeader() {

		headerView = new FileManagerHeaderView(mContext);
		bottomView = (FileManagerBottomView) this.findViewById(R.id.bottonbar);
		operationView = (FileManagerOperationView) this.findViewById(R.id.operation_bar);
		operationView.setVisibility(View.GONE);
		header.addView(headerView);
	}

	public void fragmentSwtich(int frag) {
		mFragmentTransaction = mFragmentManager.beginTransaction();
		if (mCategoryFragment == null) {
			mCategoryFragment = new CategoryFragment();
		}
		if (mFileExplorerFragment == null) {
			mFileExplorerFragment = new FileExplorerFragment();
		}

		if (mRemoteManagerFragment == null) {
			mRemoteManagerFragment = new RemoteManagerFragment();
		}

		if (mDirectoryExplorerFragment == null) {

			mDirectoryExplorerFragment = new DirectoryExplorerFragment();
		}

		if (mFileExplorerFragment.isAdded()) {

			mFragmentTransaction.remove(mFileExplorerFragment);
		}
		if (mCategoryFragment.isAdded()) {
			mFragmentTransaction.remove(mCategoryFragment);
		}
		if (mRemoteManagerFragment.isAdded()) {
			mFragmentTransaction.remove(mRemoteManagerFragment);
		}
		if (mDirectoryExplorerFragment.isAdded()) {
			mFragmentTransaction.remove(mDirectoryExplorerFragment);
		}

		switch (frag) {
		default:
		case CategoryFragment:

			if (!mCategoryFragment.isAdded()) {
				mFragmentTransaction.add(R.id.fragment_container, mCategoryFragment, "CategoryFragment");
			}

			break;

		case FileExplorerFragment:

			if (!mFileExplorerFragment.isAdded()) {
				mFragmentTransaction.add(R.id.fragment_container, mFileExplorerFragment, "FileExplorerFragment");
			}

			break;
		case ALLFileFragment:
			if (!mDirectoryExplorerFragment.isAdded()) {
				mFragmentTransaction.add(R.id.fragment_container, mDirectoryExplorerFragment,
						"DirectoryExplorerFragment");
			}
			break;
		case RemoteManagerFragment:
			if (!mRemoteManagerFragment.isAdded()) {
				mFragmentTransaction.add(R.id.fragment_container, mRemoteManagerFragment, "RemoteManagerFragment");
			}

			break;

		}
		mIBackPressedListener = null;
		mFragmentTransaction.commit();
	}

	public MountPointManager getMountManager() {
		return mMountPointManager;
	}

	public void SetHeadViewMode(int mode) {
		headerView.setHeadMode(mode);

		if (mode == FileManagerHeaderView.HOMEPAGE) {
			oldfilemanager.setVisibility(View.VISIBLE);
		} else {
			oldfilemanager.setVisibility(View.GONE);
		}

	}
	
	int bottommode = Integer.MAX_VALUE;
	public void SetBottomViewMode(int mode) {

		bottomView.setBottomViewMode(mode);
		if(bottommode != mode){
			bottommode = mode;
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_to_enter);  
			bottomView.startAnimation(animation);
		}
		
	}

	public void ShowMessage(String str, boolean flag) {

		if (showMessage != null) {

			showMessage.setText(str);

			if (flag) {
				showMessage.setVisibility(View.VISIBLE);
			} else {

				showMessage.setVisibility(View.GONE);
			}
		}
	}

	public void startSearchActivity(String path) {
		Intent intent = new Intent();
		intent.setClass(this, FileManagerNewSearchActivity.class);
		intent.putExtra(FileManagerNewSearchActivity.CURRENT_PATH, path);
		intent.putExtra(FileManagerNewSearchActivity.IS_FROM_FILEMANAGER, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	// do with on onback event
	public interface IBackPressedListener {

		boolean onBack();
	}

	public void setOnbackWatcher(IBackPressedListener lis) {

		mIBackPressedListener = lis;
	}

	@Override
	public void onBackPressed() {

		if (mIBackPressedListener == null || mIBackPressedListener.onBack()) {
			super.onBackPressed();
		}
	}

}
