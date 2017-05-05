
package com.ape.newfilemanager.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;  
  
/** 
 * 下拉时具有弹性的ListView 
 */  
public class FlexibleListView extends ListView{  
      
    private static final int MAX_OVERSCROLL_Y = 300;

    private Context mContext;
    private int newMaxOverScrollY;

    public FlexibleListView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public FlexibleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float density = metrics.density;
        newMaxOverScrollY = (int) (density * MAX_OVERSCROLL_Y);
    }

    // 最关键的地方。
    //支持到SDK8需要增加@SuppressLint("NewApi")。
    @SuppressLint("NewApi")
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
            int scrollY, int scrollRangeX, int scrollRangeY,
            int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, newMaxOverScrollY,
                isTouchEvent);
    }
  
}  