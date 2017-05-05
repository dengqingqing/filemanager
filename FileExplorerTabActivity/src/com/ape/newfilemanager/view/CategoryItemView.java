package com.ape.newfilemanager.view;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.filemanager.R;
import com.ape.utils.MyLog;

public class CategoryItemView extends RelativeLayout {

	private final static String TAG = "CategoryItemView";
	private Context mContext;

	// open to set content.
	public ImageView category_img;
	public TextView category_text, category_filenumtext;

	private int winWidth, winHeight, width;

	public CategoryItemView(Context context) {
		super(context);
		mContext = context;

		/*WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);

		winWidth = wm.getDefaultDisplay().getWidth();
		winHeight = wm.getDefaultDisplay().getHeight();

		if (winWidth < winHeight) { // horizontal

			width = winWidth / 4;
		} else {  // vertical
			width = winHeight / 4;
		}

		RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(
				width, width);

		setLayoutParams(parms);*/

		initview();
	}

	private void initview() {

		MyLog.i(TAG, "initview start");
		View view = View.inflate(mContext, R.layout.filemanager_category_item,
				this);

		category_img = (ImageView) view.findViewById(R.id.category_img);
		category_text = (TextView) view.findViewById(R.id.category_text);
		category_filenumtext = (TextView) view
				.findViewById(R.id.category_filenumtext);

	}

}
