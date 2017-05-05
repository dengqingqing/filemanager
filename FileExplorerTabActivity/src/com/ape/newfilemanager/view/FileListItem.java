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

package com.ape.newfilemanager.view;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.filemanager.FileExplorerTabActivity;
import com.ape.filemanager.R;
import com.ape.newfilemanager.helper.FileIconHelper;
import com.ape.newfilemanager.helper.FileViewInteractionHub;
import com.ape.newfilemanager.helper.FileViewInteractionHub.Mode;
import com.ape.newfilemanager.helper.Util;
import com.ape.newfilemanager.model.FileInfo;
import com.ape.utils.MyLog;

public class FileListItem {
    private static final float DEFAULT_ICON_ALPHA = 1f;
    private static final float HIDE_ICON_ALPHA = 0.3f;

    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon,
            FileViewInteractionHub fileViewInteractionHub) {

        // if in moving mode, show selected file always
//        if (fileViewInteractionHub != null && fileViewInteractionHub.isMoveState()) {
//            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
//        }
        if (fileViewInteractionHub != null) {
            //long running operation.
            fileInfo.Selected = fileViewInteractionHub.isCheckedFile(fileInfo.filePath);
        }

        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        
        if (fileViewInteractionHub == null || fileViewInteractionHub.getMode() == Mode.View) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
            checkbox.setImageResource(fileInfo.Selected ? R.drawable.new_select_icon_focus
                    : R.drawable.new_select_icon_unfocus);
            
            view.setSelected(fileInfo.Selected);
        }
        checkbox.setTag(fileInfo);

        boolean hasDisplayName = !TextUtils.isEmpty(fileInfo.displayName);
        if (hasDisplayName) {
            Util.setText(view, R.id.file_name, fileInfo.displayName);
            view.findViewById(R.id.file_count).setVisibility(View.GONE);

            view.findViewById(R.id.modified_time).setVisibility(View.GONE);
            view.findViewById(R.id.storage_status).setVisibility(View.VISIBLE);
            Util.setText(view, R.id.free_space, context.getString(R.string.sd_card_available, Util.convertStorage(fileInfo.freeSpace)));
            Util.setText(view, R.id.total_space, context.getString(R.string.sd_card_size, Util.convertStorage(fileInfo.totalSpace)));
            Util.setText(view, R.id.file_size, "");

            view.findViewById(R.id.file_image_frame).setVisibility(View.GONE);
            ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
            lFileImage.setImageResource(R.drawable.new_fold_icon);
            lFileImage.setAlpha((fileInfo.isHidden) ? HIDE_ICON_ALPHA : DEFAULT_ICON_ALPHA);
        } else {
            Util.setText(view, R.id.file_name, fileInfo.fileName);

            TextView ctView = (TextView) view.findViewById(R.id.file_count);
            ctView.setVisibility(View.VISIBLE);
            String countString = "";
            if (fileInfo.IsDir) {
                countString = "(" + fileInfo.Count + ")";
            }
            ctView.setText(countString);

            if (fileInfo.ModifiedDate > 0) {
                Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfo.ModifiedDate));
                view.findViewById(R.id.modified_time).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.modified_time).setVisibility(View.GONE);
            }
            view.findViewById(R.id.storage_status).setVisibility(View.GONE);
            
            Util.setText(view, R.id.file_size, (fileInfo.IsDir ? "" : Util.convertStorage(fileInfo.fileSize)));

            ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
            ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);

            lFileImage.setAlpha((fileInfo.isHidden) ? HIDE_ICON_ALPHA : DEFAULT_ICON_ALPHA);
            if (fileInfo.IsDir) {
                fileIcon.cancelLoadFileIcon(fileInfo, lFileImage);
                lFileImageFrame.setVisibility(View.GONE);
                lFileImage.setImageResource(R.drawable.new_fold_icon);
            } else {
                fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame);
            }
        }
    }

    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
        	
        	MyLog.i(" dqq "," FileItemOnClickListener  onClick");
        	
            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            if(tag != null){
            	 tag.Selected = !tag.Selected;

                 if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                     img.setImageResource(tag.Selected ? R.drawable.new_select_icon_focus
                             : R.drawable.new_select_icon_unfocus);
                 } else {
                     tag.Selected = !tag.Selected;
                 }
            }    
           
        }
    }

    
}
