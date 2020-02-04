package com.karl.fingerprintmodule;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.util.TypedValue;

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

    //Returns current date
    public String today() {

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    //Returns current time
    public String now() {

        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public float integerToDP(int i) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, ctx.getResources().getDisplayMetrics());
    }

    public CircularProgressDrawable getCircleAnimation() {

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(ctx);
        int[] cs = new int[1];
        cs[0] = ctx.getResources().getColor(R.color.black);
        circularProgressDrawable.setColorSchemeColors(cs);
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        return circularProgressDrawable;
    }


    public String convertToReadableDate(String date) {

        String string_date = date;
        String converted_date = "--- --, ----";
        SimpleDateFormat raw_date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat date_day = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try {
            Date raw_dateDt = raw_date.parse(string_date);
            converted_date = date_day.format(raw_dateDt);
            Log.d("@date_with_day", converted_date);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return converted_date;
    }


    public String convertToReadableTime(String time) {

        String string_time = time;
        String converted_time = "--:--:-- --";
        SimpleDateFormat _24Hour = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat _readableFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        if (!time.isEmpty()) {
            try {
                Date _24HourDt = _24Hour.parse(string_time);
                converted_time = _readableFormat.format(_24HourDt);
                Log.d("@12Hr_format", converted_time);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        return converted_time.replace("a.m.", "AM").replace("p.m.", "PM");
    }

    public String convertToFormattedTime(String time, String input_time_pattern, String pattern) {

        String string_time = time;
        String converted_time = "--:--:-- --";
        SimpleDateFormat _24Hour = new SimpleDateFormat(input_time_pattern, Locale.getDefault());
        SimpleDateFormat _readableFormat = new SimpleDateFormat(pattern, Locale.getDefault());

        if (!time.isEmpty()) {
            try {
                Date _24HourDt = _24Hour.parse(string_time);
                converted_time = _readableFormat.format(_24HourDt);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        return converted_time.replace("a.m.", "AM").replace("p.m.", "PM");
    }

}
