package com.karl.fingerprintmodule.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karl.fingerprintmodule.Helper;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.Static;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.ViewModels.TimekeepingViewModel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.text.Charsets;

public class PinInputFragment extends Fragment {

    private Context ctx;
    private TimekeepingViewModel tkviewModel;

    private Helper helper;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pin_input, container, false);
        ctx = v.getContext();
        init();
        initViews(v);
        setListeners();
        setObservers();

        getUser();


        return v;
    }

    private void init() {
        helper = Helper.getInstance(ctx);
        tkviewModel = ViewModelProviders.of(getActivity()).get(TimekeepingViewModel.class);
    }

    private void getUser() {
        try {
            user = (User) getArguments().getSerializable("user");

            Glide.with(this)
                    .load(user.getImage_path())
                    .apply(RequestOptions
                            .circleCropTransform())
                    .into(iv_avatar);

            String fullName = user.getF_name() + " " + user.getL_name();
            tv_name.setText(fullName);
            tv_time_in.setText(helper.convertToReadableTime(user.getTime_in()));
            tv_time_out.setText(helper.convertToReadableTime(user.getTime_out()));
        } catch (Exception e) {

            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private Button numpad_0;
    private Button numpad_1;
    private Button numpad_2;
    private Button numpad_3;
    private Button numpad_4;
    private Button numpad_5;
    private Button numpad_6;
    private Button numpad_7;
    private Button numpad_8;
    private Button numpad_9;
    private ImageView pin_1;
    private ImageView pin_2;
    private ImageView pin_3;
    private ImageView pin_4;

    private LinearLayout numpad_clear;
    private LinearLayout numpad_send;

    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_time_in;
    private TextView tv_time_out;

    private void initViews(View v) {

        iv_avatar = v.findViewById(R.id.iv_avatar);
        tv_name = v.findViewById(R.id.tv_name);
        tv_time_in = v.findViewById(R.id.tv_time_in);
        tv_time_out = v.findViewById(R.id.tv_time_out);

        numpad_0 = v.findViewById(R.id.numpad_0);
        numpad_1 = v.findViewById(R.id.numpad_1);
        numpad_2 = v.findViewById(R.id.numpad_2);
        numpad_3 = v.findViewById(R.id.numpad_3);
        numpad_4 = v.findViewById(R.id.numpad_4);
        numpad_5 = v.findViewById(R.id.numpad_5);
        numpad_6 = v.findViewById(R.id.numpad_6);
        numpad_7 = v.findViewById(R.id.numpad_7);
        numpad_8 = v.findViewById(R.id.numpad_8);
        numpad_9 = v.findViewById(R.id.numpad_9);

        pin_1 = v.findViewById(R.id.pin_1);
        pin_2 = v.findViewById(R.id.pin_2);
        pin_3 = v.findViewById(R.id.pin_3);
        pin_4 = v.findViewById(R.id.pin_4);

        numpad_clear = v.findViewById(R.id.numpad_clear);
        numpad_send = v.findViewById(R.id.numpad_send);
    }

    private void setListeners() {
        numpad_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("0");
            }
        });
        numpad_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("1");
            }
        });
        numpad_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("2");
            }
        });
        numpad_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("3");
            }
        });
        numpad_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("4");
            }
        });
        numpad_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("5");
            }
        });
        numpad_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("6");
            }
        });
        numpad_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("7");
            }
        });
        numpad_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("8");
            }
        });
        numpad_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("9");
            }
        });

        numpad_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPINS()) {

                    String enteredPIN = PIN1 + PIN2 + PIN3 + PIN4;
                    if (checkUserPIN(user.getPin(), enteredPIN)) {

                        tkviewModel.sendClockInOut(user);
                    } else {
                        Toast.makeText(getActivity(), "Wrong PIN", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        numpad_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTo("clear");
            }
        });
    }

    String PIN1 = null;
    String PIN2 = null;
    String PIN3 = null;
    String PIN4 = null;

    private void setTo(String value) {
        if (value.equals("clear")) {
            if (PIN4 != null) {

                PIN4 = null;
                pin_4.setSelected(false);
            } else {
                if (PIN3 != null) {

                    PIN3 = null;
                    pin_3.setSelected(false);
                } else {
                    if (PIN2 != null) {

                        PIN2 = null;
                        pin_2.setSelected(false);
                    } else {

                        PIN1 = null;
                        pin_1.setSelected(false);
                    }
                }
            }

        } else {
            if (PIN1 == null) {

                PIN1 = value;
                pin_1.setSelected(true);
            } else {
                if (PIN2 == null) {

                    PIN2 = value;
                    pin_2.setSelected(true);
                } else {

                    if (PIN3 == null) {

                        PIN3 = value;
                        pin_3.setSelected(true);
                    } else {

                        if (PIN4 == null) {

                            PIN4 = value;
                            pin_4.setSelected(true);
                        }
                    }

                }
            }
        }
    }

    private Boolean checkPINS() {
        return PIN1 != null && PIN2 != null && PIN3 != null && PIN4 != null;
    }

    private Boolean checkUserPIN(String user_pin, String entered_password) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        md.update(entered_password.trim().getBytes(Charsets.UTF_8));
        byte[] digest = md.digest();
        StringBuffer hexString = new StringBuffer(digest.length * 2);

        for (byte i : digest) {

            int b = i & (byte) 0xFF;

            if (b < 0x10) {
                hexString.append('0');
            }
            hexString.append(Integer.toHexString(b));
        }

        String hs = hexString.toString().replace("0ffffff", "");

        if (hs.equals(user_pin)) {
            return true;
        } else {
            clearAll();
            return false;
        }
    }

    private void clearAll() {

        PIN1 = null;
        PIN2 = null;
        PIN3 = null;
        PIN4 = null;

        pin_1.setSelected(false);
        pin_2.setSelected(false);
        pin_3.setSelected(false);
        pin_4.setSelected(false);
    }

    private void setObservers() {
        tkviewModel.getTimeInOutResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(@Nullable Result result) {
                if (result != null && result.getStatus().equals(Static.API_STATUS_SUCCESS)) {
                    Toast.makeText(ctx, result.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ctx, result.getMessage(), Toast.LENGTH_LONG).show();
                }
                clearAll();
            }
        });
    }

}
