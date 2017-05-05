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

package com.ape.newfilemanager.interfaces;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.ape.newfilemanager.helper.FileIconHelper;
import com.ape.newfilemanager.helper.FileSortHelper;
import com.ape.newfilemanager.model.FileInfo;

import java.util.Collection;

public interface IFileInteractionListener {

    public View getViewById(int id);

    public Context getmContext();

    public void startActivity(Intent intent);
    
    public Activity getActivity();

    public void onDataChanged();

    public void onPick(FileInfo f);

    public boolean onOperation(int id);

    public String getDisplayPath(String path);

    public String getRealPath(String displayPath);
    
    public void runOnUiThread(Runnable r);

    public FileIconHelper getFileIconHelper();

    public FileInfo getItem(int pos);

    public void sortCurrentList(FileSortHelper sort);

    public Collection<FileInfo> getAllFiles();

    public void addSingleFile(FileInfo file);

    public boolean onRefreshFileList(String path, FileSortHelper sort);

    public int getItemCount();
}
