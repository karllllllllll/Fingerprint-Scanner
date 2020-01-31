package com.karl.fingerprintmodule.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karl.fingerprintmodule.Flags;
import com.karl.fingerprintmodule.Models.User;
import com.karl.fingerprintmodule.R;
import com.karl.fingerprintmodule.RecyclerViewClickListener;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    private ArrayList<User> mDataset;
    private Context fragment;
    private RecyclerViewClickListener recyclerviewOnClickListener;

    public UsersAdapter(ArrayList<User> myDataset, Context fragment, RecyclerViewClickListener listener) {
        this.mDataset = myDataset;
        this.fragment = fragment;
        this.recyclerviewOnClickListener = listener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RecyclerViewClickListener clickListener;
        TextView textView;
        ImageView iv_user_image;

        MyViewHolder(View v, RecyclerViewClickListener onItemClickListener) {
            super(v);

            this.textView = v.findViewById(R.id.tv_name);
            this.iv_user_image = v.findViewById(R.id.iv_user_image);
            this.clickListener = onItemClickListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), Flags.SHOW);
        }
    }

    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fingerprint_register_user_recyclerview, parent, false);

        MyViewHolder vh = new MyViewHolder(v, recyclerviewOnClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        User user = mDataset.get(position);

        holder.textView.setText(user.getF_name() + " " + user.getL_name());

        Glide.with(fragment)
                .load(user.getImage_path())
                .apply(RequestOptions
                        .circleCropTransform())
                .into(holder.iv_user_image);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}



