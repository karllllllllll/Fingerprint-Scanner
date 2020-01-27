package com.karl.fingerprintmodule.Activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Result;
import com.skyfishjy.library.RippleBackground;


public class TimeInActivity extends AppCompatActivity {

    private FmdViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_in);


        initViews();
        initReader();

        viewModel = ViewModelProviders.of(this).get(FmdViewModel.class);
        viewModel.retrieveFingerPrints();
        setBooleans(true, false, false, false);
        UpdateGUI();

        viewModel.getfmdListConversionResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(@Nullable Result result) {
                if (result.getStatus().equals("success")) {

                    tv_debugger_text = "Place your thumb on top of the device";
                    setBooleans(false, false, false, false);
                    UpdateGUI();
                    runThread();
                } else {

                    tv_debugger_text = "";
                    setBooleans(false, false, false, true);
                    showRetryDialog();
                }
            }
        });

//        runThread();
    }

    private void showRetryDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Oops")
                .setMessage("Something went wrong!\nRetry?")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        viewModel.retrieveFingerPrints();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
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

    private ProgressBar pb_loading;
    private ImageView iv_success;
    private ImageView iv_failed;
    private ImageView iv_fingerprint;
    private TextView tv_message;
    private RippleBackground rb_animation;

    private void initViews() {

        pb_loading = findViewById(R.id.pb_loading);
        tv_message = findViewById(R.id.tv_message);
        iv_success = findViewById(R.id.iv_success);
        iv_failed = findViewById(R.id.iv_failed);
        iv_fingerprint = findViewById(R.id.iv_fingerprint);

        rb_animation = findViewById(R.id.rb_animation);
    }

    private String m_deviceName;
    private String m_enginError;
    private Reader m_reader = null;
    private int m_DPI = 0;
    private Engine m_engine = null;
    private Engine.Candidate[] results = null;

    private void initReader() {
        try {
            Context applContext = getApplicationContext();

            m_deviceName = getIntent().getExtras().getString("device_name");
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);

            m_reader.Open(Reader.Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();

        } catch (Exception e) {

            onBackPressed();
            return;
        }
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

//            rb_animation.startRippleAnimation();
        }
    }

    private boolean m_reset = false;
    private Reader.CaptureResult cap_result = null;
    private Fmd searchFMD = null;


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
                            onBackPressed();
                        }
                    }

                    // an error occurred
                    if (cap_result == null || cap_result.image == null) continue;

                    try {
                        searchFMD = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                        Result r = viewModel.findUserFMD(searchFMD);

                        if (r.getStatus().equals("success")) {
                            setBooleans(false, false, true, false);
                        } else {
                            setBooleans(false, false, false, true);
                        }

                        tv_debugger_text = r.getMessage();

                    } catch (Exception e) {
                        setBooleans(false, false, false, true);
                        tv_debugger_text = e.toString();
                        Log.w("UareUSampleJava", "Engine error: " + e.toString());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateGUI();
                        }
                    });
                }
            }
        }).start();
    }
}

