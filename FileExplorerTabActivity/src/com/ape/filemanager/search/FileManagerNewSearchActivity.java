package com.ape.filemanager.search;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.FileExplorerTabActivity;
import com.ape.filemanager.FileIconHelper;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.FileListItem;
import com.ape.filemanager.IntentBuilder;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.ape.newfilemanager.view.CircleProgressDialog;
import com.ape.newfilemanager.view.FileManagerHeaderView;
import com.ape.newfilemanager.view.FileManagerHeaderView.onClickListenr;
import com.ape.utils.MyLog;

public class FileManagerNewSearchActivity extends Activity implements
		OnItemClickListener {
	private static final String TAG = "FileManagerNewSearchActivity";
	private TextView mResultView = null;
	private String mSearchPath = null;
	private SearchView mSearchView = null;
	private ImageView mSearchBg = null;

	private Context mContext;
	private FileManagerHeaderView head_view;

	// private MenuItem mSearchItem;
	private long mTotal = 0;
	private long mSearchTotal = 0;
	private String mSearchText = null;
	private boolean mIsFromFileManger;

	public static final String CURRENT_PATH = "current_path";
	public static final String SEARCH_TEXT = "search_text";
	public static final String SEARCH_TOTAL = "search_total";
	public static final String IS_FROM_FILEMANAGER = "is_from_fileManger";

	private ListView mListView;
	private SearchListAdapter mAdapter;
	private ArrayList<FileInfo> mFileInfoList;
	private FileIconHelper mFileIconHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_new_main);

		mContext = this;
		initView();

		parseIntent();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mResultView = (TextView) findViewById(R.id.search_result);
		mSearchBg = (ImageView) findViewById(R.id.search_bg);
	
		mSearchView = (SearchView) findViewById(R.id.searchview);

		mSearchView.setIconifiedByDefault(true);

		mSearchView.setSubmitButtonEnabled(true);
		mSearchView.onActionViewExpanded();
		mSearchView.setIconifiedByDefault(true);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null)
        {
            mSearchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
        }

		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				MyLog.i(TAG, " onQueryTextSubmit  query="+query);
				if (!TextUtils.isEmpty(mSearchText)) {
					
					MyLog.i(TAG, " onQueryTextSubmit");
					mSearchView.setQuery(mSearchText, false);
					mSearchView.clearFocus();
					requestSearch(query);				

				}
				
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				MyLog.i(TAG, " onQueryTextChange  newText="+newText);
				
				
				return false;
			}
		});

		head_view = (FileManagerHeaderView) findViewById(R.id.head_view);
		head_view.setHeadMode(FileManagerHeaderView.RETURN_MODE);
		head_view.headtitle.setText(R.string.category_allfiles);

		head_view.setOnClickListener(new onClickListenr() {

			@Override
			public void HeadViewSelectAllOnclick(View v) {
				// TODO Auto-generated method stub

			}

			@Override
			public void HeadViewReturnOnclick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

			@Override
			public void HeadViewEditOnclick(View v) {
				// TODO Auto-generated method stub

			}

			@Override
			public void HeadViewCancelOnclick(View v) {
				// TODO Auto-generated method stub

			}
		});

		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setOnItemClickListener(this);
		mFileInfoList = new ArrayList<FileInfo>();
		mFileIconHelper = new FileIconHelper(this);
		mAdapter = new SearchListAdapter(this, R.layout.file_browser_item,
				mFileInfoList, mFileIconHelper);
		mListView.setAdapter(mAdapter);
	}

	private void parseIntent() {
		Intent intent = getIntent();

		mIsFromFileManger = getIntent().getBooleanExtra(IS_FROM_FILEMANAGER,
				false);
		mSearchPath = intent.getStringExtra(CURRENT_PATH);
		if (mSearchPath == null || Util.getTinnoRootPath().equals(mSearchPath)) {
			mSearchPath = "/";
		}

		handleIntent(intent);
		MyLog.i(TAG, "onCreate, intent:" + intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mSearchText != null) {
			outState.putString(SEARCH_TEXT, mSearchText);
			outState.putLong(SEARCH_TOTAL, mTotal);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState != null && mResultView != null) {
			mSearchText = savedInstanceState.getString(SEARCH_TEXT);
			if (!TextUtils.isEmpty(mSearchText)) {
				mTotal = savedInstanceState.getLong(SEARCH_TOTAL);
				mResultView.setVisibility(View.VISIBLE);
				mResultView.setText(getResources().getString(
						R.string.search_result, mSearchText, mTotal));
			}
		}

		Intent intent = getIntent();
		handleIntent(intent);
	}

	/**
	 * The method handles received intent.
	 * 
	 * @param intent
	 *            the intent FileManagerSearchActivity received.
	 */
	private void handleIntent(Intent intent) {

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String path = null;
			if (intent.getData() != null) {
				path = intent.getData().toString();
			}
			if (TextUtils.isEmpty(path)) {
				Log.w(TAG, "handleIntent intent uri path == null");
				return;
			}
			onItemClick(Util.GetFileInfo(path));
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			requestSearch(query);
		}
	}

	/**
	 * The method start search task.
	 * 
	 * @param query
	 *            the search target.
	 */
	private void requestSearch(String query) {
		MyLog.i(TAG, "requestSearch, query:" + query);
	
		if (query != null && !query.isEmpty()) {
			
			mResultView.setText(R.string.file_search_status);
			SearchTask task = new SearchTask(new SearchListener(query), query,
					mSearchPath, getContentResolver());
			task.execute();
			if (mSearchView != null) {
				mSearchView.setQuery(query, false);
				mSearchView.clearFocus();
			}
		} else {
			Toast.makeText(this, R.string.search_text_empty, Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected class SearchListener implements OperationEventListener {
		private static final int FRIST_UPDATE_COUNT = 20;
		private static final int NEED_UPDATE_LIST = 6;
		private boolean mIsResultSet = false;
		private int mCount = 0;

		/**
		 * Constructor of SearchListener.
		 * 
		 * @param text
		 *            the search target(String), which will be shown on
		 *            searchResult TextView..
		 */
		public SearchListener(String text) {
			if (text == null) {
				throw new IllegalArgumentException();
			}
			mSearchText = text;
		}

		@Override
		public void onTaskResult(int result) {
			MyLog.i(TAG, "onTaskResult, size:" + mFileInfoList.size());
			mAdapter.notifyDataSetChanged();
			updateSearchResult();
		}

		@Override
		public void onTaskPrepare() {
			mAdapter.clear();
			// mAdapter.changeMode(FileInfoAdapter.MODE_SEARCH);
		}

		@Override
		public void onTaskProgress(ProgressInfo progressInfo) {
			if (!progressInfo.isFailInfo()) {
				if (mResultView != null && !mIsResultSet) {
					mSearchTotal = progressInfo.getTotal();
					mIsResultSet = true;
				}

				FileInfo fileInfo = progressInfo.getFileInfo();
				if (fileInfo != null) {
					mAdapter.add(fileInfo);
				}
				mCount++;
				if (mCount > FRIST_UPDATE_COUNT) {
					if (mListView.getLastVisiblePosition() + NEED_UPDATE_LIST > mAdapter
							.getCount()) {
						mAdapter.notifyDataSetChanged();
						updateSearchResult();
						mCount = 0;
					}
				}
			}
		}

		private void updateSearchResult() {
			
			mTotal = mAdapter.getCount();
			mResultView.setVisibility(View.VISIBLE);
			mResultView.setText(getResources().getString(
					R.string.search_result, mSearchText, mTotal));
			mSearchBg.setVisibility(View.GONE);
		}
	}

	public class SearchListAdapter extends ArrayAdapter<FileInfo> {
		private LayoutInflater mInflater;
		private FileIconHelper mFileIcon;
		private Context mContext;
		private int mLayout;
		List<FileInfo> mListData;

		public SearchListAdapter(Context context, int resource,
				List<FileInfo> fileInfoList, FileIconHelper fileIcon) {
			super(context, resource, fileInfoList);
			mInflater = LayoutInflater.from(context);
			mFileIcon = fileIcon;
			mContext = context;
			mLayout = resource;
			mListData = fileInfoList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView != null) {
				view = convertView;
			} else {
				view = mInflater.inflate(mLayout, parent, false);
			}

			FileInfo lFileInfo = mListData.get(position);
			FileListItem.setupFileListItemInfo(mContext, view, lFileInfo,
					mFileIcon, null);
			return view;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (position >= mAdapter.getCount() || position < 0) {
			MyLog.e(TAG, "click events error");
			MyLog.e(TAG, "mFileInfoList.size(): " + mAdapter.getCount());
			return;
		}
		FileInfo selectedFileInfo = (FileInfo) mAdapter.getItem(position);
		onItemClick(selectedFileInfo);
	}

	/**
	 * The method deal with the event that a certain childView of listView in
	 * searchActivity is clicked.
	 * 
	 * @param selectedFileInfo
	 *            The FileInfo associate with the selected childView of
	 *            listView.
	 */
	private void onItemClick(FileInfo selectedFileInfo) {
		if (selectedFileInfo == null)
			return;

		if (selectedFileInfo.IsDir) {
			if (!mIsFromFileManger) {
				Intent intent = new Intent(this, FileExplorerTabActivity.class);
				intent.setData(Uri.parse("file://" + selectedFileInfo.filePath));
				startActivity(intent);
			} else {
				FileExplorerTabActivity.mIsFromSearch = true;
				FileExplorerTabActivity.mSearchPath = selectedFileInfo.filePath;
			}
		} else {
			try {
				IntentBuilder.viewFile(this, selectedFileInfo.filePath, false);
			} catch (ActivityNotFoundException e) {
				MyLog.e(TAG, "fail to view file: " + e.toString());
			}
		}
		finish();
	}
}
