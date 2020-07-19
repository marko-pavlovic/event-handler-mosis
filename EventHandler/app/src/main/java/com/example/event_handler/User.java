package com.example.event_handler;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    public String firstName, lastName, email, username, phone, latitude, longitude, imageURL, currentEvent, registrationToken, signedOut, visible;
    public int checkins = 1000000000;
    public List<String> friends;
    public HashMap<String, Boolean> receivedEventNotif;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
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
        this.checkins = checkins;
        this.currentEvent = currentEvent;
        this.registrationToken = registrationToken;
        this.signedOut = signedOut;
        this.visible = visible;
        this.receivedEventNotif = receivedEventNotif;

    }

    public User(User object){
        this.username = username;
        this.firstName = object.firstName;
        this.lastName = object.lastName;
        this.phone = object.phone;
        this.email = object.email;
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
