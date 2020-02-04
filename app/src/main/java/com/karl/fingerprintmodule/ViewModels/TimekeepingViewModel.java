package com.karl.fingerprintmodule.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karl.fingerprintmodule.Helper;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.Session;
import com.karl.fingerprintmodule.SharedPref.SharedPreferenceManager;
import com.karl.fingerprintmodule.Static;
import com.karl.fingerprintmodule.UserEDTR;
import com.karl.fingerprintmodule.volleyQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimekeepingViewModel extends AndroidViewModel {


    //PRE REPLACE MO LAHAT STATIC ZOLVERE
    private Application app;
    private RequestQueue queue;
    private Helper helper;
    private SharedPreferenceManager sharedPreferenceManager;
    private Session session;

    private MutableLiveData<ArrayList<User>> userArrayList = new MutableLiveData<>();


    public TimekeepingViewModel(@NonNull Application application) {
        super(application);

        this.app = application;
        Context ctx = application.getBaseContext();

        this.helper = Helper.getInstance(ctx);

        this.queue = volleyQueue.getInstance(ctx).
                getRequestQueue();

        sharedPreferenceManager = SharedPreferenceManager.getInstance(ctx);

        session = SharedPreferenceManager.getInstance(ctx).getSessions();

    }

    public String deviceName = "";

    public MutableLiveData<ArrayList<User>> getUserArrayList() {

        return this.userArrayList;
    }

    private MutableLiveData<Result> LoginResult = new MutableLiveData<>();

    public MutableLiveData<Result> getLoginResult() {
        return this.LoginResult;
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
                        LoginResult.setValue(new Result(Static.API_STATUS_FAILED, "Login Error!\n" + error.toString()));
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
                //this.token = token;

                //CAST all first value of JSONARRAYS AS JSONObject
                JSONObject tenant_info_object = tenant_info_array.getJSONObject(0);
                JSONObject user_object = user_array.getJSONObject(0);
                JSONObject user_branch_object = user_branch_array.getJSONObject(0);

                //GET VALUES database,table,api_token,location_id
                String d = tenant_info_object.get("database").toString();
                //this.d = d;

                String t = tenant_info_object.get("tbl").toString();
                //this.t = t;

                String company_name = tenant_info_object.get("company_name").toString();

                String api_token = user_object.get("api_token").toString();
                //this.api_token = api_token;

                String timekeeper_id = user_object.get("user_id").toString();

                JSONArray user_name_array = user_object.getJSONArray("name");
                JSONObject user_name_object = user_name_array.getJSONObject(0);
                String fname = user_name_object.getString("fname");
                String lname = user_name_object.getString("lname");

                String full_name = fname + " " + lname;

                String first_branch_id = user_branch_object.getString("branch_id");


                Session s = new Session(
                        first_branch_id,
                        user_branch_array.toString(),
                        link, d, t,
                        api_token, token);

                sharedPreferenceManager.timekeeperLogin(s);

                LoginResult.setValue(new Result(Static.API_STATUS_SUCCESS, "Login Successful!"));
            } else {
                LoginResult.setValue(new Result(Static.API_STATUS_FAILED, response.toString()));
            }
        } catch (Exception e) {
            LoginResult.setValue(new Result(Static.API_STATUS_FAILED, "Login Session Error!\n" + e.toString()));
        }
    }

    public void getUsers() {

        String url = Static.URL_EMPLOYEES + session.getSelectedLocationID() + "?api_token=" + session.getApi_token() + "&link=" + session.getLink();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        addToList(response, session.getApi_token(), session.getLink());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(app.getBaseContext(), "Users Error!\n" + error.toString(), Toast.LENGTH_LONG).show();
                        userArrayList.setValue(null);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                params.put("d", session.getD());
                params.put("t", session.getT());
                params.put("token", session.getToken());

                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, Static.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private void addToList(JSONObject response, String api_token, String link) {

        ArrayList<User> innerList = new ArrayList<>();

        try {

            if (response.has("status") && response.getString("status").equals("success")) {


                JSONArray jsonArray = response.getJSONArray("msg");


                for (int i = 0; i < jsonArray.length(); i++) {

                    try {

                        JSONObject person = jsonArray.getJSONObject(i);

                        JSONObject employee = person.getJSONObject("employee");
                        String id = person.get("user_id").toString();
                        String f_name = employee.getString("fname");
                        String l_name = employee.getString("lname");
                        String image_file_name = employee.getString("image");


                        String emp_pin = person.getString("emp_pin");

                        String image_path = image_file_name.equals("Photo.png") ? "http://" + Static.TK_IP + "/adminbackend/public/assets/" + image_file_name :
                                "http://" + Static.TK_IP + "/adminbackend/public/assets/zolvere/images/users/" + image_file_name;


                        //EDTR
                        String default_value = "null";

                        String edtr_time_in = default_value;
                        String edtr_time_out = default_value;
                        String edtr_date = default_value;

                        JSONArray edtr_array = person.getJSONArray("edtr");
                        if (edtr_array.length() > 0) {
                            JSONObject employee_edtr = edtr_array.getJSONObject(0);
                            edtr_date = employee_edtr.getString("date_in");

                            if (employee_edtr.getString("time_out").equals(default_value) && edtr_date.equals(helper.today())) {
                                edtr_time_in = employee_edtr.get("time_in").toString();
                            }

                            if (!employee_edtr.getString("time_out").equals(default_value) && edtr_date.equals(helper.today())) {
                                edtr_time_in = employee_edtr.getString("time_in");
                                edtr_time_out = employee_edtr.getString("time_out");
                            }
                        }

                        innerList.add(new User(
                                id,
                                f_name,
                                l_name,
                                image_path,
                                emp_pin,
                                api_token,
                                link,
                                edtr_time_in,
                                edtr_time_out,
                                edtr_date
                        ));

                    } catch (Exception e) {
                        Toast.makeText(app.getBaseContext(), "User Parse Error!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(app.getBaseContext(), response.toString(), Toast.LENGTH_LONG).show();
                innerList = null;
            }
        } catch (Exception e) {

            String msg = e.toString();

            try {
                if (response.has("msg")) {
                    msg = response.getString("msg");
                }
            } catch (JSONException ex) {
                msg = ex.toString();
            }

            Toast.makeText(app.getBaseContext(), msg, Toast.LENGTH_LONG).show();
            innerList = null;
        }

        Toast.makeText(app.getBaseContext(), "User List Ready!", Toast.LENGTH_LONG).show();
        userArrayList.setValue(innerList);
    }

    public User findUserFromPosition(int i) {

        if (userArrayList.getValue() != null)
            return userArrayList.getValue().get(i);
        else
            return null;
    }

    private MutableLiveData<Result> findUserResult = new MutableLiveData<>();

    public MutableLiveData<Result> getFindUserResult() {
        return this.findUserResult;
    }

    public User findUserByUserID(String userID) {

        ArrayList<User> innerList = userArrayList.getValue();
        User user = null;

        if (innerList != null) {

            findUserResult.postValue(new Result(Static.API_STATUS_SUCCESS, innerList.toString()));

            for (User u : innerList) {
                if (u.getId().equals(userID)) {
                    user = u;
                }
            }
        }

        if (user != null) {

            findUserResult.postValue(new Result(Static.API_STATUS_SUCCESS, user.getId()));
        } else {
            findUserResult.postValue(new Result(Static.API_STATUS_FAILED, "Something went wrong"));
        }

        return user;
    }

    public void sendClockInOut(final User owner) {

        final String time = helper.now();
        final String date = helper.today();

        final HashMap<String, String> params = new HashMap<>();
        params.put("user_id", owner.getId());
        params.put("pin", owner.getPin());
        params.put("date", date);
        params.put("time", time);
        params.put("location", "");
        params.put("reference", Static.reference);
        params.put("link", session.getLink());
        params.put("api_token", session.getApi_token());

        final HashMap<String, String> headers = new HashMap<>();
        headers.put("d", session.getD());
        headers.put("t", session.getT());
        headers.put("token", session.getToken());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Static.URL_CLOCK_IN, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        clockInResult(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                        addPendingUpdate(new UserEDTR(owner.getId(), time, Static.reference, owner.getPin(), date, ""));
                        clockResult.setValue(new Result(Static.API_STATUS_FAILED, "Your time in was saved"));

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(Static.DEFAULT_TIMEOUT_MS, Static.DEFAULT_MAX_RETRIES, Static.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }


    private MutableLiveData<Result> clockResult = new MutableLiveData<>();

    public MutableLiveData<Result> getClockResult() {
        return this.clockResult;
    }

    private void clockInResult(JSONObject jsonObj) {

        try {

            String status = jsonObj.getString("status");
            String msg = jsonObj.getString("msg");

            if (status.equals(Static.API_STATUS_SUCCESS)) {

                clockResult.setValue(new Result(Static.API_STATUS_SUCCESS, "Time in saved!"));
            } else {
                clockResult.setValue(new Result(Static.API_STATUS_FAILED, msg));
            }
        } catch (Exception e) {
            clockResult.setValue(new Result(Static.API_STATUS_FAILED, "Oops! Something went wrong"));
        }
    }

    private void addPendingUpdate(UserEDTR edtr) {

        try {
            JSONArray pendingUpdates = new JSONArray(sharedPreferenceManager.getPendingUpdates());

            HashMap<String, String> pu = new HashMap<>();
            pu.put("user_id", edtr.getUser_id());
            pu.put("time", edtr.getTime());
            pu.put("reference", edtr.getReference());
            pu.put("pin", edtr.getPin());
            pu.put("date", edtr.getDate());
            pu.put("location_id", edtr.getLocation_id());

            pendingUpdates.put(new JSONObject(pu));

            sharedPreferenceManager.setPendingUpdates(pendingUpdates.toString());

//            if (pendingUpdates.length() > 0) {
//
//                for (int i = 0; i < pendingUpdates.length(); i++) {
//
//                    JSONObject jo = pendingUpdates.getJSONObject(i);
//
//                    HashMap pendingUpdateHashMap = new Gson().fromJson(jo.toString(), HashMap.class);
//                    pendingUpdateHashMap.put("")
//                }
//            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
