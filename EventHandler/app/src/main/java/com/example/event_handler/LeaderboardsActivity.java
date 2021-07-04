package com.example.event_handler;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.event_handler.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.event_handler.adapters.LeaderboardsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LeaderboardsActivity extends AppCompatActivity {

    private static final String TAG = "LeaderboardsActivity";
    private RecyclerView recyclerView;
    private DatabaseReference database;
    private Context that=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);
        recyclerView=findViewById(R.id.leaderboards_rv);
        database= FirebaseDatabase.getInstance().getReference();
        //region Action bar
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.leaderboard);
        }
        //endregion

        database.child("users").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> leaderboards=new ArrayList<>();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    User user = userSnapshot.getValue(User.class);
                    user.uID=userSnapshot.getKey();
                    Log.d(TAG, "onDataChange: "+user);
                    leaderboards.add(user);
                }
                Collections.sort(leaderboards, Comparator.comparing(User::getPoints).reversed());
                LeaderboardsAdapter leaderboardsAdapter=new LeaderboardsAdapter(that, leaderboards);
                leaderboardsAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(leaderboardsAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(that));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}