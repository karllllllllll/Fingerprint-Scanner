package com.karl.fingerprintmodule;

public class Static {

    private static String IP = "192.168.137.1";
    public static String IP_LIVE = "timekeeping.caimitoapps.com:8081";

    public static String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    public static String URL_BIOMETRIX = "http://" + IP + "/Biometrix/api/fmds";
    public static String URL_LOGIN = "http://" + IP_LIVE + "/clock/api/kiosk-login";
    public static String URL_EMPLOYEES = "http://" + IP_LIVE + "/adminbackend/api/location/employee/";
}
