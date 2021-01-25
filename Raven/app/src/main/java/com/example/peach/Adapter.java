package com.example.peach;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.myViewHolder> {

    Context mContext;
    List<Profile> mData;

    public Adapter(Context mContext, List<Profile> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public Adapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.user, parent, false);
        return new myViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if (mData.get(position).getUserID() != null) {
            try {
                storageRef.child("images/" + mData.get(position).getUserID()).getDownloadUrl().addOnSuccessListener(uri ->
                        Picasso.get().load(uri).into(holder.profile_photo)).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
        holder.tv_name.setText(" " + mData.get(position).getUsername());
        if(mData.get(position).getLamp() == 0){
            holder.lamp.setVisibility(View.INVISIBLE);
        }else {
            holder.lamp.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_photo;
        TextView tv_name;
        View lamp;

        public myViewHolder(View itemView) {
            super(itemView);
            profile_photo = itemView.findViewById(R.id.profile_photo);
            tv_name = itemView.findViewById(R.id.profile_name);
            lamp = itemView.findViewById(R.id.lamp);
        }

    }

}
