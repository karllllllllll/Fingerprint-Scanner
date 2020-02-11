package com.karl.fingerprintmodule.Fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.digitalpersona.uareu.Reader;
import com.karl.fingerprintmodule.Activities.MainActivity;
import com.karl.fingerprintmodule.CaptureResultObservable;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.fingerprint;

import java.beans.PropertyChangeEvent;

public class RegisterFragment extends Fragment {

    private Context ctx;
    private FmdViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_register_activity, container, false);
        ctx = v.getContext();
        viewModel = ViewModelProviders.of(getActivity()).get(FmdViewModel.class);

        getUser();
        initViews(v);
        setListeners();

        tv_name.setText(user.getF_name() + " " + user.getL_name());
        Glide.with(this)
                .load(user.getImage_path())
                .apply(RequestOptions
                        .circleCropTransform())
                .into(iv_avatar);

        return v;
    }

    private User user;

    private void getUser() {
        try {
            user = (User) getArguments().getSerializable("user");

        } catch (Exception e) {

            Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private ImageView iv_thumbprint;
    private ImageView iv_avatar;
    private TextView tv_message;
    private TextView tv_name;
    private Button btn_register;
    private Button btn_delete;
    //private Button btn_find;
    private TextView tv_debugger;

    private void initViews(View v) {

        iv_thumbprint = v.findViewById(R.id.iv_thumbprint);
        iv_avatar = v.findViewById(R.id.iv_avatar);
        tv_message = v.findViewById(R.id.tv_message);
        tv_name = v.findViewById(R.id.tv_name);
        btn_register = v.findViewById(R.id.btn_register);
        btn_delete = v.findViewById(R.id.btn_delete);
        btn_register.setEnabled(false);
        //btn_find = findViewById(R.id.btn_find);
        tv_debugger = v.findViewById(R.id.tv_debugger);
    }

    private Boolean userHasFingerprint = false;

    private Reader.CaptureResult cap_result;

    private void setListeners() {

        try {

            cap_result = Globals.captureResultObservable.getCaptureResult();

            if (cap_result == null){
                tv_message.setText("Place your finger on top of the device");
            } else {
                Globals.captureResultObservable.setCaptureResult(null);
            }


            Globals.captureResultObservable.setListener(
                    new CaptureResultObservable.ChangeListener() {
                        @Override
                        public void onChange() {
                            Activity act = getActivity();

                            if (act != null) {

                                act.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        cap_result = Globals.captureResultObservable.getCaptureResult();

                                        final Bitmap m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                                        if (m_bitmap != null) {
                                            iv_thumbprint.setImageBitmap(m_bitmap);
                                            iv_thumbprint.invalidate();

                                        } else {
                                            Toast.makeText(getContext(), "Could not create image", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
        }


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = user.getId();

                if (!name.isEmpty() && cap_result != null && cap_result.image != null) {

                    Bitmap m_bitmap = Globals.GetBitmapFromRaw(
                            cap_result.image.getViews()[0].getImageData(),
                            cap_result.image.getViews()[0].getWidth(),
                            cap_result.image.getViews()[0].getHeight());
                    iv_thumbprint.setImageBitmap(m_bitmap);

                    // Convert byte[] to String for network transfer
                    String encoded = Base64.encodeToString(cap_result.image.getViews()[0].getImageData(), Base64.DEFAULT);
                    //Decodes converted string back
                    byte[] data = Base64.decode(encoded, Base64.DEFAULT);

                    if (userHasFingerprint) {

                        viewModel.updateFingerprint(user.getId(), encoded);
                    } else {

                        viewModel.saveFingerprint(
                                new fingerprint(
                                        name,
                                        encoded,
                                        cap_result.image.getViews()[0].getWidth(),
                                        cap_result.image.getViews()[0].getHeight(),
                                        cap_result.image.getImageResolution(),
                                        cap_result.image.getCbeffId()
                                )
                                , user
                        );
                    }
                } else {
                    tv_message.setText("Please fill all fields");
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.removeFingerprint(user.getId());
            }
        });

        viewModel.getUserFingerprint(user.getId());
        viewModel.getUserHasFingerint().

                observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {

                        userHasFingerprint = aBoolean;
                        btn_register.setEnabled(true);

                        if (aBoolean) {
                            btn_delete.setVisibility(View.VISIBLE);
                            btn_register.setText("Change Fingerprint");
                        } else {
                            btn_delete.setVisibility(View.GONE);
                            btn_register.setText("Register Fingerprint");
                        }
                    }
                });
    }
}
