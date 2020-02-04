package com.karl.fingerprintmodule.Activities;

import android.arch.lifecycle.Observer;
import android.support.v7.app.AppCompatActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.employeeBiometrix;
import com.karl.fingerprintmodule.fingerprint;

import java.util.ArrayList;


import android.util.Base64;
import android.widget.Toast;


public class RegisterActivity extends AppCompatActivity {

    private AppCompatActivity activity;
    private FmdViewModel viewModel;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_register_activity);

        activity = this;
        viewModel = ViewModelProviders.of(activity).get(FmdViewModel.class);

        user = (User) getIntent().getSerializableExtra("user");

        initViews();

        initReader();
        runThread();

        setListeners();
        viewModel.retrieveFingerPrints();

        tv_name.setText(user.getF_name() + " " + user.getL_name());
        Glide.with(this)
                .load(user.getImage_path())
                .apply(RequestOptions
                        .circleCropTransform())
                .into(iv_avatar);
    }

    private ImageView iv_thumbprint;
    private ImageView iv_avatar;
    private TextView tv_message;
    private TextView tv_name;
    private Button btn_register;
    private Button btn_delete;
    //private Button btn_find;
    private TextView tv_debugger;

    private void initViews() {

        iv_thumbprint = findViewById(R.id.iv_thumbprint);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_message = findViewById(R.id.tv_message);
        tv_name = findViewById(R.id.tv_name);
        btn_register = findViewById(R.id.btn_register);
        btn_delete = findViewById(R.id.btn_delete);
        btn_register.setEnabled(false);
        //btn_find = findViewById(R.id.btn_find);
        tv_debugger = findViewById(R.id.tv_debugger);
    }

    private String m_deviceName;
    private String m_enginError;
    private Reader m_reader = null;
    private int m_DPI = 0;
    private Bitmap m_bitmap = null;
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

            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            //onBackPressed();
            return;
        }
    }

    ArrayList<employeeBiometrix> fmdArrayList = new ArrayList<>();
    ArrayList<Fmd> fmdList = new ArrayList<>();

    private void setListeners() {

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = user.getId();

                if (currentFMD != null && !name.isEmpty()) {

                    fmdArrayList.add(new employeeBiometrix(name, currentFMD));
                    fmdList.add(currentFMD);

                    // Convert byte[] to String for network transfer
                    String encoded = Base64.encodeToString(cap_result.image.getViews()[0].getImageData(), Base64.DEFAULT);
                    //Decodes converted string back
                    byte[] data = Base64.decode(encoded, Base64.DEFAULT);

                    if (userHasFingerprint) {

                        viewModel.updateFingerprint(user.getId(), encoded);
                    } else {

                        viewModel.saveFingerprint(
                                new fingerprint(
                                        name,
                                        encoded,
                                        cap_result.image.getViews()[0].getWidth(),
                                        cap_result.image.getViews()[0].getHeight(),
                                        cap_result.image.getImageResolution(),
                                        cap_result.image.getCbeffId()
                                )
                                , user
                        );
                    }

                    resetRegistration();
                } else {
                    tv_message.setText("Please fill all fields");
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.removeFingerprint(user.getId());
            }
        });

        viewModel.getUserFingerprint(user.getId());
        viewModel.getUserHasFingerint().

                observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {

                        userHasFingerprint = aBoolean;
                        btn_register.setEnabled(true);

                        if (aBoolean) {
                            btn_delete.setVisibility(View.VISIBLE);
                            btn_register.setText("Change Fingerprint");
                        } else {
                            btn_register.setText("Register Fingerprint");
                        }
                    }
                });
    }

    private Boolean userHasFingerprint = false;

    String tv_debugger_text = "";

    private void resetRegistration() {
        tv_message.setText("");

        StringBuilder list = new StringBuilder();

        for (employeeBiometrix eb : fmdArrayList) {
            list.append(eb.getName()).append("\n");
        }

        tv_debugger.setText(list.toString());

        iv_thumbprint.setImageBitmap(null);
        iv_thumbprint.invalidate();
    }

    private void UpdateGUI() {

        iv_thumbprint.setImageBitmap(m_bitmap);
        iv_thumbprint.invalidate();

        tv_debugger.setText(tv_debugger_text);
    }

    private boolean m_reset = false;
    private Reader.CaptureResult cap_result = null;
    private Boolean check = false;
    private int m_score = -1;

    private Fmd currentFMD = null;
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

                    Fmd[] fmdCheckList = null;

                    try {
                        m_enginError = "";

                        //Register Fingerprint
                        if (!check) {

                            //Image
                            m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                            //Create fingerprint FID and format
                            //currentFMD = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                            Fid fid = cap_result.image;

                            // Convert byte[] to String for network transfer
                            String encoded = Base64.encodeToString(fid.getViews()[0].getImageData(), Base64.DEFAULT);
                            //Decodes converted string back
                            byte[] data = Base64.decode(encoded, Base64.DEFAULT);

                            //Create fingerprint FID and format
                            currentFMD = m_engine.CreateFmd(
                                    //data,
                                    fid.getViews()[0].getImageData(),
                                    fid.getViews()[0].getWidth(),
                                    fid.getViews()[0].getHeight(),
                                    fid.getImageResolution(),
                                    0,
                                    fid.getCbeffId(),
                                    Fmd.Format.ANSI_378_2004);
                        }

                        //Eind user
                        else {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resetRegistration();
                                }
                            });

                            searchFMD = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                            Result r = viewModel.findUserFMD(searchFMD);
                            tv_debugger_text = r.getMessage();


//                            fmdCheckList = createFMDArrayFromArrayList();
//
//                            results = m_engine.Identify(searchFMD, 0, fmdCheckList, 100000, 2);
//\
//                            if (results.length != 0) {
//
//                                m_score = m_engine.Compare(fmdCheckList[results[0].fmd_index], 0, searchFMD, 0);
//
//                                String message = fmdArrayList.get(results[0].fmd_index).getName();
//                                if (m_score != -1) {
//
//                                    DecimalFormat formatting = new DecimalFormat("##.######");
//                                    tv_debugger_text = message + "\n (Dissimilarity Score: " + m_score + ", False match rate: " + Double.valueOf(formatting.format((double) m_score / 0x7FFFFFFF)) + ")";
//                                } else {
//                                    tv_debugger_text = message;
//                                }
//                            } else {
//
//                                m_score = -1;
//                                tv_debugger_text = "No Match Found";
//                            }

                            currentFMD = null;
                            searchFMD = null;
                        }

                    } catch (Exception e) {

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

    private Fmd[] createFMDListFromArrayList() {

        Fmd[] innerList = null;

        if (fmdArrayList != null) {

            int i = 0;
            innerList = new Fmd[fmdArrayList.size()];

            for (employeeBiometrix f : fmdArrayList) {
                innerList[i] = f.getFingerPrint();
            }
        }

        return innerList;
    }

    private Fmd[] createFMDArrayFromArrayList() {

        Fmd[] innerList = new Fmd[fmdList.size()];
        fmdList.toArray(innerList);

        return innerList;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            m_reset = true;
            try {
                m_reader.CancelCapture();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            m_reader.Close();
        } catch (Exception e) {

            Log.w("UareUSampleJava", "error during reader shutdown");
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}

