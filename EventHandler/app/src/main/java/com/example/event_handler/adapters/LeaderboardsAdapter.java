package com.example.event_handler.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.example.event_handler.ProfileActivity;
import com.example.event_handler.R;
import com.example.event_handler.Singleton;
import com.example.event_handler.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.ViewHolder> {

    private Context ctx;
    private ArrayList<User> users;

    public LeaderboardsAdapter(Context ctx, ArrayList<User> users) {
        this.ctx = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboards_rv_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fullName=users.get(position).firstName+" "+users.get(position).lastName;
        holder.fullnameTextView.setText(fullName);
        holder.pointsTextView.setText(users.get(position).points+" stars");
        FirebaseStorage.getInstance().getReference().child("users").child(users.get(position).uID).child("profile").getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.with(ctx).load(uri).resize(200,200).centerInside().into(holder.profileImageView);
        });
        holder.profileImageView.setOnClickListener(v -> {
            Toast.makeText(ctx, "Go to "+fullName+" profile", Toast.LENGTH_SHORT).show();
        });
        holder.layout.setOnClickListener(v -> {
            Intent intent=new Intent(ctx, ProfileActivity.class);
            if(users.get(position).uID.compareTo(Singleton.getInstance().getUser().uID)==0)
                intent.putExtra("type","loggedIn");
            else
                intent.putExtra("type","other");
            intent.putExtra("key",users.get(position).uID);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fullnameTextView;
        ConstraintLayout layout;
        TextView pointsTextView;
        CircleImageView profileImageView;

        ViewHolder(View itemView) {
            super(itemView);

            fullnameTextView = itemView.findViewById(R.id.leaderboards_rv_item_name);
            pointsTextView = itemView.findViewById(R.id.leaderboards_rv_points);
            profileImageView=itemView.findViewById(R.id.leaderboards_rv_item_image);
            layout=itemView.findViewById(R.id.leaderboards_rv_layout);
        }
    }
}
