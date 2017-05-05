
package com.ape.newfilemanager.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ape.filemanager.R;

import java.util.ArrayList;

public class FileSortDialog extends AlertDialog implements View.OnClickListener {

    private final static String TAG = "FileSortDialog";
    private Context mContext;
    private View rootView;

    private ListView listview;
    private Button leftbutton, rightbutton;
    private ArrayList<Integer> sortdata;
    private SortAdapter mSortAdapter;
    private static int selectPosion = 0;

    public final static int SORT_NAME = 0;
    public final static int SORT_SIZE = 1;
    public final static int SORT_DATA = 2;
    public final static int SORT_TYPE = 3;

    private SortWatcher mSortWatcher = null;

    public FileSortDialog(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public FileSortDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        initView();

    }

    public FileSortDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        initView();
    }

    private void initView() {

        rootView = View.inflate(mContext, R.layout.file_sort_dialog_layout, null);

        listview = (ListView) rootView.findViewById(R.id.listview);
        leftbutton = (Button) rootView.findViewById(R.id.leftbutton);
        rightbutton = (Button) rootView.findViewById(R.id.rightbutton);
        leftbutton.setOnClickListener(this);
        rightbutton.setOnClickListener(this);

        sortdata = new ArrayList<Integer>();
        sortdata.add(R.string.menu_item_sort_name);
        sortdata.add(R.string.menu_item_sort_size);
        sortdata.add(R.string.menu_item_sort_date);
        sortdata.add(R.string.menu_item_sort_type);
        mSortAdapter = new SortAdapter();

        listview.setAdapter(mSortAdapter);

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectPosion = position;
                mSortAdapter.refresh();

            }

        });

        setView(rootView);
    }

    @Override
    public void onClick(View v) {

        if (mSortWatcher != null) {

            if (v.getId() == R.id.leftbutton) {
                
                mSortWatcher.sortAscdingOnclick(selectPosion, v);

            } else if (v.getId() == R.id.rightbutton) {
                
                mSortWatcher.sortDescdingOnclick(selectPosion, v);          
            }
        }
        
        dismiss();
    }

    private class SortAdapter extends BaseAdapter {

        ViewHold hold;

        public void refresh() {

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return sortdata.size();
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

                View itemView = View.inflate(mContext, R.layout.file_sort_list_item, null);
                convertView = itemView;

                hold = new ViewHold();
                hold.title = (TextView) itemView.findViewById(R.id.title);
                hold.checkbox = (ImageView) itemView.findViewById(R.id.checkbox);
                convertView.setTag(hold);
            } else {

                hold = (ViewHold) convertView.getTag();
            }
            hold.title.setText(sortdata.get(position));
            if (selectPosion == position) {
                hold.checkbox.setBackgroundResource(R.drawable.new_select_icon_focus);
                ;
            } else {
                hold.checkbox.setBackgroundResource(R.drawable.new_select_icon_unfocus);
            }

            return convertView;
        }

    }

    public class ViewHold {

        TextView title;
        ImageView checkbox;
    }

    public void setSortWatcher(SortWatcher watcher) {

        mSortWatcher = watcher;
    }

    public interface SortWatcher {

        void sortAscdingOnclick(int sorttype, View v);

        void sortDescdingOnclick(int sorttype, View v);
    }

}
