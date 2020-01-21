package com.karl.fingerprintmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader.CaptureResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.karl.fingerprintmodule.R.layout.activity_engine;

public class IdentificationActivity extends Activity {
    private Button m_back;
    private String m_deviceName = "";

    private String m_enginError;
    private Reader m_reader = null;
    private int m_DPI = 0;
    private Bitmap m_bitmap = null;
    private Bitmap m_bitmap_2 = null;
    private ImageView m_imgView;
    private ImageView m_imgView2;
    private TextView m_selectedDevice;
    private TextView m_title;
    private boolean m_reset = false;


    private TextView m_text;
    private TextView m_text_conclusion;
    private String m_textString;
    private String m_text_conclusionString;
    private Engine m_engine = null;
    private Engine.Candidate[] results = null;
    private Fmd m_fmd1 = null;
    private Fmd m_fmd2 = null;
    private Fmd m_fmd3 = null;
    private Fmd m_fmd4 = null;
    private boolean m_first = true;
    private int m_score = -1;
    private CaptureResult cap_result = null;


    private Button btn_check;

    private void initializeActivity() {
        m_title = (TextView) findViewById(R.id.title);
        m_title.setText("Identification");

        m_enginError = "";

        m_selectedDevice = (TextView) findViewById(R.id.selected_device);
        m_deviceName = getIntent().getExtras().getString("device_name");

        m_selectedDevice.setText("Device: " + m_deviceName);

        m_imgView = (ImageView) findViewById(R.id.bitmap_image);
        m_imgView2 = (ImageView) findViewById(R.id.bitmap_image_2);
        m_bitmap = Globals.GetLastBitmap();

        if (m_bitmap == null)
            m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);

        if (m_bitmap_2 == null)
            m_bitmap_2 = BitmapFactory.decodeResource(getResources(), R.drawable.black);

        m_imgView.setImageBitmap(m_bitmap);
        m_imgView2.setImageBitmap(m_bitmap);
        m_back = (Button) findViewById(R.id.back);


        m_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        m_text = (TextView) findViewById(R.id.text);
        m_text_conclusion = (TextView) findViewById(R.id.text_conclusion);

