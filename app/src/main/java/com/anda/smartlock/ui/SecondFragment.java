package com.anda.smartlock.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anda.smartlock.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {
    private TextView txt_fir;

    public SecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_second, container, false);

        txt_fir = (TextView) view.findViewById(R.id.txt_fir);
        return view;
    }

    public void setNotifyText(String msg) {
        txt_fir.setText(msg);
    }
}
