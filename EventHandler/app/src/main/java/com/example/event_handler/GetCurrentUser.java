package com.example.event_handler;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import io.paperdb.Paper;

public class GetCurrentUser {
    User user;
    private FirebaseUser FBUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabaseUserID;

    public GetCurrentUser(){}

    public User Get(Context ctx){
        Paper.init(ctx);
        FBUser = FirebaseAuth.getInstance().getCurrentUser();
        Boolean asd = Paper.exist(FBUser.getUid());
        user = (User) Paper.book(FBUser.getUid()).read("userInfo");
        String dsa = "asd";

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
                String opgkf = "asds";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Class GetCurrentUser", "Failed to create user");
            }
        });

        return user;
    }

    public void GetProfilePicture(Context ctx){

    }
}
