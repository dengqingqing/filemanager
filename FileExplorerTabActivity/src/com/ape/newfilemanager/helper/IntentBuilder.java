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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ape.filemanager.MimeUtils;
import com.ape.filemanager.R;
import com.ape.newfilemanager.model.FileInfo;

import java.io.File;
import java.util.ArrayList;

public class IntentBuilder {

    private static final String TAG = "IntentBuilder";

    public static void viewFile(final Context context, final String filePath, boolean needUnknowSelect) {
        String type = getMimeType(filePath);

        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            if (type.equals("video/3gpp") || type.equals("audio/3gpp")) {
                String type2 = loadMimetypeFromDB(filePath, context);
                if (!TextUtils.isEmpty(type2)) {
                    type = type2;
                }
            }
            /* 设置intent的file与MimeType */
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (type.equals("application/vnd.android.package-archive"))
            {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            try
            {
                context.startActivity(intent);
            } catch (Exception e)
            {
                AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(R.string.msg_unable_open_file)
                .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
            }
        } else if (needUnknowSelect) {
            // unknown MimeType
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.dialog_select_type);

            CharSequence[] menuItemArray = new CharSequence[] {
                    context.getString(R.string.dialog_type_text),
                    context.getString(R.string.dialog_type_audio),
                    context.getString(R.string.dialog_type_video),
                    context.getString(R.string.dialog_type_image) };
            dialogBuilder.setItems(menuItemArray,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selectType = "*/*";
                            switch (which) {
                            case 0:
                                selectType = "text/plain";
                                break;
                            case 1:
                                selectType = "audio/*";
                                break;
                            case 2:
                                selectType = "video/*";
                                break;
                            case 3:
                                selectType = "image/*";
                                break;
                            }
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
                            context.startActivity(intent);
                        }
                    });
            dialogBuilder.show();
        } else {
            Toast.makeText(context, R.string.msg_unable_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent buildSendFile(ArrayList<FileInfo> files, Context context) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.IsDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = getMimeType(file.fileName);
            Uri u = tryContentMediaUri(context, file.filePath); //Uri.fromFile(fileIn);
            if (u == null)
                u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            if (mimeType.startsWith("application") && !mimeType.equals("application/ogg"))
            {
                mimeType = "application/zip";
            }
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
    
    /**
     * some file could be video type or audio type. The method try to find out its real MIME type
     * from database of MediaStore.
     * 
     * @param filePath path of a file
     * @return the file's real MIME type
     */
    public static String loadMimetypeFromDB(String filePath, Context context) {
        String mimeType = null;
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null && filePath != null) {
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[] { MediaStore.MediaColumns.MIME_TYPE };
            final String selection = MediaStore.MediaColumns.DATA + "=?";
            final String[] selectionArgs = new String[] { filePath };
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection,
                        selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                     mimeType = cursor.getString(cursor
                            .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return mimeType;
    }

     
    // permission check.
/*    public boolean selfPermissionGranted(Context context, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } 

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = context.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }*/
    
    
    public static Uri tryContentMediaUri(Context context, String path) {
        if (path == null) {
            return null;
        }
        Cursor cursor = null;
        Uri uri = null;
        String mimeType = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"),
                    new String[] { FileColumns._ID, FileColumns.MEDIA_TYPE, FileColumns.MIME_TYPE },
                    FileColumns.DATA + "=?",
                    new String[] { path },
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(FileColumns._ID));
                int mediaType = cursor.getInt(cursor.getColumnIndex(FileColumns.MEDIA_TYPE));
                mimeType = cursor.getString(cursor.getColumnIndex(FileColumns.MIME_TYPE));
                if (mediaType == FileColumns.MEDIA_TYPE_IMAGE) {
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                } else if (mediaType == FileColumns.MEDIA_TYPE_VIDEO) {
                    uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                } else if (mediaType == FileColumns.MEDIA_TYPE_AUDIO) {
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    
         
                	
                    
                } else {
                    // do-nothing
                    mimeType = null;
                    Log.d(TAG, "tryContentMediaUri ,the file is not a media file.");
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
//        if (mimeType != null) {
//            intent.setType(mimeType);
//        }
//        if (uri != null) {
//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//        }
        Log.d(TAG, "tryContentMediaUri with real mimeType = " + mimeType + ", uri = " + uri);
        return uri;
    }
}
