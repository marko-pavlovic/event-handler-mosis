package com.example.event_handler.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.example.event_handler.ProfileActivity;
import com.example.event_handler.R;
import com.example.event_handler.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    ArrayList<User> users;
    Context ctx;

    public FriendsAdapter(ArrayList<User> users, Context ctx) {
        this.users = users;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_card,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.usernameTextView.setText(users.get(position).firstName+" "+users.get(position).lastName);
        FirebaseStorage.getInstance().getReference().child("users").child(users.get(position).uID).child("profile").getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.with(ctx).load(uri).resize(200,200).centerInside().into(holder.profileImageView);
        });
        holder.cardView.setOnClickListener(v -> {
            Intent intent=new Intent(ctx, ProfileActivity.class);
            intent.putExtra("type","other");
            intent.putExtra("key",users.get(position).uID);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        CircleImageView profileImageView;
        CardView cardView;


        ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.friends_username);
            profileImageView=itemView.findViewById(R.id.friends_profile_pic);
            cardView=itemView.findViewById(R.id.card_view_friends);

        }
    }
}
