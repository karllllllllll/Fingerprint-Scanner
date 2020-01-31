package com.karl.fingerprintmodule.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.karl.fingerprintmodule.Fragments.RegisterFromListFragment;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.RecyclerViewClickListener;

public class RegisterFromListActivity extends AppCompatActivity {

    private FrameLayout fl_user_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_register_list_activity);

        fl_user_list = findViewById(R.id.fl_user_list);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        String m_deviceName = "";

        try {
            m_deviceName = getIntent().getExtras().getString("device_name");

            Bundle b = new Bundle();
            b.putString("device_name", m_deviceName);
            RegisterFromListFragment fragment = new RegisterFromListFragment();
            fragment.setArguments(b);

            fragmentTransaction.replace(R.id.fl_user_list, fragment);
            fragmentTransaction.addToBackStack(null);

            // Commit the transaction
            fragmentTransaction.commit();

        } catch (Exception e) {

        }
    }
}
