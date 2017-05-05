
package com.ape.newfilemanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ape.filemanager.R;
import com.ape.utils.MyLog;

public class FileManagerOperationView extends RelativeLayout implements View.OnClickListener {

	private final static String TAG = "FileManagerOprationView";
	private Context mContext;

	private Button paster, cancel;
	private LinearLayout oprate_container;

	private OperateOnClickWatcher mOperateOnClickWatcher = null;

	public FileManagerOperationView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public FileManagerOperationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		initView();
	}

	public FileManagerOperationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	private void initView() {

		MyLog.i(TAG, "initview start");
		View view = View.inflate(mContext, R.layout.filemanager_opration_view_layout, this);

		oprate_container = (LinearLayout) view.findViewById(R.id.oprate_container);

		paster = (Button) view.findViewById(R.id.paste);
		cancel = (Button) view.findViewById(R.id.cancle);

		paster.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		MyLog.i(TAG, "onClick ");
			
		if (mOperateOnClickWatcher != null) {
			switch (v.getId()) {
			case R.id.paste:
				mOperateOnClickWatcher.pasterOnclick(v);
				break;
			case R.id.cancle:
				mOperateOnClickWatcher.cancelOnclick(v);
				break;
			default:
				break;
			}
		}
	}

	public void setOperateOnClickWatcher(OperateOnClickWatcher listener) {

		mOperateOnClickWatcher = listener;

	}

	public static interface OperateOnClickWatcher {

		void pasterOnclick(View v);

		void cancelOnclick(View v);

	}

}
