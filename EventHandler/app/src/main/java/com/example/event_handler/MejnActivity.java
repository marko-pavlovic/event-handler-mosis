package com.example.event_handler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MejnActivity extends AppCompatActivity {
    TextView textView;
    Button btnDeleteUser,btnLogout,btnUploadToDatabase, btnAddPicture, btnMainActivity;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbrMessage;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private DatabaseReference mDatabase;
    String uriProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mejn);
        textView = (TextView) findViewById(R.id.textView1);
        btnDeleteUser =(Button) findViewById(R.id.deleteUser);
        btnUploadToDatabase =(Button) findViewById(R.id.btnBaza);
        btnAddPicture =(Button) findViewById(R.id.btnAddPicture);
        btnLogout =(Button) findViewById(R.id.logout);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnMainActivity =(Button) findViewById(R.id.btnMain);

        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        //final UserSingleton currentUser;
        //currentUser = UserSingleton.getInstance();
        //currentUser.GetCurrentUser();

        String asd = "asd";

        if(user != null)
        {
            textView.setText("Hi " );
        }
        else
        {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        };



        btnUploadToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null){
                    //writeNewUser(user.getUid(),"Pera", "Peric", "pera@mejl.com", "Perica", "0654243235");
                    uriProfileImage = "asdasdoji.jpg";
                    mDatabase.child("Users").child(user.getUid()).child("ProfilePicURI").setValue(uriProfileImage);
                }
            }
        });

        btnMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null){
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),"User deleted",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                //currentUser.DeleteUser();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });


    }

    private void writeNewUser(String userId, String FirstName, String LastName, String email, String username, String phone) {
        User user = new User(FirstName,LastName, email, username, phone);

        mDatabase.child("users").child(userId).setValue(user);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //FirebaseUser user = mAuth.getCurrentUser();
        //firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}