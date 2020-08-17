package com.example.event_handler;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class FriendList {


    private ArrayList<String> list;
    private static final FriendList instance = new FriendList();

    private FriendList()
    {
        list = new ArrayList<String>();
        list = Paper.book("friends").read("friendList");
    }

    public static FriendList getInstance(){
        if (instance != null)
        {
            return instance;
        }
        return new FriendList();
    }

    public ArrayList<String> getList(){
        if(list == null) {
            list = new ArrayList<String>();
        }
        return list;
    }

    public void addFriend(String friendId){
        if(list.contains(friendId))
            return;
        list.add(friendId);
        Paper.book("friends").write("friendList", list);
    }

    public void removeFriend(String friendId){
        list.remove(friendId);
        Paper.book("friends").write("friendList", list);
    }
}
