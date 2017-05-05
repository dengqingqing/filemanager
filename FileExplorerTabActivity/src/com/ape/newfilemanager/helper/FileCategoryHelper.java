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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.ape.filemanager.R;
import com.ape.newfilemanager.helper.FileSortHelper.SortMethod;
import com.ape.newfilemanager.helper.MediaFile.MediaFileType;
import com.ape.newfilemanager.observer.ProviderQueryDataChange;
import com.ape.utils.MyLog;

import java.io.FilenameFilter;
import java.util.HashMap;

public class FileCategoryHelper {

    private static FileCategoryHelper instance;

    public static final int COLUMN_ID = 0;

    public static final int COLUMN_PATH = 1;

    public static final int COLUMN_SIZE = 2;

    public static final int COLUMN_DATE = 3;

    private static final String LOG_TAG = "FileCategoryHelper";

    public enum FileCategory {
        All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite, app_MM
    }

    private static String APK_EXT = "apk";
    private static String THEME_EXT = "mtz";
    private static String[] ZIP_EXTS = new String[] {
            "zip", "rar"
    };

    public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();

    public static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();

    static {
        categoryNames.put(FileCategory.All, R.string.category_all);
        categoryNames.put(FileCategory.Music, R.string.category_music);
        categoryNames.put(FileCategory.Video, R.string.category_video);
        categoryNames.put(FileCategory.Picture, R.string.category_picture);
        categoryNames.put(FileCategory.Theme, R.string.category_theme);
        categoryNames.put(FileCategory.Doc, R.string.category_document);
        categoryNames.put(FileCategory.Zip, R.string.category_zip);
        categoryNames.put(FileCategory.Apk, R.string.category_apk);
        categoryNames.put(FileCategory.Other, R.string.category_other);
        categoryNames.put(FileCategory.Favorite, R.string.category_favorite);
    }

    public static FileCategory[] sCategories = new FileCategory[] {
            FileCategory.Music, FileCategory.Video, FileCategory.Picture,
            FileCategory.Theme, FileCategory.Doc, FileCategory.Zip,
            FileCategory.Apk, FileCategory.Other, FileCategory.app_MM
    };

    // need storage and
    private int mCategory;

    private Context mContext;

    private long mMultiMediaTotal;
    private long mMultiMediaSize;

    public static FileCategoryHelper getInstance(Context context) {

        if (instance == null) {
            synchronized (FileCategoryHelper.class) {
                if (instance == null) {
                    instance = new FileCategoryHelper(context);
                }
            }
        }

        return instance;
    }

    private FileCategoryHelper(Context context) {
        mContext = context;

        mCategory = FileCategory.All.ordinal();
    }

    public FileCategory getCurCategory() {

        return FileCategory.values()[mCategory];
    }

    public void setCurCategory(FileCategory c) {
        mCategory = c.ordinal();
    }

    public int getCurCategoryNameResId() {
        return categoryNames.get(mCategory);
    }

    public void setCustomCategory(String[] exts) {
        mCategory = FileCategory.Custom.ordinal();
        if (filters.containsKey(FileCategory.Custom)) {
            filters.remove(FileCategory.Custom);
        }

        filters.put(FileCategory.Custom, new FilenameExtFilter(exts));
    }

    public FilenameFilter getFilter() {
        return filters.get(FileCategory.values()[mCategory]);
    }

    private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

