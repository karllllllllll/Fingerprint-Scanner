package com.karl.fingerprintmodule.Activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.Result;
import com.karl.fingerprintmodule.SharedPref.SharedPreferenceManager;
import com.karl.fingerprintmodule.Static;
import com.karl.fingerprintmodule.ViewModels.TimekeepingViewModel;

public class LoginActivity extends AppCompatActivity {

    private TimekeepingViewModel tkviewModel;

    private Button btn_login;
    private EditText et_user_name;
    private EditText et_password;
    private EditText et_link;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Init Shared Preference
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(getApplicationContext());

        if (sharedPreferenceManager.isTimekeeperLoggedIn()) {
            mainIntent();
        }


        tkviewModel = ViewModelProviders.of(this).get(TimekeepingViewModel.class);


        btn_login = findViewById(R.id.btn_login);
        et_user_name = findViewById(R.id.et_user_name);
        et_password = findViewById(R.id.et_password);
        et_link = findViewById(R.id.et_link);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tkviewModel.loginApi(
                        et_user_name.getText().toString(),
                        et_password.getText().toString(),
                        et_link.getText().toString()
                );
            }
        });

        tkviewModel.getLoginResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result.getStatus().equals(Static.API_STATUS_SUCCESS)) {

                    Toast.makeText(getApplicationContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    mainIntent();
                } else {
                    Toast.makeText(getApplicationContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mainIntent() {
        Intent i = new Intent(
                getApplicationContext(),
                MainActivity.class);

        startActivity(i);
    }
}
