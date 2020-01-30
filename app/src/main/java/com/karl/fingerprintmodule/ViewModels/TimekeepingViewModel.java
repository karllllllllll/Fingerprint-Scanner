package com.karl.fingerprintmodule.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karl.fingerprintmodule.Helper;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.Static;
import com.karl.fingerprintmodule.volleyQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TimekeepingViewModel extends AndroidViewModel {


    //PRE REPLACE MO LAHAT STATIC ZOLVERE
    private Application app;
    private RequestQueue queue;
    private MutableLiveData<ArrayList<User>> userArrayList = new MutableLiveData<>();

    public String deviceName = "";
    private String link = "demo";


    public MutableLiveData<ArrayList<User>> getUserArrayList() {

        return this.userArrayList;
    }

    public TimekeepingViewModel(@NonNull Application application) {
        super(application);

        this.app = application;
        queue = volleyQueue.getInstance(application.getBaseContext()).
                getRequestQueue();
    }

    public void loginApi(String email, String password, final String link) {

        HashMap<String, String> params = new HashMap<>();

        params.put("username", email);
        params.put("password", password);
        params.put("link", link);

        JSONObject joParams = new JSONObject(params);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Static.URL_LOGIN, joParams, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        setSessions(response, link);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                        Toast.makeText(app.getBaseContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }

    private void setSessions(JSONObject response, String link) {

        try {

            if (response.has("status") && response.getString("status").equals("success")) {

                JSONObject msg = response.getJSONObject("msg");


                //CAST JSONObject values AS JSONArray
                JSONArray tenant_info_array = msg.getJSONArray("tenant_info");
                JSONArray user_array = msg.getJSONArray("user");
                JSONArray user_branch_array = msg.getJSONArray("user_branch");
                String token = msg.getString("token");

                //CAST all first value of JSONARRAYS AS JSONObject
                JSONObject tenant_info_object = tenant_info_array.getJSONObject(0);
                JSONObject user_object = user_array.getJSONObject(0);
                JSONObject user_branch_object = user_branch_array.getJSONObject(0);

                //GET VALUES database,table,api_token,location_id
                String d = tenant_info_object.get("database").toString();
                String t = tenant_info_object.get("tbl").toString();
                String company_name = tenant_info_object.get("company_name").toString();

                String api_token = user_object.get("api_token").toString();
                String timekeeper_id = user_object.get("user_id").toString();

                JSONArray user_name_array = user_object.getJSONArray("name");
                JSONObject user_name_object = user_name_array.getJSONObject(0);
                String fname = user_name_object.getString("fname");
                String lname = user_name_object.getString("lname");

                String full_name = fname + " " + lname;

                getUsers(user_branch_object.getString("branch_id"), api_token, link, d, t, token);

            } else {
                Toast.makeText(app.getBaseContext(), "Error!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(app.getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void getUsers(String location_id, String api_token, final String link, final String d, final String t, final String token) {


        String url = Static.URL_EMPLOYEES + location_id + "?api_token=" + api_token + "&link=" + link;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        addToList(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(app.getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                params.put("d", d);
                params.put("t", t);
                params.put("token", token);

                return params;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void addToList(JSONObject response) {


        ArrayList<User> innerList = new ArrayList<>();

        try {

            if (response.has("status") && response.getString("status").equals("success")) {


                JSONArray jsonArray = response.getJSONArray("msg");


                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject person = jsonArray.getJSONObject(i);

                    JSONObject employee = person.getJSONObject("employee");
                    String id = person.get("user_id").toString();
                    String f_name = employee.getString("fname");
                    String l_name = employee.getString("lname");
                    String image_file_name = employee.getString("image");

                    String image_path = image_file_name.equals("Photo.png") ? "http://" + Static.IP_LIVE + "/adminbackend/public/assets/" + image_file_name :
                            "http://" + Static.IP_LIVE + "/adminbackend/public/assets/zolvere/images/users/" + image_file_name;

                    String emp_pin = person.getString("emp_pin");
                    innerList.add(new User(
                            id,
                            f_name,
                            l_name,
                            image_path
                    ));
                }
            } else {
                Toast.makeText(app.getBaseContext(), "Error!", Toast.LENGTH_LONG).show();
                innerList = null;
            }
        } catch (Exception e) {

            String msg =  e.toString();

            try {
                if(response.has("msg")){
                    msg = response.getString("msg");
                }
            } catch (JSONException ex) {
                msg = ex.toString();
            }

            Toast.makeText(app.getBaseContext(), msg, Toast.LENGTH_LONG).show();
            innerList = null;
        }

        Toast.makeText(app.getBaseContext(), innerList.toString(), Toast.LENGTH_LONG).show();
        userArrayList.setValue(innerList);
    }

    public User findUserFromPosition(int i) {

        if (userArrayList.getValue() != null)
            return userArrayList.getValue().get(i);
        else
            return null;
    }

    private MutableLiveData<Result> clockResult = new MutableLiveData<>();

    public MutableLiveData<Result> getClockResult() {
        return this.clockResult;
    }

    public User findUserByUserID(String userID) {

        ArrayList<User> innerList = userArrayList.getValue();
        User user = null;

        if (innerList != null) {

            clockResult.postValue(new Result(Static.API_STATUS_SUCCESS, innerList.toString()));

            for (User u : innerList) {
                if (u.getId().equals(userID)) {
                    user = u;
                }
            }
        }

        if (user != null) {

            clockResult.postValue(new Result(Static.API_STATUS_SUCCESS, user.getId()));

        } else {
            clockResult.postValue(new Result(Static.API_STATUS_FAILED, "Something went wrong"));
        }

        return user;
    }

    //public void sendClockInOut(final HashMap<String, String> params,final HashMap<String, String> headers) {
    public void sendClockInOut(User userID) {

//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.POST, Static.URL_CLOCK_IN, new JSONObject(params), new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//
//                        Toast.makeText(app.getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//                        Toast.makeText(app.getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return params;
//            }
//        };
//
//        queue.add(jsonObjectRequest);
    }

}
