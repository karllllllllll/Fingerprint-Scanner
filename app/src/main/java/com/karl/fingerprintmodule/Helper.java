package com.karl.fingerprintmodule;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {

    private static Helper single_instance = null;

    private Context ctx;

    private Helper(Context i_ctx) {
        this.ctx = i_ctx;
    }

    public static Helper getInstance(Context i_ctx) {
        if (single_instance == null)
            single_instance = new Helper(i_ctx);

        return single_instance;
    }

    public Boolean isNetworkConnected() {

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null;
    }

    public String getDate() {

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
