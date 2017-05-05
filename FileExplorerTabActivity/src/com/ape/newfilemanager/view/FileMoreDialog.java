package com.ape.newfilemanager.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ape.filemanager.R;

public class FileMoreDialog extends AlertDialog implements View.OnClickListener {

	private final static String TAG = "FileMoreDialog";
	private Context mContext;
	private View rootView;
	private LinearLayout topView;

	private TextView share_button, zip_button;;

	private MoreWatcher mMoreWatcher = null;

	public FileMoreDialog(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public FileMoreDialog(Context context, int themeResId) {
		super(context, themeResId);
		mContext = context;
		initView();

	}

	public FileMoreDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;
		initView();
	}

	private void initView() {

		rootView = View.inflate(mContext, R.layout.file_more_dialog_layout,
				null);
		
		topView = (LinearLayout) rootView.findViewById(R.id.top);
		
		topView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dismiss();
				
			}
		});

		share_button = (TextView) rootView.findViewById(R.id.share);
		zip_button = (TextView) rootView.findViewById(R.id.zip);

		share_button.setOnClickListener(this);
		zip_button.setOnClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(rootView);

		//in here realize full screem
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		 Window window = getWindow();  
	     window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置  
	     window.setWindowAnimations(R.style.Dialog_Enter_Exit_animal); // 添加动画  

	}

	@Override
	public void onClick(View v) {

		if (mMoreWatcher != null) {

			if (v.getId() == R.id.share) {

				mMoreWatcher.shareOnclick(v);

			} else if (v.getId() == R.id.zip) {

				mMoreWatcher.zipOnclick(v);
			}
		}

	}

	public void setMoreWatcher(MoreWatcher watcher) {

		mMoreWatcher = watcher;

	}

	public interface MoreWatcher {

		void shareOnclick(View v);

		void zipOnclick(View v);
	}

}
