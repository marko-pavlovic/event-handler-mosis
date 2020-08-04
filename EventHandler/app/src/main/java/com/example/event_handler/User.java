package com.example.event_handler;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User implements Serializable {

    public String firstName, lastName, email, username, phone, latitude, longitude, imageURL, currentEvent, registrationToken, signedOut, visible, profilePictureStorageName;
    //public int checkins = 1000000000;
    public List<String> friends;
    public HashMap<String, Boolean> receivedEventNotif;
    public List<String> friendRequests;
    public User pomUser;
    //public Bitmap profilePicture;

    transient User user;
    transient private FirebaseUser FBUser;
    transient FirebaseDatabase database = FirebaseDatabase.getInstance();
    transient DatabaseReference myRef = database.getReference("Users");
    transient private DatabaseReference databaseReference;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String FirstName, String LastName, String email, String username, String phone, String profilePictureStorageName)
    {
        this.firstName = FirstName;
        this.lastName = LastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.profilePictureStorageName = profilePictureStorageName;
    }

    public User(String FirstName, String LastName, String email, String username, String phone)
    {
        this.firstName = FirstName;
        this.lastName = LastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
    }

    public User(String firstName, String lastName, String email, String username, String phone, String latitude, String longitude, String imageURL, int checkins, String currentEvent, String registrationToken, List<String> friends, String signedOut, String visible, HashMap<String, Boolean> receivedEventNotif) {

        this.friends = friends;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageURL = imageURL;
        //this.checkins = checkins;
        this.currentEvent = currentEvent;
        this.registrationToken = registrationToken;
        this.signedOut = signedOut;
        this.visible = visible;
        this.receivedEventNotif = receivedEventNotif;

    }

    public User(User object){
        this.username = object.username;
        this.firstName = object.firstName;
        this.lastName = object.lastName;
        this.phone = object.phone;
        this.email = object.email;
        this.imageURL = object.imageURL;
    }

     public User GetCurrentUser(String UId){
        //Paper.init(ctx);
         FBUser = FirebaseAuth.getInstance().getCurrentUser();
        //pomUser = Paper.book(UId).read("userInfo");
        //Log.d("gcu","pre baze " + pomUser.firstName);
         databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(UId);
         databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //pomUser = (User) dataSnapshot.getValue();
                //Log.d("gcu",pomUser.firstName);
                User.this.firstName = dataSnapshot.child("firstName").getValue().toString();
                User.this.lastName = dataSnapshot.child("lastName").getValue().toString();
                User.this.username = dataSnapshot.child("username").getValue().toString();
                User.this.email = dataSnapshot.child("email").getValue().toString();
                User.this.phone = dataSnapshot.child("phone").getValue().toString();
                User.this.profilePictureStorageName = dataSnapshot.child("profilePictureStorageName").getValue().toString();

                //user = new User(FirstName,LastName,Email,UserName,Phone,profilePictureStorageName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Class GetCurrentUser", "Failed to create user");
            }
        });
        return User.this;
    }


    public void addFriend(String UId){
        friends.add(UId);
        FBUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(FBUser.getUid());
        databaseReference.setValue(UId);
    }

    @Exclude
    public String getFirstName() {
        return firstName;
    }

    @Exclude
    public String getLastName() {
        return lastName;
    }


}
