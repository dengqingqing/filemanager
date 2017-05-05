package com.ape.newfilemanager;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.ape.filemanager.Util.SDCardInfo;
import com.ape.newfilemanager.adapter.CategoryAdapter;
import com.ape.newfilemanager.adapter.CategoryAdapter.ViewHold;
import com.ape.newfilemanager.helper.FileCategoryHelper;
import com.ape.newfilemanager.helper.FileCategoryHelper.FileCategory;
import com.ape.newfilemanager.model.Category;
import com.ape.newfilemanager.observer.ProviderQueryDataChange;
import com.ape.newfilemanager.observer.ProviderQueryWatcher;
import com.ape.newfilemanager.view.ButtonEntryView;
import com.ape.newfilemanager.view.FileManagerBottomView;
import com.ape.newfilemanager.view.FileManagerHeaderView;
import com.ape.utils.MyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class CategoryFragment extends Fragment {

	private static final String TAG = "CategoryFragment";
	private GridView category_gridview;
	private LinearLayout otherentry_linearlayout;
	private Context mContext;

	private FileExplorerMainPagerActiviy mActivity;

	private CategoryAdapter mCategoryAdapter;

	FileCategoryHelper fileCategoryHelper;

	List<Category> categoryDataList = null;
	ProviderQueryWatcher watcher;

	public CategoryFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();

		mActivity = (FileExplorerMainPagerActiviy) getActivity();

		fileCategoryHelper = FileCategoryHelper.getInstance(mContext);

		fileCategoryHelper.refreshCategoryInfo();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater
				.inflate(R.layout.category_mainpager_fragment, null);

		category_gridview = (GridView) view
				.findViewById(R.id.category_gridview);

		otherentry_linearlayout = (LinearLayout) view
				.findViewById(R.id.otherentry_linearlayout);

		mActivity.SetHeadViewMode(FileManagerHeaderView.HOMEPAGE);
		mActivity.SetBottomViewMode(FileManagerBottomView.HOMEPAGE);

		initCategoryData();

		initOtherEntryview();

		mActivity.ShowMessage(" ", false);

		watcher = new ProviderQueryWatcher() {

			@Override
			public void update(Observable observable, Object data) {

				MyLog.i(TAG, " update ");
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {

						MyLog.i(TAG, " mCategoryAdapter.reflesh() ");
						upateCategoryDataList();
						mCategoryAdapter.reflesh();
					}
				});
			}

		};

		ProviderQueryDataChange.getInstance().addObserver(watcher);

		return view;

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		ProviderQueryDataChange.getInstance().deleteObserver(watcher);
	}

	private void initCategoryData() {

		categoryDataList = new ArrayList<Category>();

		MyLog.i(TAG, " getCategoryInfos= "
				+ fileCategoryHelper.getCategoryInfos().size());

		upateCategoryDataList();

		mCategoryAdapter = new CategoryAdapter(mContext, categoryDataList);

		category_gridview.setAdapter(mCategoryAdapter);

		category_gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				MyLog.i(TAG, "  position=" + position);

				CategoryAdapter.ViewHold viewhold = (ViewHold) view.getTag();

				// mActivity.mFileCategory =
				// categoryDataList.get(position).mFileCategory;

				fileCategoryHelper.getInstance(mContext).setCurCategory(
						categoryDataList.get(position).mFileCategory);

				MyLog.i(TAG, "  getCurCategory="
						+ fileCategoryHelper.getInstance(mContext)
								.getCurCategory().ordinal());

				mActivity
						.fragmentSwtich(FileExplorerMainPagerActiviy.FileExplorerFragment);
				mActivity.headerView.headtitle.setText(categoryDataList
						.get(position).textid);
			}

		});

	}

	private void upateCategoryDataList() {

		categoryDataList.clear();
		Category categorymusic = new Category();
		categorymusic.mFileCategory = FileCategory.Music;
		categorymusic.imgid = Category.MUSIC_ImgID;
		categorymusic.img_focus_id = Category.MUSIC_ImgFocusID;
		categorymusic.textid = Category.MUSIC_TextID;
		categorymusic.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Music) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Music).count : 0;
		categoryDataList.add(categorymusic);

		Category categoryvideo = new Category();
		categoryvideo.mFileCategory = FileCategory.Video;
		categoryvideo.imgid = Category.VIDEO_ImgID;
		categoryvideo.img_focus_id = Category.VIDEO_ImgFocusID;
		categoryvideo.textid = Category.VIDEO_TextID;
		categoryvideo.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Video) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Video).count : 0;
		categoryDataList.add(categoryvideo);

		Category categorypic = new Category();
		categorypic.mFileCategory = FileCategory.Picture;
		categorypic.imgid = Category.PICTURE_ImgID;
		categorypic.img_focus_id = Category.PICTURE_ImgFocusID;
		categorypic.textid = Category.PICTURE_TextID;
		categorypic.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Picture) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Picture).count : 0;
		categoryDataList.add(categorypic);

		Category categorydoc = new Category();
		categorydoc.mFileCategory = FileCategory.Doc;
		categorydoc.imgid = Category.DOC_ImgID;
		categorydoc.img_focus_id = Category.DOC_ImgID;
		categorydoc.textid = Category.DOC_TextID;
		categorydoc.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Doc) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Doc).count : 0;
		categoryDataList.add(categorydoc);

		Category categoryzip = new Category();
		categoryzip.mFileCategory = FileCategory.Zip;
		categoryzip.imgid = Category.ZIP_ImgID;
		categoryzip.img_focus_id = Category.ZIP_ImgFocusID;

		categoryzip.textid = Category.ZIP_TextID;
		categoryzip.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Zip) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Zip).count : 0;
		categoryDataList.add(categoryzip);

		Category categoryapk = new Category();
		categoryapk.mFileCategory = FileCategory.Apk;
		categoryapk.imgid = Category.APK_ImgID;
		categoryapk.img_focus_id = Category.APK_ImgFocusID;

		categoryapk.textid = Category.APK_TextID;
		categoryapk.numtextid = fileCategoryHelper.getCategoryInfos().get(
				FileCategory.Apk) != null ? fileCategoryHelper
				.getCategoryInfos().get(FileCategory.Apk).count : 0;
		categoryDataList.add(categoryapk);

	}

	private void initOtherEntryview() {

		ButtonEntryView allfileEntry = new ButtonEntryView(mContext);
		allfileEntry.title.setText(R.string.category_allfiles);

		SDCardInfo sdCardInfo = Util.getSDCardInfoFromPaths(mActivity,
				mActivity.mMountPointManager.getMountPointPaths());
		if (sdCardInfo != null) {

			// getString(R.string.sd_card_size,
			// Util.convertStorage(sdCardInfo.total))
			allfileEntry.storageshow.setText(Util
					.convertStorage(sdCardInfo.free)
					+ getString(R.string.available));
		} else {
			allfileEntry.storageshow.setText("");
		}
		allfileEntry.topline.setVisibility(View.VISIBLE);
		allfileEntry.setOnclickListener(new ButtonEntryView.onclickListener() {
			@Override
			public void onClick(View v) {

				MyLog.i(TAG, "onClick");
				mActivity
						.fragmentSwtich(FileExplorerMainPagerActiviy.ALLFileFragment);
				mActivity.SetHeadViewMode(FileManagerHeaderView.FILE_NOMAL);
				mActivity.headerView.headtitle
						.setText(R.string.category_allfiles);
			}
		});
		otherentry_linearlayout.addView(allfileEntry);

		ButtonEntryView remoteManagerEntry = new ButtonEntryView(mContext);
		remoteManagerEntry.title.setText(R.string.tab_remote);
		remoteManagerEntry.storageshow.setText("  ");
		remoteManagerEntry
				.setOnclickListener(new ButtonEntryView.onclickListener() {
					@Override
					public void onClick(View v) {
						MyLog.i(TAG, "onClick");
						mActivity
								.fragmentSwtich(FileExplorerMainPagerActiviy.RemoteManagerFragment);
						mActivity
								.SetHeadViewMode(FileManagerHeaderView.RETURN_MODE);
						mActivity.headerView.headtitle
								.setText(R.string.tab_remote);
					}
				});
		otherentry_linearlayout.addView(remoteManagerEntry);
	}
}
