package com.karl.fingerprintmodule;

import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

public class Static {

    private static String IP = "192.168.137.1";
    public static String TK_IP = "192.168.137.1/timekeeping_new/tk";
    //public static String TK_IP = "timekeeping.caimitoapps.com:8081";

    public static String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    public static String URL_BIOMETRIX = "http://" + IP + "/Biometrix/api/fmds";
    public static String URL_LOGIN = "http://" + TK_IP + "/clock/api/kiosk-login";
    public static String URL_EMPLOYEES = "http://" + TK_IP + "/adminbackend/api/location/employee/";
    public static String URL_PENDING_UPDATES = "http://" + TK_IP + "/clock/api/kiosk";

    public static String URL_CLOCK_IN = "http://" + TK_IP + "/clock/api/check";
    public static String reference = "web_kiosk";

    public static final int DEFAULT_TIMEOUT_MS = 30 * 1000;
    public static final int DEFAULT_MAX_RETRIES = -1;
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public static String API_STATUS_SUCCESS = "success";
    public static String API_STATUS_FAILED = "failed";

    public static String BLANK_TIME = "--:--:-- --";

    public static String JSON_BLANK_VALUE = "null";
}
