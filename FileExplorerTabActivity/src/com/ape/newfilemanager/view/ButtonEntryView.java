package com.ape.newfilemanager.view;

import com.ape.filemanager.R;
import com.ape.utils.MyLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ButtonEntryView extends RelativeLayout {

	private final static String TAG = "ButtonEntryView";
	private Context mContext;

	public ImageView topline;
	public TextView title, storageshow;
	private RelativeLayout entrybutton;
	private onclickListener mOnclickListener;

	public ButtonEntryView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	private void initView() {

		MyLog.i(TAG, "initview start");
		View view = View.inflate(mContext, R.layout.button_entryview_layout,
				this);

		topline = (ImageView) view.findViewById(R.id.topline);
		
		entrybutton = (RelativeLayout) view.findViewById(R.id.entrybutton);

		title = (TextView) view.findViewById(R.id.title);

		storageshow = (TextView) view.findViewById(R.id.storageshow);

		entrybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MyLog.i(TAG, "onClick");
				if (mOnclickListener != null) {
					mOnclickListener.onClick(v);
				}
			}
		});

	}

	public void setOnclickListener(onclickListener mOnclickListener) {

		this.mOnclickListener = mOnclickListener;
	}

	public interface onclickListener {

		void onClick(View v);
	}

}
