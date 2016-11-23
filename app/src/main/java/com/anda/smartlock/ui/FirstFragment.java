package com.anda.smartlock.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.anda.smartlock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment {

    private Button.OnClickListener onClickListener;
    private TextView txt_notify;

    public FirstFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

      //  Button button = (Button) view.findViewById(R.id.notify);
      //  button.setOnClickListener(onClickListener);
        txt_notify = (TextView) view.findViewById(R.id.txt_notify);
        return view;
    }
/*
    public void setOnClickListener(Button.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
    */
    public void setNotifyText(String msg) {
        txt_notify.setText(msg);
    }
}
