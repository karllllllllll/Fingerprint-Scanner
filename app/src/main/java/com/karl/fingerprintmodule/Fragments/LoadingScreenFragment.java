package com.karl.fingerprintmodule.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karl.fingerprintmodule.R;
import com.skyfishjy.library.RippleBackground;

public class LoadingScreenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_loading_data, container, false);
        initViews(v);
        //rb_animation.startRippleAnimation();
        return v;
    }

    private ProgressBar pb_loading;
    private ImageView iv_success;
    private ImageView iv_failed;
    private ImageView iv_fingerprint;
    private TextView tv_message;
    private RippleBackground rb_animation;

    private void initViews(View v) {

        pb_loading = v.findViewById(R.id.pb_loading);
        tv_message = v.findViewById(R.id.tv_message);
        iv_success = v.findViewById(R.id.iv_success);
        iv_failed = v.findViewById(R.id.iv_failed);
        iv_fingerprint = v.findViewById(R.id.iv_fingerprint);

        rb_animation = v.findViewById(R.id.rb_animation);
    }
}

