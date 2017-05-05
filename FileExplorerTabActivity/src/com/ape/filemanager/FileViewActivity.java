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

package com.ape.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
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

import com.ape.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.ape.filemanager.FileViewInteractionHub.Mode;

public class FileViewActivity extends Fragment implements
        IFileInteractionListener, IBackPressedListener {

    public static final String EXT_FILTER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileViewActivity";

    public static final String EXT_FILE_FIRST_KEY = "ext_file_first";

    public static final String ROOT_DIRECTORY = "root_directory";

    public static final String PICK_FOLDER = "pick_folder";

    private ListView mFileListView;

    // private TextView mCurrentPathTextView;
    private ArrayAdapter<FileInfo> mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();

    private Activity mActivity;

    private View mRootView;
    private LinearLayout mSearchView;

    private static final String sdDir = Util.getSdDirectory();
    
    private MountPointManager mMountPointManager;

    // memorize the scroll positions of previous paths
    private ArrayList<PathScrollPositionItem> mScrollPositionList = new ArrayList<PathScrollPositionItem>();
    private String mPreviousPath;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast:" + intent.toString());
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                
                if (mMountPointManager != null)
                {
                    mMountPointManager.init(mActivity);
                }
                if (mFileViewInteractionHub != null && action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
                {
                    Uri path = intent.getData();
                    String ummountPath = path.getPath();
                    if (mFileViewInteractionHub.getCurrentPath().startsWith(ummountPath))
                    {
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

    private boolean mBackspaceExit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mMountPointManager = ((FileExplorerTabActivity) mActivity).getMountManager();
        // getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mRootView = inflater.inflate(R.layout.file_explorer_list, container, false);
        ActivitiesManager.getInstance().registerActivity(ActivitiesManager.ACTIVITY_FILE_VIEW, mActivity);

        mFileCagetoryHelper = new FileCategoryHelper(mActivity);
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)
                && (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)
                        || action.equals("com.mediatek.filemanager.ADD_FILE"))) {
            mFileViewInteractionHub.setMode(Mode.Pick);

            boolean pickFolder = intent.getBooleanExtra(PICK_FOLDER, false);
            if (!pickFolder) {
                String[] exts = intent.getStringArrayExtra(EXT_FILTER_KEY);
                if (exts != null) {
                    mFileCagetoryHelper.setCustomCategory(exts);
                }
            } else {
                mFileCagetoryHelper.setCustomCategory(new String[]{} /*folder only*/);
                mRootView.findViewById(R.id.pick_operation_bar).setVisibility(View.VISIBLE);

                mRootView.findViewById(R.id.button_pick_confirm).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            Intent intent = Intent.parseUri(mFileViewInteractionHub.getCurrentPath(), 0);
                            mActivity.setResult(Activity.RESULT_OK, intent);
                            mActivity.finish();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });

                mRootView.findViewById(R.id.button_pick_cancel).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        mActivity.finish();
                    }
                });
            }
        } else {
            mFileViewInteractionHub.setMode(Mode.View);
        }

        mFileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
        mFileIconHelper = new FileIconHelper(mActivity);
        mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item, mFileNameList, mFileViewInteractionHub,
                mFileIconHelper);

        boolean baseSd = intent.getBooleanExtra(GlobalConsts.KEY_BASE_SD, !FileExplorerPreferenceActivity.isReadRoot(mActivity));
        Log.i(LOG_TAG, "baseSd = " + baseSd);

        String rootDir = intent.getStringExtra(ROOT_DIRECTORY);
        if (!TextUtils.isEmpty(rootDir)) {
            if (baseSd && this.sdDir.startsWith(rootDir)) {
                rootDir = this.sdDir;
            }
        } else {
            rootDir = baseSd ? this.sdDir : Util.getTinnoRootPath(); //GlobalConsts.ROOT_PATH;
        }
        mFileViewInteractionHub.setRootPath(rootDir);

        String currentDir = FileExplorerPreferenceActivity.getPrimaryFolder(mActivity);
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

        mBackspaceExit = (uri != null)
                && (TextUtils.isEmpty(action)
                || (!action.equals(Intent.ACTION_PICK) && !action.equals(Intent.ACTION_GET_CONTENT)));

        mFileListView.setAdapter(mAdapter);
        //mFileViewInteractionHub.refreshFileList();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mReceiver, intentFilter);

        setSearchUI();

        updateUI();
        setHasOptionsMenu(true);

        return mRootView;
    }
    
    private void setSearchUI()
    {
        mSearchView = (LinearLayout) mRootView.findViewById(R.id.search_view_frame);
        mSearchView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getId() == R.id.search_view_frame
                        && Util.isSDCardReady())
                {
                    FileExplorerTabActivity activity = (FileExplorerTabActivity) getActivity();
                    activity.startSearchActivity(mFileViewInteractionHub.getCurrentPath());
                }
            }
        });
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        if(mFileViewInteractionHub != null)
        {
            mFileViewInteractionHub.refreshCheckFileList();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu == null)
            return;

        mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onBack() {
        if (mBackspaceExit || !Util.isSDCardReady() || mFileViewInteractionHub == null) {
            return false;
        }
        return mFileViewInteractionHub.onBackPressed();
    }

    private class PathScrollPositionItem {
        String path;
        int pos;
        int top;

        PathScrollPositionItem(String s, int p, int t) {
            path = s;
            pos = p;
            top = t;
        }
    }

    private int mListPos = 0;
    private int mListTop = 0;

    // execute before change, return the memorized scroll position
    private int computeScrollPosition(String path) {
        mListPos = mListTop = 0;
        if(mPreviousPath!=null) {
            if (path.startsWith(mPreviousPath)) {
                int firstVisiblePos = mFileListView.getFirstVisiblePosition();
                int firstVisibleTop = 0;
                View view = mFileListView.getChildAt(0);
                if (view != null) {
                    firstVisibleTop = view.getTop();
                }

                if (mScrollPositionList.size() != 0
                        && mPreviousPath.equals(mScrollPositionList.get(mScrollPositionList.size() - 1).path)) {
                    mListPos = firstVisiblePos;
                    mListTop = firstVisibleTop;
                    mScrollPositionList.get(mScrollPositionList.size() - 1).pos = mListPos;
                    mScrollPositionList.get(mScrollPositionList.size() - 1).top = mListTop;
                    Log.i(LOG_TAG, "computeScrollPosition: update item: " + mPreviousPath + " " + firstVisiblePos);
                } else if (mPreviousPath.equals(path)) { // for return from other application.
                    mListPos = firstVisiblePos;
                    mListTop = firstVisibleTop;
                    mScrollPositionList.add(new PathScrollPositionItem(mPreviousPath, mListPos, mListTop));
                } else {
                    mScrollPositionList.add(new PathScrollPositionItem(mPreviousPath, firstVisiblePos, firstVisibleTop));
                    Log.i(LOG_TAG, "computeScrollPosition: add item: " + mPreviousPath + " " + firstVisiblePos + " " + firstVisibleTop);
                }
            } else {
                int i;
                boolean isLast = false;
                for (i = 0; i < mScrollPositionList.size(); i++) {
                    if (!path.startsWith(mScrollPositionList.get(i).path)) {
                        break;
                    }
                }
                // navigate to a totally new branch, not in current stack
                if (i > 0) {
                    mListPos = mScrollPositionList.get(i - 1).pos;
                    mListTop = mScrollPositionList.get(i - 1).top;
                }

                for (int j = mScrollPositionList.size() - 1; j >= i-1 && j>=0; j--) {
                    mScrollPositionList.remove(j);
                }
            }
        }

        Log.i(LOG_TAG, "computeScrollPosition: result path:" + path + ", pos:" + mListPos + ", top:" + mListTop);
        mPreviousPath = path;

        return mListPos;
    }
    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        
        if (!mFileViewInteractionHub.isInProgress() && !mIsLoading)
        {
            ListFilesTask listTask = new ListFilesTask(path, sort);
            listTask.execute();
        }

        return true;
    }
    
    private boolean mIsLoading = false;
    class ListFilesTask extends AsyncTask<Void, Object, Object>
    {
        private boolean needInvalidOptionMenu;
        private String mListPath;
        private FileSortHelper mListSort;
        Runnable mDelayDisplay;
        ProgressDialog progressDialog;
        ArrayList<FileInfo> mListFiles;

        public ListFilesTask(String path, FileSortHelper sort)
        {
            needInvalidOptionMenu = mFileViewInteractionHub.needInvalidOptionMenu();
            mIsLoading = true;
            mListPath = path;
            mListSort = sort;
            progressDialog = null;
            mDelayDisplay = new Runnable()
            {
                @Override
                public void run()
                {
                    if (mIsLoading)
                    {
                        progressDialog = new ProgressDialog(mActivity);
                        progressDialog.setMessage(mActivity.getString(R.string.files_loading));
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                }
            };
        }

        @Override
        protected void onPreExecute()
        {
            mFileListView.postDelayed(mDelayDisplay, 500);
        }

        @Override
        protected void onPostExecute(Object result)
        {
            if (mListFiles != null)
            {
                mFileNameList.clear();
                mFileNameList.addAll(mListFiles);
                mListFiles.clear();
                mListFiles = null;
            }

            if (progressDialog != null)
            {
                progressDialog.dismiss();
                progressDialog = null;
            }
            mFileListView.removeCallbacks(mDelayDisplay);

            mAdapter.notifyDataSetChanged();
            if (Util.SDK_VERSION >= 19) {
                mFileListView.setSelectionFromTop(mListPos, mListTop);
            } else {
                mFileListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mFileListView.setSelectionFromTop(mListPos, mListTop);
                    }
                });
            }
            showEmptyView(mFileNameList.size() == 0 && Util.isSDCardReady());

            if (needInvalidOptionMenu)
            {
                mActivity.invalidateOptionsMenu();
            }

            mIsLoading = false;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object doInBackground(Void... params)
        {
            File file = new File(mListPath);
            computeScrollPosition(mListPath);
            mListFiles = new ArrayList<FileInfo>();
            mListFiles.clear();

            if (Util.getTinnoRootPath().equals(mListPath))
            {
                mListFiles.addAll(mMountPointManager.getMountPointFileInfo());
            } else 
            {
                FilenameFilter filter = mFileCagetoryHelper.getFilter();
                File[] listFiles = file.listFiles(filter);
                if (listFiles == null)
                    return true;
        
                boolean isOnlyMoveState = mFileViewInteractionHub.isOnlyMoveState();
                boolean isShowHidden = Settings.instance().getShowDotAndHiddenFiles();
                for (File child : listFiles) {
                    // do not show selected file if in move state
                    if (isOnlyMoveState && mFileViewInteractionHub.isFileSelected(child.getPath()))
                        continue;
        
                    String absolutePath = child.getAbsolutePath();
                    if (Util.isNormalFile(absolutePath) && Util.shouldShowFile(absolutePath)) {
                        FileInfo lFileInfo = Util.GetFileInfo(child, filter, isShowHidden);
        
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

        View navigationBar = mRootView.findViewById(R.id.navigation_bar);
        navigationBar.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
        mFileListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

        if(sdCardReady) {
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

    @Override
    public Context getContext() {
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
            Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath)).toString(), 0);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        return false;
    }

    //支持显示真实路径
    @Override
    public String getDisplayPath(String path) {
        if (path.startsWith(this.sdDir) && !FileExplorerPreferenceActivity.showRealPath(mActivity)) {
            return getString(R.string.sd_folder) + path.substring(this.sdDir.length());
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

    @Override
    public boolean onNavigation(String path) {
        return false;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return false;
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

    public interface SelectFilesCallback {
        // files equals null indicates canceled
        void selected(ArrayList<FileInfo> files);
    }

    public void startSelectFiles(SelectFilesCallback callback) {
        if (mFileViewInteractionHub != null) {
            mFileViewInteractionHub.startSelectFiles(callback);
        }
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    public boolean setPath(String location) {
//        if (!location.startsWith(mFileViewInteractionHub.getRootPath())) {
//            return false;
//        }
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
        //mFileViewInteractionHub.refreshCheckFileList();
    }

    @Override
    public int getItemCount() {
        return mFileNameList.size();
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    } 
}
