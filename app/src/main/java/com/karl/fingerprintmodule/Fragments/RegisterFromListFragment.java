package com.karl.fingerprintmodule.Fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Transaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.karl.fingerprintmodule.Activities.RegisterActivity;
import com.karl.fingerprintmodule.Adapters.UsersAdapter;
import com.karl.fingerprintmodule.Flags;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.GridAutofitLayoutManager;
import com.karl.fingerprintmodule.Helper;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.RecyclerViewClickListener;
import com.karl.fingerprintmodule.ViewModels.FmdViewModel;
import com.karl.fingerprintmodule.ViewModels.TimekeepingViewModel;

import java.util.ArrayList;

public class RegisterFromListFragment extends Fragment implements RecyclerViewClickListener {

    private Context ctx;
    private TimekeepingViewModel viewModel;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    //private String m_deviceName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fingerprint_register_fragment, container, false);
        ctx = v.getContext();
        //m_deviceName = getArguments().getString("device_name");


        viewModel = ViewModelProviders.of(getActivity()).get(TimekeepingViewModel.class);

        recyclerView = v.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        int cols = (int) Helper.getInstance(ctx).integerToDP(150);
        // use a linear layout manager
        layoutManager = new GridAutofitLayoutManager(ctx, cols);
        recyclerView.setLayoutManager(layoutManager);

        final RecyclerViewClickListener listener = this;

        ArrayList<User> users = viewModel.getUserArrayList().getValue();

        if (users != null) {
            mAdapter = new UsersAdapter(users, ctx, listener);
        } else {

            viewModel.getUsers();

            viewModel
                    .getUserArrayList()
                    .observe(this, new Observer<ArrayList<User>>() {
                        @Override
                        public void onChanged(@Nullable ArrayList<User> users) {
                            if (users != null) {

                                mAdapter = new UsersAdapter(users, ctx, listener);
                            } else {
                                mAdapter = new UsersAdapter(new ArrayList<User>(), ctx, listener);
                            }
                        }
                    });
        }

        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return v;
    }

    @Override
    public void onItemClick(int position, int flag) {

        User user = viewModel.findUserFromPosition(position);

        if (user != null) {

            try {
                Bundle b = new Bundle();
                b.putSerializable("user", user);

                Fragment f = getArguments().getInt("flag") == Flags.REGISTER ? new RegisterFragment() : new PinInputFragment();
                f.setArguments(b);

                ViewModelProviders.of(getActivity()).get(TimekeepingViewModel.class).addFragment(f);

            } catch (Exception e) {

                Toast.makeText(ctx, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Globals.register = false;
    }
}
