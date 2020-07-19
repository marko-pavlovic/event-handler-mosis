package com.example.event_handler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdapterFriends extends BaseAdapter {
    private Context ctx;
    private List<User> userList;
    private LayoutInflater inflater;

    public AdapterFriends(Context ctx, List<User> userList){
        this.ctx=ctx;
        this.userList=userList;
        this.inflater = LayoutInflater.from(ctx);
    }
    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        User user = userList.get(i);
        View viewItemFriend = inflater.inflate(R.layout.item_friend_list,null);
        TextView txtName = viewItemFriend.findViewById(R.id.txtName);
        txtName.setText(user.getFirstName()+" "+user.getLastName());
        ImageView imgProfile = viewItemFriend.findViewById(R.id.imgProfile);
        //imgProfile.setImageBitmap(user.getProfilePictureBitmap());

        return viewItemFriend;
    }
}
