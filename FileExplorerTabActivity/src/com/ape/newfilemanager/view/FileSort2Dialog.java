package com.ape.newfilemanager.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ape.filemanager.R;
import com.ape.newfilemanager.helper.FileSortHelper.SortMethod;

public class FileSort2Dialog extends AlertDialog {

	private final static String TAG = "FileSort2Dialog";
	private Context mContext;
	private View rootView;
	private ListView sort_listview;
	private LinearLayout topview;

	private ArrayList<Integer> SortList;
	public int selectPosion = 0;
	private SortListAdapter mSortAdapter;

	private SortOnClickWatcher mSortOnClickWatcher;

	public FileSort2Dialog(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public FileSort2Dialog(Context context, int themeResId) {
		super(context, themeResId);
		mContext = context;
		initView();

	}

	public FileSort2Dialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;
		initView();
	}

	private void initView() {

		rootView = View.inflate(mContext, R.layout.file_sort2_dialog_layout,
				null);

		topview = (LinearLayout) rootView.findViewById(R.id.top);
		topview.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dismiss();
			}
		});

		sort_listview = (ListView) rootView.findViewById(R.id.sort_list);

		SortList = new ArrayList<Integer>();
		SortList.add(SortMethod.name.ordinal(), R.string.menu_item_sort2_name);
		SortList.add(SortMethod.size.ordinal(), R.string.menu_item_sort2_size);
		SortList.add(SortMethod.date.ordinal(), R.string.menu_item_sort2_date);
		SortList.add(SortMethod.type.ordinal(), R.string.menu_item_sort2_type);
		mSortAdapter = new SortListAdapter();
		sort_listview.setAdapter(mSortAdapter);
		sort_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				selectPosion = position;

				mSortAdapter.refresh();

				//实现点击时，刷新ICON
				new Handler(new Callback() {

					@Override
					public boolean handleMessage(Message msg) {

						if (mSortOnClickWatcher != null) {

							switch (position) {
							case 0:
								mSortOnClickWatcher.onNameClick();
								break;
							case 1:
								mSortOnClickWatcher.onSizeClick();
								break;
							case 2:
								mSortOnClickWatcher.onDateClick();
								break;
							case 3:
								mSortOnClickWatcher.onTypeClick();
								break;

							}
						}

						return false;
					}
				}).sendEmptyMessage(0);
			}

		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(rootView);

		// in here realize full screem
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		Window window = getWindow();
		window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
		window.setWindowAnimations(R.style.Dialog_Enter_Exit_animal); // 添加动画

	}

	public void setSortOnClickWatcher(SortOnClickWatcher mSortOnClickWatcher) {

		this.mSortOnClickWatcher = mSortOnClickWatcher;
	}

	public interface SortOnClickWatcher {

		void onNameClick();

		void onSizeClick();

		void onDateClick();

		void onTypeClick();

	}

	private class SortListAdapter extends BaseAdapter {
		ViewHold hold;

		public void refresh() {

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return SortList.size();
		}

		@Override
		public Object getItem(int position) {

			return null;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {

				View itemView = View.inflate(mContext,
						R.layout.file_sort_list_item, null);
				convertView = itemView;

				hold = new ViewHold();
				hold.title = (TextView) itemView.findViewById(R.id.title);
				hold.checkbox = (ImageView) itemView
						.findViewById(R.id.checkbox);
				convertView.setTag(hold);
			} else {

				hold = (ViewHold) convertView.getTag();
			}
			hold.title.setText(SortList.get(position));
			if (selectPosion == position) {
				hold.checkbox
						.setBackgroundResource(R.drawable.new_select_icon_focus);

			} else {
				hold.checkbox
						.setBackgroundResource(R.drawable.new_select_icon_unfocus);
			}

			return convertView;
		}

		public class ViewHold {

			TextView title;
			ImageView checkbox;
		}

	}

}
