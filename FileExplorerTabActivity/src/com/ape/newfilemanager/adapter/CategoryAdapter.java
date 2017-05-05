package com.ape.newfilemanager.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.filemanager.R;
import com.ape.newfilemanager.helper.FileCategoryHelper.FileCategory;
import com.ape.newfilemanager.model.Category;
import com.ape.newfilemanager.view.CategoryItemView;
import com.ape.utils.MyLog;

public class CategoryAdapter extends BaseAdapter {

	private final static String TAG = "CategoryAdapter";
	private Context mContext;
	private List<Category> dataList;

	public CategoryAdapter(Context context, List<Category> datalist) {

		mContext = context;
		dataList = datalist;
	}

	public void reflesh() {

		notifyDataSetChanged();

	}

	@Override
	public int getCount() {

		if (dataList != null) {
			return dataList.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {

		MyLog.i(TAG, "   ----getItem(int position=" + position + ")");
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHold viewHold;
		String mUnit = "";
		if (convertView == null) {

			CategoryItemView cateview = new CategoryItemView(mContext);

			viewHold = new ViewHold();

			viewHold.img = cateview.category_img;
			viewHold.text = cateview.category_text;
			viewHold.textnum = cateview.category_filenumtext;

			convertView = cateview;
			convertView.setTag(viewHold);

		} else {

			viewHold = (ViewHold) convertView.getTag();
		}

		if(position > (dataList.size()-1)){
			
			return convertView;
		}

		viewHold.mFileCategory = dataList.get(position).mFileCategory;
		viewHold.img.setBackgroundResource(dataList.get(position).imgid);
		viewHold.text.setText(dataList.get(position).textid);

		switch (viewHold.mFileCategory) {
		case Music:
			mUnit = mContext.getString(R.string.song_unit);
			break;
		default:
		case Zip:
		case Apk:
		case Video:
			mUnit = mContext.getString(R.string.a_unit);
			break;
		case Picture:
			mUnit = mContext.getString(R.string.sheet_unit);
			break;
		case Doc:
			mUnit = mContext.getString(R.string.doc_unit);
			break;

		}

		// need change int to string to show.

		viewHold.textnum
				.setText((String.valueOf(dataList.get(position).numtextid))
						+ "" + mUnit);

		return convertView;
	}

	public static class ViewHold {
		public FileCategory mFileCategory;
		public ImageView img;
		public TextView text;
		public TextView textnum;

	}

}
