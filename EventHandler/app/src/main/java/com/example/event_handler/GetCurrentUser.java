package com.example.event_handler;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;


public class GetCurrentUser {
    User user;
    private FirebaseUser FBUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabaseUserID;

    public GetCurrentUser(){}

    public User GetCurrentUser(){
        mDatabaseUserID = FirebaseDatabase.getInstance().getReference().child("Users").child(FBUser.getUid());
        mDatabaseUserID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String FirstName = dataSnapshot.child("firstName").getValue().toString();
                String LastName = dataSnapshot.child("lastName").getValue().toString();
                String UserName = dataSnapshot.child("username").getValue().toString();
                String Email = dataSnapshot.child("email").getValue().toString();
                String Phone = dataSnapshot.child("phone").getValue().toString();
                String profilePictureStorageName = dataSnapshot.child("profilePictureStorageName").getValue().toString();

                user = new User(FirstName,LastName,Email,UserName,Phone,profilePictureStorageName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Class GetCurrentUser", "Failed to create user");
            }
        });

        return user;
    }
}
