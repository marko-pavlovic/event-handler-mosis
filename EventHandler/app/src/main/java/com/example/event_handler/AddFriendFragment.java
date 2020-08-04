package com.example.event_handler;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import io.paperdb.Paper;

public class AddFriendFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;
    private FirebaseUser FBUser;
    private DatabaseReference dbrMessage;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabaseUserID;
    StorageReference mStorageRef;

    EditText etAddFriendUid;
    Button btnAddFriend, btnRemoveFriend;
    ImageView imgviewSlika;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addfriend, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        FBUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseUserID = FirebaseDatabase.getInstance().getReference().child("Friends");

        etAddFriendUid = (EditText) view.findViewById(R.id.editTextAddFriendUid);
        btnAddFriend = (Button) view.findViewById(R.id.btnAddFriend);
        btnRemoveFriend = (Button) view.findViewById(R.id.btnDeleteFriend);
        imgviewSlika = (ImageView) view.findViewById(R.id.imgviewSlika);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String asdasd= "asda";
        asdasd = asdasd + " asdas";
        btnAddFriend.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String asdd = "Assdd";
                mDatabaseUserID.child(FBUser.getUid()).setValue(etAddFriendUid.getText().toString());
                mDatabaseUserID.child(etAddFriendUid.getText().toString()).setValue(FBUser.getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Friend added successfully", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnRemoveFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseUserID.child(FBUser.getUid()).child(etAddFriendUid.getText().toString()).removeValue();
                mDatabaseUserID.child(etAddFriendUid.getText().toString()).child(FBUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Friend removed successfully", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        imgviewSlika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseUserID.child(FBUser.getUid()).child(etAddFriendUid.getText().toString()).removeValue();
                mDatabaseUserID.child(etAddFriendUid.getText().toString()).child(FBUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Friend removed successfully", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    public void SetProfilePicture() {

        Bitmap bitmap = new ImageSaver(getActivity().getApplicationContext())
                .setDirectoryName(FBUser.getUid())
                .setFileName("userProfilePicture")
                .load();
        imgviewSlika.setImageBitmap(bitmap);

    }

}

