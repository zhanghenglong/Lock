package com.anda.smartlock.ui;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anda.smartlock.R;
import com.anda.smartlock.data.FingerPrint;
import com.anda.smartlock.data.FingerPrintRepo;

import java.util.ArrayList;
import java.util.HashMap;

public class FingerPrintActivity extends ListActivity{
    private static final String TAG = FingerPrintActivity.class.getSimpleName();
    public static final String EXTRAS_FingerPrint_ID = "ID";

    private ArrayList<HashMap<String, String>> mList;
    private ArrayList<FPBean> fpBeanArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FingerPrintRepo fp_repo = new FingerPrintRepo(this);
        fpBeanArrayList = new ArrayList<FPBean>();
        mList = fp_repo.getFingerPrintList();
        for(int i=0; i<mList.size(); i++) {
            HashMap<String, String> bean = mList.get(i);
            FPBean fpBean = new FPBean();
            fpBean.setId(bean.get("id"));
            fpBean.setName(bean.get("name"));
            fpBeanArrayList.add(fpBean);
        }
        FingerPrintListAdapter adapter = new FingerPrintListAdapter(fpBeanArrayList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected_id = fpBeanArrayList.get(position).getId();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRAS_FingerPrint_ID, selected_id);
                setResult(RESULT_OK, returnIntent);
                FingerPrintActivity.this.finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**==============================================================================**
     * 类名：LeDeviceListAdapter
     * 类功能：Adapter for holding devices found through scanning.
     * 范围：inner类
     **==============================================================================**/
    private class FingerPrintListAdapter extends BaseAdapter {
        private ArrayList<FPBean> mFPList;
        private LayoutInflater mInflator;

        public FingerPrintListAdapter(ArrayList<FPBean> mList) {
            super();
            mFPList = mList;
            mInflator = FingerPrintActivity.this.getLayoutInflater();
        }

        public void clear() {
            mFPList.clear();
        }
        @Override
        public int getCount() {
            return mFPList.size();
        }
        @Override
        public Object getItem(int i) {
            return mFPList.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            FingerPrintActivity.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.layout_item_fingerprint, null);
                viewHolder = new FingerPrintActivity.ViewHolder();
                viewHolder.id = (TextView) view.findViewById(R.id.fp_id);
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (FingerPrintActivity.ViewHolder) view.getTag();
            }
            FPBean fingerPrint = mFPList.get(i);
            viewHolder.id.setText(fingerPrint.getId());
            viewHolder.name.setText(fingerPrint.getName());
            return view;
        }
    }
    /**==============================================================================**
     * 类名：ViewHolder
     * 类功能：ViewHolder
     * 范围：static inner类
     **==============================================================================**/
    static class ViewHolder {
        TextView id;
        TextView name;
    }

    class FPBean {
        private String id;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