        btn_check = (Button) findViewById(R.id.btn_check);
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check = true;
            }
        });


        userBiometrixList = retrieveData();
        //String name = userBiometrixList.get(1).getName();
        UpdateGUI();
        //createTestData();
    }

    private Boolean check = false;
    private ArrayList<userBiometrix> userBiometrixList = new ArrayList<>();
    Fmd[] m_fmds_temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_engine);
        m_textString = "Place your thumb on the reader";
        initializeActivity();
        // initiliaze dp sdk
        try {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
            m_reader.Open(Reader.Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();

        } catch (Exception e) {
            Log.w("UareUSampleJava", "error during init of reader");
            m_deviceName = "";
            onBackPressed();
            return;
        }


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
                        m_enginError = "";

                        // save bitmap image locally
                        //if (m_fmd1 == null)
                        if (!check) {
                            //Image
                            m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                            //FMD Object
                            m_fmd1 = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                            //fmdArrayList.add(m_fmd1);
                            //addData(new userBiometrix("Karl", m_fmd1));

                            createTestData();

                        } else {
                            m_bitmap_2 = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                            //FINGERPRINT FOR CHECKING
                            Fmd m_temp = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                            m_fmds_temp = createFMDCheckList();
                            //m_fmds_temp = new Fmd[] {m_fmd1, m_fmd1, m_fmd1, m_fmd1};

                            //Returns Array of Candidate Object
                            results = m_engine.Identify(m_temp, 0, m_fmds_temp, 100000, 2);

                            if (results.length != 0) {
                                m_score = m_engine.Compare(m_fmds_temp[results[0].fmd_index], 0, m_temp, 0);
                            } else {
                                m_score = -1;
                            }
                            m_fmd1 = null;
                            m_fmd2 = null;
                            m_fmd3 = null;
                            m_fmd4 = null;
                        }
                    } catch (Exception e) {


                        m_enginError = e.toString();
                        Log.w("UareUSampleJava", "Engine error: " + e.toString());
                    }

                    m_text_conclusionString = Globals.QualityToString(cap_result);

                    if (!m_enginError.isEmpty()) {
                        m_text_conclusionString = "Engine: " + m_enginError;
                    }
                    if (m_fmd1 == null) {
                        if (!m_first) {
                            if (m_text_conclusionString.length() == 0) {
                                String conclusion = "";
                                if (results.length > 0) {

                                    //conclusion = fmdArrayList.get(results[0].fmd_index).toString();
                                    conclusion = userBiometrixList.get(results[0].fmd_index).getName();

                                } else {
                                    conclusion = "No match found";
                                }
                                m_text_conclusionString = conclusion;
                                if (m_score != -1) {
                                    DecimalFormat formatting = new DecimalFormat("##.######");
                                    m_text_conclusionString = m_text_conclusionString + " (Dissimilarity Score: " + String.valueOf(m_score) + ", False match rate: " + Double.valueOf(formatting.format((double) m_score / 0x7FFFFFFF)) + ")";
                                }
                            }
                        }

                        m_textString = "Place your thumb on the reader";
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


    public void UpdateGUI() {
        m_imgView.setImageBitmap(m_bitmap);
        m_imgView2.setImageBitmap(m_bitmap_2);
        m_imgView.invalidate();
        m_text_conclusion.setText(m_text_conclusionString);
        m_text.setText(m_textString);


        String s = "";

//        for (userBiometrix item : userBiometrixList) {
//            s = m_title.getText() + item.getName() + "\n";
//        }

        if (m_fmds_temp != null) {
            s = String.valueOf(m_fmds_temp.length);
        }

        m_title.setText(s);
    }


    @Override
    public void onBackPressed() {
        try {
            m_reset = true;
            try {
                m_reader.CancelCapture();
            } catch (Exception e) {
            }
            m_reader.Close();
        } catch (Exception e) {
            Log.w("UareUSampleJava", "error during reader shutdown");
        }

        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    // called when orientation has changed to manually destroy and recreate activity
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(activity_engine);
        initializeActivity();
    }


    File dir = Environment.getExternalStorageDirectory();
    //String location = dir.getAbsolutePath() + "/Thumbprints/employeeBiometrix";
    String location = dir + "/Thumbprints/employeeBiometrix.txt";

    private void addData(userBiometrix ub) {

        ArrayList<userBiometrix> employees = retrieveData();
        employees.add(ub);
        employees.add(ub);
        employees.add(ub);
        employees.add(ub);
        employees.add(ub);

        try {
            FileOutputStream fos = new FileOutputStream(location);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(employees);
            oos.close();
            fos.close();
        } catch (IOException ioe) {

            m_text_conclusionString = ioe.toString();
            UpdateGUI();
            ioe.printStackTrace();
        }
    }


    private ArrayList<userBiometrix> retrieveData() {

        ArrayList<userBiometrix> innerList = new ArrayList<>();

        try {
            FileInputStream fis = new FileInputStream(location);
            ObjectInputStream ois = new ObjectInputStream(fis);
            innerList = (ArrayList<userBiometrix>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {

            m_text_conclusionString = ioe.toString();
            return innerList;
        } catch (ClassNotFoundException c) {

            m_text_conclusionString = c.toString();
            return innerList;
        } finally {

            UpdateGUI();
            return innerList;
        }
    }


    private Fmd[] createFMDCheckList() {

        Fmd[] innerList;

        if (!userBiometrixList.isEmpty()) {

            int index = 0;
            innerList = new Fmd[userBiometrixList.size()];

            for (userBiometrix ub : userBiometrixList) {

                innerList[index] = ub.getFingerPrint();
                index++;
            }
        } else {

            innerList = new Fmd[0];
        }

        return innerList;
    }

    private void createTestData() {

        ArrayList<userBiometrix> employees = new ArrayList<>();
        employees.add(new userBiometrix("karl", m_fmd1));
        employees.add(new userBiometrix("karl", m_fmd1));
        employees.add(new userBiometrix("karl", m_fmd1));
        employees.add(new userBiometrix("karl", m_fmd1));
        employees.add(new userBiometrix("karl", m_fmd1));

        try {

            FileOutputStream fos = new FileOutputStream(location);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(employees);
            oos.close();
            fos.close();
        } catch (IOException ioe) {

            ioe.printStackTrace();
        }
    }

    private class userBiometrix implements Serializable {
        transient String name;
        transient Fmd fingerPrint;

        userBiometrix(String name, Fmd fmd){
            this.name = name;
            this.fingerPrint = fmd;
        }

        public String getName() {
            return this.name;
        }

        public Fmd getFingerPrint() {
            return this.fingerPrint;
        }
    }
}

