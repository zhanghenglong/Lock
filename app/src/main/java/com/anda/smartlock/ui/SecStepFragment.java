package com.anda.smartlock.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anda.smartlock.R;


public class SecStepFragment extends Fragment {
    private TextView txt_sec;

    public SecStepFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_third, container, false);
        txt_sec = (TextView) view.findViewById(R.id.txt_sec);
        return view;
    }


    public void setNotifyText(String msg) {
        txt_sec.setText(msg);
    }


}
