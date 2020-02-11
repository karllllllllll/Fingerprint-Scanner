package com.karl.fingerprintmodule.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Session;
import com.karl.fingerprintmodule.SharedPref.SharedPreferenceManager;
import com.skyfishjy.library.RippleBackground;

public class ScannerReadyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scanner_ready, container, false);
        initViews(v);
        //rb_animation.startRippleAnimation();
        setListeners();
        return v;
    }

    private ProgressBar pb_loading;
    private ImageView iv_success;
    private ImageView iv_failed;
    private ImageView iv_fingerprint;
    private TextView tv_message;
    private RippleBackground rb_animation;
    private ImageView iv_more;

    private void initViews(View v) {

        pb_loading = v.findViewById(R.id.pb_loading);
        tv_message = v.findViewById(R.id.tv_message);
        iv_success = v.findViewById(R.id.iv_success);
        iv_failed = v.findViewById(R.id.iv_failed);
        iv_fingerprint = v.findViewById(R.id.iv_fingerprint);

        rb_animation = v.findViewById(R.id.rb_animation);
        iv_more = v.findViewById(R.id.iv_more);
    }

    private void setListeners() {
        iv_more.setOnCreateContextMenuListener(this);

        iv_more.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        PopupMenu popupMenu = new PopupMenu(getActivity(), iv_more, Gravity.START);
                        popupMenu.getMenuInflater().inflate(R.menu.settings_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                switch (menuItem.getItemId()) {
                                    case R.id.switch_locations:
                                        break;

                                    case R.id.settings:
                                        break;

                                    case R.id.logout:
                                        SharedPreferenceManager.getInstance(getContext()).timekeeperLogout();
                                        getActivity().onBackPressed();
                                        break;

                                    default:
                                        break;
                                }
                                return false;
                            }
                        });

                        popupMenu.show();

                    }
                }
        );
    }

//    private fun showSelectLocationDialog() {
//        val setLocationDialog = Dialog(this)
//        setLocationDialog.setContentView(R.layout.dialog_timekeeper_location_select)
//
//        val spnnr_locations = setLocationDialog.findViewById < Spinner > (R.id.spnnr_locations)
//                val
//        cv_select_location = setLocationDialog.findViewById < CardView > (R.id.cv_select_location)
//
//                val timekeeperLocations = session.getTimekeeperLocations()
//        val locOptions = ArrayList < String > ()
//        var pos = 0
//
//        repeat(timekeeperLocations.length()) {
//            val branch = timekeeperLocations[pos] as JSONObject
//
//            locOptions.add(branch.getString("branch_name"))
//            pos++
//        }
//
//        val adapter = ArrayAdapter < String > (this, R.layout.layout_spiiner_item_style, locOptions)
//        spnnr_locations.adapter = adapter
//        //Sets default value
//        //spnnr_locations.setSelection(0, false)
//
//        var selectedLocation = ""
//
//        val branch = timekeeperLocations[0] as JSONObject
//        //selectedLocation =
//        spnnr_locations.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent:AdapterView<*>?){
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onItemSelected(parent:AdapterView<*>?,view:
//            View ?, position:Int, id:Long){
//
//                val branch = timekeeperLocations[position] as JSONObject
//
//                selectedLocation = branch.getString("branch_id")
//
//            }
//        }
//    }
}

