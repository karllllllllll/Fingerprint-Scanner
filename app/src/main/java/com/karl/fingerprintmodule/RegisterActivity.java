package com.karl.fingerprintmodule;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;


import android.util.Base64;


public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_fingerprint);

        initViews();
        initReader();

        runThread();
        setListeners();

    }

    private ImageView iv_thumbprint;
    private TextView tv_message;
    private EditText et_name;
    private Button btn_register;
    private Button btn_find;
    private TextView tv_debugger;

    private void initViews() {

        iv_thumbprint = findViewById(R.id.iv_thumbprint);
        tv_message = findViewById(R.id.tv_message);
        et_name = findViewById(R.id.et_name);
        btn_register = findViewById(R.id.btn_register);
        btn_find = findViewById(R.id.btn_find);
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

            onBackPressed();
            return;
        }
    }

    ArrayList<employeeBiometrix> fmdArrayList = new ArrayList<>();
    ArrayList<Fmd> fmdList = new ArrayList<>();

    private void setListeners() {

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = et_name.getText().toString();

                if (currentFMD != null && !name.isEmpty()) {

                    fmdArrayList.add(new employeeBiometrix(name, currentFMD));
                    fmdList.add(currentFMD);

                    resetRegistration();
                } else {
                    tv_message.setText("Please fill all fields");
                }
            }
        });

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check = true;
            }
        });
    }

    String tv_debugger_text = "";

    private void resetRegistration() {
        et_name.setText("");
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


                            //fmdCheckList = createFMDListFromArrayList();
                            fmdCheckList = createFMDArrayFromArrayList();

                            results = m_engine.Identify(searchFMD, 0, fmdCheckList, 100000, 2);

                            if (results.length != 0) {

                                m_score = m_engine.Compare(fmdCheckList[results[0].fmd_index], 0, searchFMD, 0);

                                String message = fmdArrayList.get(results[0].fmd_index).getName();
                                if (m_score != -1) {

                                    DecimalFormat formatting = new DecimalFormat("##.######");
                                    tv_debugger_text = message + "\n (Dissimilarity Score: " + m_score + ", False match rate: " + Double.valueOf(formatting.format((double) m_score / 0x7FFFFFFF)) + ")";
                                } else {
                                    tv_debugger_text = message;
                                }
                            } else {

                                m_score = -1;
                                tv_debugger_text = "No Match Found";
                            }

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
}

