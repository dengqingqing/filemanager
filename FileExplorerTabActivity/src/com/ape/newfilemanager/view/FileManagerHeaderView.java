package com.ape.newfilemanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.filemanager.R;
import com.ape.utils.MyLog;

public class FileManagerHeaderView extends RelativeLayout implements
		View.OnClickListener {

	private final static String TAG = "FileManagerHeaderView";
	private Context mContext;

	public TextView headtitle;
	private TextView operation_selectall, edit_text;
	private RelativeLayout leftbutton, rightbutton;
	private ImageView return_img;

	public final static int HOMEPAGE = 0;
	public final static int FILE_NOMAL = 1;
	public final static int FILE_EIDT = 2;
	public final static int RETURN_MODE = 3;

	private int HeadMode = HOMEPAGE;
	private onClickListenr mOnClickListenr = null;

	public FileManagerHeaderView(Context context) {
		super(context);
		mContext = context;

		initView();
	}

	public FileManagerHeaderView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		initView();
	}

	public FileManagerHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	private void initView() {

		MyLog.i(TAG, "initview start");
		View view = View.inflate(mContext,
				R.layout.filemanager_headview_layout, this);

		headtitle = (TextView) view.findViewById(R.id.headtitle);

		leftbutton = (RelativeLayout) view.findViewById(R.id.leftbutton);
		rightbutton = (RelativeLayout) view.findViewById(R.id.rightbutton);

		/*
		 * leftbutton.setOnClickListener(this);
		 * rightbutton.setOnClickListener(this);
		 */

		operation_selectall = (TextView) view
				.findViewById(R.id.operation_selectall);
		return_img = (ImageView) view.findViewById(R.id.return_img);

		edit_text = (TextView) view.findViewById(R.id.edit_text);

		operation_selectall.setOnClickListener(this);
		return_img.setOnClickListener(this);
		edit_text.setOnClickListener(this);

		// default
		setHeadMode(HOMEPAGE);

	}

	public int getHeadMode() {
		return HeadMode;
	}

	public void setHeadMode(int headMode) {
		HeadMode = headMode;

		switch (HeadMode) {
		default:
		case HOMEPAGE:
			leftbutton.setVisibility(View.INVISIBLE);
			rightbutton.setVisibility(View.INVISIBLE);
			headtitle.setText(R.string.app_name);
			break;
		case FILE_NOMAL:
			leftbutton.setVisibility(View.VISIBLE);
			rightbutton.setVisibility(View.VISIBLE);

			operation_selectall.setVisibility(View.INVISIBLE);
			return_img.setVisibility(View.VISIBLE);

			edit_text.setVisibility(View.VISIBLE);
			edit_text.setText(R.string.operation_edit);
			break;
		case FILE_EIDT:
			rightbutton.setVisibility(View.VISIBLE);
			leftbutton.setVisibility(View.VISIBLE);

			operation_selectall.setVisibility(View.VISIBLE);
			return_img.setVisibility(View.INVISIBLE);

			edit_text.setVisibility(View.VISIBLE);
			edit_text.setText(R.string.operation_cancel);
			break;
		case RETURN_MODE:
			leftbutton.setVisibility(View.VISIBLE);
			rightbutton.setVisibility(View.INVISIBLE);

			operation_selectall.setVisibility(View.INVISIBLE);
			return_img.setVisibility(View.VISIBLE);
			break;

		}

	}

	@Override
	public void onClick(View v) {

		MyLog.i(TAG, "onClick ");

		if (mOnClickListenr != null) {

			switch (v.getId()) {

			case R.id.return_img:

				mOnClickListenr.HeadViewReturnOnclick(v);
				break;

			case R.id.operation_selectall:

				mOnClickListenr.HeadViewSelectAllOnclick(v);
				;
				break;

			case R.id.edit_text:

				if (HeadMode == FILE_EIDT) {
					mOnClickListenr.HeadViewCancelOnclick(v);

				} else {

					mOnClickListenr.HeadViewEditOnclick(v);
				}

			}

		}

	}

	public void setOnClickListener(onClickListenr listener) {

		mOnClickListenr = listener;

	}

	public static interface onClickListenr {

		void HeadViewReturnOnclick(View v);

		void HeadViewSelectAllOnclick(View v);

		void HeadViewEditOnclick(View v);

		void HeadViewCancelOnclick(View v);

	}

}
