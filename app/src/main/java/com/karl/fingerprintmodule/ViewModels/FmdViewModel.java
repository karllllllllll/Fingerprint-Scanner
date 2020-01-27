package com.karl.fingerprintmodule.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.fingerprint;
import com.karl.fingerprintmodule.volleyQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FmdViewModel extends AndroidViewModel {

    private RequestQueue queue;
    private Application app;
    private Engine m_engine = UareUGlobal.GetEngine();

    public FmdViewModel(@NonNull Application application) {
        super(application);

        app = application;
        queue = volleyQueue.getInstance(application.getBaseContext()).
                getRequestQueue();
    }

    private String url = "http://192.168.137.1/Biometrix/api/fmds";

    public void saveFingerprint(fingerprint fp) {

        Map<String, String> params = new HashMap<>();
        params.put("userID", fp.getUserID());
        params.put("data", fp.getData());
        params.put("width", String.valueOf(fp.getWidth()));
        params.put("height", String.valueOf(fp.getHeight()));
        params.put("resolution", String.valueOf(fp.getResolution()));
        params.put("cbeff_id", String.valueOf(fp.getCbeff_id()));
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Toast.makeText(app.getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(app.getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });


        queue.add(jsonObjectRequest);
    }

    public void retrieveFingerPrints() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        createFMDList(response);
                        //Toast.makeText(app.getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(app.getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
                        getfmdListConversionResult().setValue(new Result("failed", error.toString()));
                    }
                });

        queue.add(jsonObjectRequest);
    }

    private Fmd[] fmdChecklist;
    private String[] uidList;
    private MutableLiveData<Result> fmdListConversionResult = new MutableLiveData<>();

    public MutableLiveData<Result> getfmdListConversionResult() {

        return this.fmdListConversionResult;
    }

    private void createFMDList(JSONObject jo) {

        try {
            JSONArray fingerPrintsArray = jo.getJSONArray("msg");
            Fmd[] innerFMDList = new Fmd[fingerPrintsArray.length()];
            String[] innerIDList = new String[fingerPrintsArray.length()];

            if (fingerPrintsArray.length() > 0) {

                for (int i = 0; i < fingerPrintsArray.length(); i++) {

                    JSONObject fingerPrintsObj = fingerPrintsArray.getJSONObject(i);

                    byte[] data = Base64.decode(fingerPrintsObj.getString("data"), Base64.DEFAULT);

                    Fmd fmd = m_engine.CreateFmd(
                            data,
                            fingerPrintsObj.getInt("width"),
                            fingerPrintsObj.getInt("height"),
                            fingerPrintsObj.getInt("resolution"),
                            0,
                            fingerPrintsObj.getInt("cbeff_id"),
                            Fmd.Format.ANSI_378_2004);

                    innerFMDList[i] = fmd;
                    innerIDList[i] = fingerPrintsObj.getString("userID");
                }

                fmdChecklist = innerFMDList;
                uidList = innerIDList;

                getfmdListConversionResult().setValue(new Result("success", ""));
                //Toast.makeText(app.getBaseContext(), Arrays.toString(uidList), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();

            Toast.makeText(app.getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            getfmdListConversionResult().setValue(new Result("failed", ""));
        } catch (UareUException e) {
            e.printStackTrace();

            Toast.makeText(app.getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            getfmdListConversionResult().setValue(new Result("failed", ""));
        }
    }

    private int m_score = -1;

    public Result findUserFMD(Fmd searchFMD) {

        Result result = new Result("", "");

        try {
            Engine.Candidate[] results = m_engine.Identify(searchFMD, 0, fmdChecklist, 100000, 2);

            if (results.length != 0) {

                result.setStatus("success");
                m_score = m_engine.Compare(fmdChecklist[results[0].fmd_index], 0, searchFMD, 0);

                String message = uidList[results[0].fmd_index];
                if (m_score != -1) {

                    DecimalFormat formatting = new DecimalFormat("##.######");
                    result.setMessage(message + "\n (Dissimilarity Score: " + m_score + ", False match rate: " + Double.valueOf(formatting.format((double) m_score / 0x7FFFFFFF)) + ")");
                } else {
                    result.setMessage(message);
                }
            } else {
                m_score = -1;

                result.setStatus("failed");
                result.setMessage("No Match Found");
            }
        } catch (UareUException e) {
            e.printStackTrace();
            result.setStatus("failed");
            result.setMessage(e.toString());
        }

        return result;
    }
}
