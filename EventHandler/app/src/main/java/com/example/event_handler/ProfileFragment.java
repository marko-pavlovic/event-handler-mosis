package com.example.event_handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbrMessage;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabaseUserID;
    StorageReference mStorageRef;
    ImageView ImViewProfile;
    TextView TextViewImeIPrezime, TextViewUsername;
    String UserProfilePictureName;
    Bitmap ProfileImage;
    //UserSingleton currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//		//super.onViewCreated(view, savedInstanceState);
//		User simpleUserData = new User("Jovana","Jovanovic","jovanajovanovic@gmail.com","Jovanaaa","0324534553");
//		ArrayList<User> listUsers = new ArrayList<User>();
//		for(int i=0;i<5;i++) {
//			listUsers.add(new User(simpleUserData));
//		}
//		AdapterFriends af = new AdapterFriends(getActivity(),listUsers);
//		GridView viewUsers = view.findViewById(R.id.grid);
//		viewUsers.setAdapter(af);

        //currentUser = UserSingleton.getInstance();
        //currentUser.GetCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mDatabaseUserID = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());


        ImViewProfile = (ImageView) view.findViewById(R.id.imageViewProfilnaSlika);
        TextViewImeIPrezime = view.findViewById(R.id.textViewIme);
        TextViewUsername = view.findViewById(R.id.textViewUsername);

        SetUserInfo();
//        ImViewProfile.setImageBitmap(GetImageUri());
    }


    public void SetUserInfo() {

        mDatabaseUserID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String FirstName = dataSnapshot.child("firstName").getValue().toString();
                String LastName = dataSnapshot.child("lastName").getValue().toString();
                String UserName = dataSnapshot.child("username").getValue().toString();
                Object asd = dataSnapshot.getValue();
                UserProfilePictureName = dataSnapshot.child("profilePictureStorageName").getValue().toString();
                mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + UserProfilePictureName);
                TextViewImeIPrezime.setText(FirstName + " " + LastName);
                TextViewUsername.setText(UserName);
                SetProfilePicture();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        TextViewImeIPrezime.setText(currentUser.firstName + " " + currentUser.lastName);
//        TextViewUsername.setText(currentUser.username);
//
//        SetProfilePicture();
    }

    public void SetProfilePicture() {
        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + UserProfilePictureName);
        mStorageRef.getBytes(1024 * 1024 * 3)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ImViewProfile.setImageBitmap(bitmap);
                        Log.w("profileFragment", "bitmap radi");
                    }
                });
        mStorageRef.getBytes(1024 * 1024 * 3).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("profileFragment", "bitmap ne radi");
            }
        });
    }
}


//    public String getUserProfilePictureName()
//    {
//        final String[] asd = new String[1];
//        mDatabaseUserID.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                asd[0] = dataSnapshot.child("profilePictureStorageName").getValue().toString();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        return asd[0];
//    }

//    public Bitmap GetImageUri() {
//        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + UserProfilePictureName);
//        try {
//            final File localFile = File.createTempFile("Images", "bmp");
//            mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    ProfileImage = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return ProfileImage;
//    }
//}
