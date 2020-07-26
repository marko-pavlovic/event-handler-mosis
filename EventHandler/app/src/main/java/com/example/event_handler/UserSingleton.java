package com.example.event_handler;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class UserSingleton {

    public String firstName, lastName, email, username, phone, latitude, longitude, imageURL, currentEvent, registrationToken, signedOut, visible, profilePictureStorageName;
    public int checkins = 1000000000;
    public List<String> friends;
    public HashMap<String, Boolean> receivedEventNotif;
    public List<String> friendRequests;
    private static UserSingleton UserObject;

    UserSingleton user;
    private FirebaseUser FBUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabaseUserID;

    public UserSingleton() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public static UserSingleton getInstance(){
        if(UserObject == null)
            UserObject = new UserSingleton();
        return UserObject;
    }

    public void DeleteUser(){
        UserObject = null;
    }

    public UserSingleton(String FirstName, String LastName, String email, String username, String phone, String profilePictureStorageName)
    {
        this.firstName = FirstName;
        this.lastName = LastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.profilePictureStorageName = profilePictureStorageName;
    }

    public UserSingleton(String FirstName, String LastName, String email, String username, String phone)
    {
        this.firstName = FirstName;
        this.lastName = LastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
    }

    public UserSingleton(String firstName, String lastName, String email, String username, String phone, String latitude, String longitude, String imageURL, int checkins, String currentEvent, String registrationToken, List<String> friends, String signedOut, String visible, HashMap<String, Boolean> receivedEventNotif) {

        this.friends = friends;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageURL = imageURL;
        this.checkins = checkins;
        this.currentEvent = currentEvent;
        this.registrationToken = registrationToken;
        this.signedOut = signedOut;
        this.visible = visible;
        this.receivedEventNotif = receivedEventNotif;

    }

    public UserSingleton(UserSingleton object){
        this.username = object.username;
        this.firstName = object.firstName;
        this.lastName = object.lastName;
        this.phone = object.phone;
        this.email = object.email;
        this.imageURL = object.imageURL;
    }

     public void GetCurrentUser(){
        FBUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseUserID = FirebaseDatabase.getInstance().getReference().child("Users").child(FBUser.getUid());
        mDatabaseUserID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserSingleton.this.firstName = dataSnapshot.child("firstName").getValue().toString();
                UserSingleton.this.lastName = dataSnapshot.child("lastName").getValue().toString();
                UserSingleton.this.username = dataSnapshot.child("username").getValue().toString();
                UserSingleton.this.email = dataSnapshot.child("email").getValue().toString();
                UserSingleton.this.phone = dataSnapshot.child("phone").getValue().toString();
                UserSingleton.this.profilePictureStorageName = dataSnapshot.child("profilePictureStorageName").getValue().toString();

                //user = new User(FirstName,LastName,Email,UserName,Phone,profilePictureStorageName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Class GetCurrentUser", "Failed to create user");
            }
        });

    }

    public void addFriend(String id){
        friends.add(id);
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