    public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
        return mCategoryInfo;
    }

    public CategoryInfo getCategoryInfo(FileCategory fc) {
        if (mCategoryInfo.containsKey(fc)) {
            return mCategoryInfo.get(fc);
        } else {
            CategoryInfo info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
            return info;
        }
    }

    public class CategoryInfo {
        public long count;

        public long size;
    }

    private void setCategoryInfo(FileCategory fc, long count, long size) {
        CategoryInfo info = mCategoryInfo.get(fc);
        if (info == null) {
            info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
        }
        info.count = count;
        info.size = size;
    }

    private String buildDocSelection() {
        StringBuilder selection = new StringBuilder();
        // Iterator<String> iter = Util.sDocMimeTypesSet.iterator();
        // while(iter.hasNext()) {
        // selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() +
        // "') OR ");
        // }
        for (String str : Util.DOC_SUPPORTED_SUFFIX) {
            selection.append("(" + FileColumns.DATA + " LIKE '%" + str
                    + "') OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    private String buildSelectionByCategory(FileCategory cat) {
        String selection = null;
        switch (cat) {
            case Theme:
                selection = FileColumns.DATA + " LIKE '%.mtz'";
                break;
            case Doc:
                selection = buildDocSelection();
                break;
            case Zip:
                selection = "(";
                for (String str : Util.SUPPORTED_ARCHIVES) {
                    selection = selection + "(" + FileColumns.DATA + " LIKE '%"
                            + str + "') OR ";
                }
                selection = selection.substring(0, selection.length() - 3);
                selection += ")";
                break;
            case Apk:
                selection = FileColumns.DATA + " LIKE '%.apk'";
                break;
            default:
                selection = null;
        }
        return selection;
    }

    private Uri getContentUriByCategory(FileCategory cat) {
        Uri uri;
        String volumeName = "external";
        switch (cat) {
            case Theme:
            case Doc:
            case Zip:
            case Apk:
                uri = Files.getContentUri(volumeName);
                break;
            case Music:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Video:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Picture:
                uri = Images.Media.getContentUri(volumeName);
                break;
            default:
                uri = null;
        }
        return uri;
    }

    private String buildSortOrder(SortMethod sort) {
        String sortOrder = null;
        switch (sort) {
            case name:
                sortOrder = FileColumns.TITLE + " asc";
                break;
            case size:
                sortOrder = FileColumns.SIZE + " desc";
                break;
            case date:
                sortOrder = FileColumns.DATE_MODIFIED + " desc";
                break;
            case type:
                sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE
                        + " asc";
                break;
        }
        return sortOrder;
    }

    public Cursor query(FileCategory fc, SortMethod sort) {
        Uri uri = getContentUriByCategory(fc);
        String selection = buildSelectionByCategory(fc);
        String sortOrder = buildSortOrder(sort);

        if (uri == null) {
            Log.e(LOG_TAG, "invalid uri, category:" + fc.name());
            return null;
        }

        String[] columns = new String[] {
                FileColumns._ID, FileColumns.DATA,
                FileColumns.SIZE, FileColumns.DATE_MODIFIED
        };

        return mContext.getContentResolver().query(uri, columns, selection,
                null, sortOrder);
    }

    private boolean refreshCategoryInfoing = true;
    
    public void setNeedrefreshCategoryInfo(){
        
        refreshCategoryInfoing = true;
    }

    public void refreshCategoryInfo() {

     
               // if(refreshCategoryInfoing == false)
                  //  return;
                
                refreshCategoryInfoing = false;
                // clear
                for (FileCategory fc : sCategories) {
                    setCategoryInfo(fc, 0, 0);
                }

                mMultiMediaTotal = 0;
                mMultiMediaSize = 0;

                // query database
                String volumeName = "external";

                Uri uri = Audio.Media.getContentUri(volumeName);
                refreshMediaCategory(FileCategory.Music, uri);

                uri = Video.Media.getContentUri(volumeName);
                refreshMediaCategory(FileCategory.Video, uri);

                uri = Images.Media.getContentUri(volumeName);
                refreshMediaCategory(FileCategory.Picture, uri);

                uri = Files.getContentUri(volumeName);
                refreshMediaCategory(FileCategory.Theme, uri);
                refreshMediaCategory(FileCategory.Doc, uri);
                refreshMediaCategory(FileCategory.Zip, uri);
                refreshMediaCategory(FileCategory.Apk, uri);

                setCategoryInfo(FileCategory.app_MM, mMultiMediaTotal,
                        mMultiMediaSize);
                
    }

    private boolean refreshMediaCategory(final FileCategory fc, final Uri uri) {
        
    
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                String[] columns = new String[] {
                        "COUNT(*)", "SUM(_size)"
                };
                Cursor c = mContext.getContentResolver().query(uri, columns,
                        buildSelectionByCategory(fc), null, null);
                if (c == null) {
                    Log.e(LOG_TAG, "fail to query uri:" + uri);
                    return;
                }

                if (c.moveToNext()) {
                    mMultiMediaTotal += c.getLong(0);
                    mMultiMediaSize += c.getLong(1);
                    setCategoryInfo(fc, c.getLong(0), c.getLong(1));
                    Log.v(LOG_TAG,
                            "Retrieved " + fc.name() + " info >>> count:"
                                    + c.getLong(0) + " size:" + c.getLong(1));
                    MyLog.i(LOG_TAG,
                            "Retrieved " + fc.name() + " info >>> count:"
                                    + c.getLong(0) + " size:" + c.getLong(1));
                    MyLog.i(LOG_TAG,
                            " countObservers=" + ProviderQueryDataChange.getInstance().countObservers());
                    c.close();

                    ProviderQueryDataChange.getInstance().notifydatachange();
                    //return true;
                } else {
                    c.close();
                }
                
            }
        }).start();        
        
        return true;
    }

    public static FileCategory getCategoryFromPath(String path) {
        if (path == null) {
            return FileCategory.Other;
        }

        MediaFileType type = MediaFile.getFileType(path);
        if (type != null) {
            if (MediaFile.isAudioFileType(type.fileType))
                return FileCategory.Music;
            if (MediaFile.isVideoFileType(type.fileType))
                return FileCategory.Video;
            if (MediaFile.isImageFileType(type.fileType))
                return FileCategory.Picture;
            if (Util.sDocMimeTypesSet.contains(type.mimeType))
                return FileCategory.Doc;
        }

        int dotPosition = path.lastIndexOf('.');
        if (dotPosition < 0) {
            return FileCategory.Other;
        }

        String ext = path.substring(dotPosition + 1);
        if (ext.equalsIgnoreCase(APK_EXT)) {
            return FileCategory.Apk;
        }
        if (ext.equalsIgnoreCase(THEME_EXT)) {
            return FileCategory.Theme;
        }

        if (matchExts(ext, ZIP_EXTS)) {
            return FileCategory.Zip;
        }

        return FileCategory.Other;
    }

    private static boolean matchExts(String ext, String[] exts) {
        for (String ex : exts) {
            if (ex.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }
}
