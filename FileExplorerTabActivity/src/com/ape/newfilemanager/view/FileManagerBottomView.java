
package com.ape.newfilemanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ape.filemanager.R;
import com.ape.utils.MyLog;

public class FileManagerBottomView extends RelativeLayout implements View.OnClickListener {

    private final static String TAG = "FileManagerBottonView";
    private Context mContext;

    private Button sort, reflesh, copy, shear, delete, more, newFold;
    private LinearLayout buttoncontainer;

    public final static int HOMEPAGE = 0;
    public final static int NO_FILES = 1;
    public final static int FILE_NOMAL = 2;
    public final static int FILE_EIDT = 3;
    public final static int FILE_NEW = 4;

    private int BottonMode = NO_FILES;
    private onClickWatcher monClickWatcher = null;

    public FileManagerBottomView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public FileManagerBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public FileManagerBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {

        MyLog.i(TAG, "initview start");
        View view = View.inflate(mContext,
                R.layout.filemanager_bottonview_layout, this);

        buttoncontainer = (LinearLayout) view.findViewById(R.id.buttoncontainer);

        sort = (Button) view.findViewById(R.id.sort);
        reflesh = (Button) view.findViewById(R.id.reflesh);
        copy = (Button) view.findViewById(R.id.copy);
        shear = (Button) view.findViewById(R.id.shear);
        delete = (Button) view.findViewById(R.id.delete);
        more = (Button) view.findViewById(R.id.more);
        newFold = (Button) view.findViewById(R.id.newFold);

        sort.setOnClickListener(this);
        reflesh.setOnClickListener(this);
        copy.setOnClickListener(this);
        shear.setOnClickListener(this);
        delete.setOnClickListener(this);
        more.setOnClickListener(this);
        newFold.setOnClickListener(this);
     
    }

    @Override
    public void onClick(View v) {

        MyLog.i(TAG, "onClick ");
        if(monClickWatcher == null)
        	return;
        
        switch (v.getId()) {
            case R.id.sort:
                monClickWatcher.sortOnclick(v);
                break;
            case R.id.reflesh:
                monClickWatcher.refleshOnclick(v);
                break;
            case R.id.copy:
                monClickWatcher.copyOnclick(v);
                break;
            case R.id.shear:
                monClickWatcher.shearOnclick(v);
                break;
            case R.id.delete:
                monClickWatcher.deleteOnclick(v);
                break;
            case R.id.more:
                monClickWatcher.moreOnclick(v);
                break;
            case R.id.newFold:
                monClickWatcher.newFoldOnclick(v);
                break;

            default:
                break;
        }

    }

    public void setBottomViewMode(int mode) {

        for (int i = 0; i < buttoncontainer.getChildCount(); i++) {

            buttoncontainer.getChildAt(i).setVisibility(View.GONE);
            buttoncontainer.removeAllViews();
        }
        this.setVisibility(View.VISIBLE);
        switch (mode) {
            case NO_FILES:
                buttoncontainer.addView(reflesh);
                reflesh.setVisibility(View.VISIBLE);            
                break;
            case FILE_NOMAL:
                buttoncontainer.addView(reflesh);
                buttoncontainer.addView(sort);
                reflesh.setVisibility(View.VISIBLE);
                sort.setVisibility(View.VISIBLE);
                break;
            case FILE_EIDT:
                buttoncontainer.addView(copy);
                buttoncontainer.addView(shear);
                buttoncontainer.addView(delete);
                buttoncontainer.addView(more);
                copy.setVisibility(View.VISIBLE);
                shear.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                more.setVisibility(View.VISIBLE);
                break;
            case FILE_NEW:
                buttoncontainer.addView(newFold);
                buttoncontainer.addView(sort);
                buttoncontainer.addView(reflesh);
                newFold.setVisibility(View.VISIBLE);
                sort.setVisibility(View.VISIBLE);
                reflesh.setVisibility(View.VISIBLE);
                break;
            default:
                buttoncontainer.removeAllViews();
                this.setVisibility(View.GONE);
                break;
        }

        requestLayout();
    }

    public void setOnClickWatcher(onClickWatcher listener) {

        monClickWatcher = listener;

    }

    public static interface onClickWatcher {

        void refleshOnclick(View v);

        void sortOnclick(View v);

        void copyOnclick(View v);

        void shearOnclick(View v);

        void deleteOnclick(View v);

        void moreOnclick(View v);

        void newFoldOnclick(View v);

    }

}
