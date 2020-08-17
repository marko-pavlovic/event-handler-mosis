package com.example.event_handler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdapterFriends extends BaseAdapter {
    private Context ctx;
    private List<User> userList;
    private LayoutInflater inflater;
    StorageReference mStorageRef;
    Bitmap temp;

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
        imgProfile.setImageBitmap(SetProfilePicture(userList.get(i).profilePictureStorageName));

        return viewItemFriend;
    }

    public Bitmap SetProfilePicture(String profilePictureStorageName) {

        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + profilePictureStorageName);
        mStorageRef.getBytes(1024 * 1024 * 3)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Log.w("profileFragment", "bitmap radi");
                    }
                });
        mStorageRef.getBytes(1024 * 1024 * 3).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("profileFragment", "bitmap ne radi");
            }
        });
        return temp;
    }
}
