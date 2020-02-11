package com.karl.fingerprintmodule.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;
import com.karl.fingerprintmodule.Adapters.UsersAdapter;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.Helper;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.Static;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.ViewModels.TimekeepingViewModel;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;

public class TimeInOutFragment extends Fragment {

    private Context applContext;
    private Activity Act;

    private FmdViewModel viewModel;
    private TimekeepingViewModel tkviewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.activity_time_in, container, false);
        //applContext = this.getContext();

        applContext = getActivity().getApplicationContext();
        Act = getActivity();
        m_deviceName = getArguments().getString("device_name");

        init();
        initViews(v);
        initReader();
        setListeners();

        retrieveData();

        return v;
    }

    private void init() {

        helper = Helper.getInstance(applContext);
        viewModel = ViewModelProviders.of(getActivity()).get(FmdViewModel.class);
        tkviewModel = ViewModelProviders.of(getActivity()).get(TimekeepingViewModel.class);
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

        ownerDialog = new Dialog(Act, R.style.AppTheme_SlideAnimation);
        //ownerDialog = new Dialog(this);
    }

    private String m_deviceName;
    private String m_enginError;
    private Reader m_reader = null;
    private int m_DPI = 0;
    private Engine m_engine = null;
    private Engine.Candidate[] results = null;
    private String deviceName = "";

    private void initReader() {
        try {

            //deviceName = getIntent().getExtras().getString("device_name");
            deviceName = getArguments().getString("device_name");
            m_deviceName = deviceName;
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);

            m_reader.Open(Reader.Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();

        } catch (Exception e) {
            //Toast.makeText(applContext, "Reader not found : " + deviceName, Toast.LENGTH_LONG).show();
            Toast.makeText(applContext, e.toString(), Toast.LENGTH_LONG).show();
            //setBooleans(false, false, false, true);
        }
    }

    private Boolean hasFingerprints = false;
    private Boolean hasUsers = false;

    private void runThreadIfDataComplete() {

        if (hasFingerprints && hasUsers) {
            tv_debugger_text = "Place your thumb on top of the device";
            setBooleans(false, false, false, false);
            UpdateGUI();
            runThread();
        }
    }

    private void getUsersAfterFingerprints() {
        Boolean userListHasValue = tkviewModel.getUserArrayList().getValue() != null;

        if (userListHasValue) {
            if (tkviewModel.getUserArrayList().getValue().size() > 0) {

                hasUsers = true;
                runThreadIfDataComplete();
            } else {
                Toast.makeText(applContext, "No Users To Search", Toast.LENGTH_LONG).show();
            }
        } else {

            tkviewModel.getUsers();

            tkviewModel.getUserArrayList().observe(this, new Observer<ArrayList<User>>() {
                @Override
                public void onChanged(@Nullable ArrayList<User> users) {

                    if (users != null && users.size() > 0) {

                        hasUsers = true;
                        runThreadIfDataComplete();
                    } else {
                        Toast.makeText(applContext, "No Users To Search", Toast.LENGTH_LONG).show();
                        showRetryDialog();
                    }
                }
            });
        }
    }

    private void setListeners() {

        viewModel.getfmdListConversionResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(@Nullable Result result) {
                if (result != null && result.getStatus().equals("success")) {

                    hasFingerprints = true;

                    ArrayList<User> users = tkviewModel.getUserArrayList().getValue();
                    if (users != null && users.size() > 0) {
                        hasUsers = true;
                        runThreadIfDataComplete();
                    } else {
                        getUsersAfterFingerprints();
                    }
                } else {

                    tv_debugger_text = "";
                    setBooleans(false, false, false, true);
                    showRetryDialog();
                }
            }
        });

        tkviewModel.getFindUserResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {

                if (result.getStatus().equals(Static.API_STATUS_SUCCESS)) {

                    //showOwner();
                } else {
                    fingerPrintOwner = null;
                    Toast.makeText(applContext, Static.API_STATUS_FAILED, Toast.LENGTH_LONG).show();
                }

                showOwner();
            }
        });

        tkviewModel.getTimeInOutResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(@Nullable Result result) {

                if (result.getStatus().equals(Static.API_STATUS_SUCCESS)) {

                    Toast.makeText(applContext, "Time in successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applContext, result.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void retrieveData() {

        viewModel.retrieveFingerPrints();
        setBooleans(true, false, false, false);
        UpdateGUI();
    }

    private Dialog ownerDialog;
    private Helper helper;

    private void showOwner() {

        if (ownerDialog.isShowing()) {
            ownerDialog.dismiss();
        }

        ownerDialog.setContentView(R.layout.dialog_fingerprint_owner_info);
        ImageView iv_avatar = ownerDialog.findViewById(R.id.iv_avatar);
        TextView tv_fullname = ownerDialog.findViewById(R.id.tv_fullname);
        TextView tv_current_time = ownerDialog.findViewById(R.id.tv_current_time);
        TextView tv_time_in = ownerDialog.findViewById(R.id.tv_time_in);
        TextView tv_time_out = ownerDialog.findViewById(R.id.tv_time_out);
        Button btn_clock_in_out = ownerDialog.findViewById(R.id.btn_clock_in_out);

        final String time = helper.now();

        if (fingerPrintOwner != null) {

            try {

                String fullName = fingerPrintOwner.getF_name() + " " + fingerPrintOwner.getL_name();

                tv_time_in.setText(helper.convertToReadableTime(fingerPrintOwner.getTime_in()));
                tv_time_out.setText(helper.convertToReadableTime(fingerPrintOwner.getTime_out()));
                tv_current_time.setText(helper.convertToReadableTime(time));
                tv_fullname.setText(fullName);

                Glide.with(this)
                        .load(fingerPrintOwner.getImage_path())
                        .apply(RequestOptions.circleCropTransform())
                        .into(iv_avatar);

                String blankTime = "null";

                if (!fingerPrintOwner.getTime_in().equals(blankTime) && !fingerPrintOwner.getTime_out().equals(blankTime)) {

                    btn_clock_in_out.setEnabled(false);
                    btn_clock_in_out.setText("You have already clocked out for the day!");
                } else {

                    btn_clock_in_out.setEnabled(true);

                    if (fingerPrintOwner.getTime_in().equals(blankTime)) {
                        btn_clock_in_out.setText("Clock in");
                    } else {
                        btn_clock_in_out.setText("Clock out");
                    }

                    btn_clock_in_out.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tkviewModel.sendClockInOut(fingerPrintOwner);
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(applContext, e.toString(), Toast.LENGTH_LONG).show();
            }

        } else {

            Toast.makeText(applContext, "Inangyan!", Toast.LENGTH_LONG).show();

            tv_time_in.setText("");
            tv_time_out.setText("");
            tv_current_time.setText(helper.convertToReadableTime(time));
            tv_fullname.setText("User not found");
            btn_clock_in_out.setEnabled(false);
            btn_clock_in_out.setText("Cannot clock in");
        }

        ownerDialog.show();
    }

    private void showRetryDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(applContext);
        alertDialogBuilder.setTitle("Oops")
                .setMessage("Something went wrong!\nRetry?")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        retrieveData();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(applContext, "Close App", Toast.LENGTH_LONG).show();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setBooleans(Boolean loading, Boolean capturing, Boolean success, Boolean failed) {

        this.loading = loading;
        this.capturing = capturing;
        this.success = success;
        this.failed = failed;
    }

    String tv_debugger_text = "";
    boolean loading = false;
    boolean capturing = false;
    boolean success = false;
    boolean failed = false;

    private void UpdateGUI() {

        tv_message.setText(tv_debugger_text);
        rb_animation.stopRippleAnimation();

        if (loading) {

            pb_loading.setVisibility(View.VISIBLE);
            iv_success.setVisibility(View.GONE);
            iv_failed.setVisibility(View.GONE);
            iv_fingerprint.setVisibility(View.GONE);
        } else if (success) {
            pb_loading.setVisibility(View.GONE);
            iv_success.setVisibility(View.VISIBLE);
            iv_failed.setVisibility(View.GONE);
            iv_fingerprint.setVisibility(View.GONE);
        } else if (failed) {
            pb_loading.setVisibility(View.GONE);
            iv_success.setVisibility(View.GONE);
            iv_failed.setVisibility(View.VISIBLE);
            iv_fingerprint.setVisibility(View.GONE);
        } else {
            pb_loading.setVisibility(View.GONE);
            iv_success.setVisibility(View.GONE);
            iv_failed.setVisibility(View.GONE);
            iv_fingerprint.setVisibility(View.VISIBLE);

            //rb_animation.startRippleAnimation();
        }
    }

    private boolean m_reset = false;
    private Reader.CaptureResult cap_result = null;
    private Fmd searchFMD = null;

    private User fingerPrintOwner;

    private void runThread() {

        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_reset = false;
                while (!m_reset) {

                    try {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                    } catch (Exception e) {
                        if (!m_reset) {
                            Log.w("UareUSampleJava", "error during capture: " + e.toString());
                            m_deviceName = "";
                            //onBackPressed();
                        }
                    }

                    // an error occurred
                    if (cap_result == null || cap_result.image == null) continue;

                    Result r = null;
                    try {
                        searchFMD = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                        r = viewModel.findUserFMD(searchFMD);

                        if (r.getStatus().equals("success")) {

                            setBooleans(false, false, true, false);
                            fingerPrintOwner = tkviewModel.findUserByUserID(r.getMessage());
                        } else {
                            setBooleans(false, false, false, true);
                        }

                        tv_debugger_text = r.getMessage();
                    } catch (Exception e) {
                        setBooleans(false, false, false, true);
                        tv_debugger_text = e.toString();
                        Log.w("UareUSampleJava", "Engine error: " + e.toString());
                    }

                    final Result finalR = r;
                    Act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateGUI();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            m_reset = true;
            try {
                m_reader.CancelCapture();
            } catch (Exception e) {
                Toast.makeText(applContext, e.toString(), Toast.LENGTH_LONG).show();
            }
            m_reader.Close();
        } catch (Exception e) {

            Log.w("UareUSampleJava", "error during reader shutdown");
            Toast.makeText(applContext, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
