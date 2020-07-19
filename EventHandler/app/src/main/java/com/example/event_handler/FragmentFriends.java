package com.example.event_handler;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import android.app.Fragment;

import java.util.ArrayList;

public class FragmentFriends extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       // return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.activity_friends,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        User simpleUserData = new User("tamaraJo","Tamara","tamarojo@gmail.com","Tamaraaa","78574645");
        ArrayList<User> listUsers = new ArrayList<User>();
        for(int i=0;i<5;i++) {
            listUsers.add(new User(simpleUserData));
        }
        AdapterFriends af = new AdapterFriends(getActivity(),listUsers);
        GridView viewUsers = view.findViewById(R.id.grid);
        viewUsers.setAdapter(af);
    }
}
