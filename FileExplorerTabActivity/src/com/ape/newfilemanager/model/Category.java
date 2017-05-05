package com.ape.newfilemanager.model;

import com.ape.filemanager.R;
import com.ape.newfilemanager.helper.FileCategoryHelper.FileCategory;

public class Category {

	public final static String MUSIC = "music";
	public final static String VIDEO = "video";
	public final static String PICTURE = "picture";
	public final static String DOC = "doc";
	public final static String ZIP = "zip";
	public final static String APK = "apk";

	// resource define
	public final static int MUSIC_ImgID = R.drawable.new_music_icon_unfocus;
	public final static int MUSIC_ImgFocusID = R.drawable.new_music_icon_focus;

	public final static int VIDEO_ImgID = R.drawable.new_video_icon_unfocus;
	public final static int VIDEO_ImgFocusID = R.drawable.new_video_icon_focus;

	public final static int PICTURE_ImgID = R.drawable.new_pic_icon_unfocus;
	public final static int PICTURE_ImgFocusID = R.drawable.new_pic_icon_focus;

	public final static int DOC_ImgID = R.drawable.new_doc_icon_unfucos;
	public final static int DOC_ImgFocusID = R.drawable.new_doc_icon_fucos;

	public final static int ZIP_ImgID = R.drawable.new_zip_icon_unfocus;
	public final static int ZIP_ImgFocusID = R.drawable.new_zip_icon_focus;

	public final static int APK_ImgID = R.drawable.new_apk_icon_unfocus;
	public final static int APK_ImgFocusID = R.drawable.new_apk_icon_focus;

	public final static int MUSIC_TextID = R.string.category_music;
	public final static int VIDEO_TextID = R.string.category_video;
	public final static int PICTURE_TextID = R.string.category_picture;
	public final static int DOC_TextID = R.string.category_document;
	public final static int ZIP_TextID = R.string.category_zip;
	public final static int APK_TextID = R.string.category_apk;

	public FileCategory mFileCategory; // indicate category
	public int imgid;
	public int img_focus_id;
	public int textid;
	public long numtextid;

	public Category() {

	}

}
