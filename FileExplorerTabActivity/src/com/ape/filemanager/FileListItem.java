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

import com.ape.filemanager.R;
import com.ape.filemanager.FileViewInteractionHub.Mode;

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
            fileInfo.Selected = fileViewInteractionHub.isCheckedFile(fileInfo.filePath);
        }

        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        if (fileViewInteractionHub == null || fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
            checkbox.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
            view.setSelected(fileInfo.Selected);
        }

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
            lFileImage.setImageResource(R.drawable.folder);
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
                lFileImage.setImageResource(R.drawable.folder);
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
            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            tag.Selected = !tag.Selected;

            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                img.setImageResource(tag.Selected ? R.drawable.btn_check_on_holo_light
                        : R.drawable.btn_check_off_holo_light);
            } else {
                tag.Selected = !tag.Selected;
            }

            ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileExplorerTabActivity) mContext)
                        .startActionMode(new ModeCallback(mContext,
                                mFileViewInteractionHub));
                ((FileExplorerTabActivity) mContext).setActionMode(actionMode);
                mFileViewInteractionHub.afterChangedToActionMode(tag);
            } else {
                actionMode.invalidate();
            }

            Util.updateActionModeTitle(actionMode, mContext,
                    mFileViewInteractionHub.getSelectedFileList().size());
        }
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        private void initMenuItemSelectAllOrCancel() {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
            mMenu.findItem(R.id.action_copy_path).setVisible(false);
        }

        private void scrollToSDcardTab() {
            ActionBar bar = ((FileExplorerTabActivity) mContext).getActionBar();
            if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            }
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.operation_menu, mMenu);
            initMenuItemSelectAllOrCancel();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
//            mMenu.findItem(R.id.action_copy_path).setVisible(
//                    mFileViewInteractionHub.getSelectedFileList().size() == 1);
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mFileViewInteractionHub.onOperationDelete();
                    mode.finish();
                    break;
                case R.id.action_copy:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_move:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_send:
                    mFileViewInteractionHub.onOperationSend();
                    //mode.finish(); //STGOO-650
                    break;
                case R.id.action_copy_path:
                    mFileViewInteractionHub.onOperationCopyPath();
                    mode.finish();
                    break;
                case R.id.action_cancel:
                    mFileViewInteractionHub.clearSelection();
                    initMenuItemSelectAllOrCancel();
                    mode.finish();
                    break;
                case R.id.action_select_all:
                    mFileViewInteractionHub.onOperationSelectAll();
                    initMenuItemSelectAllOrCancel();
                    break;
            }
            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
                    .getSelectedFileList().size());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.clearSelection();
            ((FileExplorerTabActivity) mContext).setActionMode(null);
            ((FileExplorerTabActivity) mContext).invalidateOptionsMenu(); //STGOO-2064 
        }
    }
}
