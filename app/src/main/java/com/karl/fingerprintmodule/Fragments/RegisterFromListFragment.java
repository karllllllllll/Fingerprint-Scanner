package com.karl.fingerprintmodule.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karl.fingerprintmodule.Adapters.UsersAdapter;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;

import java.util.ArrayList;

public class RegisterFromListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fingerprint_register_fragment, container, false);

        recyclerView = v.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(layoutManager);


        ArrayList<User> ul = new ArrayList<>();
        ul.add(new User("1","James","Bond",""));
        ul.add(new User("2","James2","Bond",""));
        ul.add(new User("3","James3","Bond",""));
        ul.add(new User("4","James4","Bond",""));
        ul.add(new User("5","James5","Bond",""));

        // specify an adapter (see also next example)
        mAdapter = new UsersAdapter(ul);
        recyclerView.setAdapter(mAdapter);

        return v;
    }

}
