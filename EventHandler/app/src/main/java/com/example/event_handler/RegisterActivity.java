package com.example.event_handler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;


public class RegisterActivity extends AppCompatActivity {
    EditText Email, Password, FirstName, LastName, UserName, Phone;
    Button registerButton, loginButton, pictureButton;
    FirebaseAuth firebaseAuth;
    private static final int CAMERA_REQUEST = 1888;
    File cameraPhotoFile;
    private final String LOG_TAG = "UserProfileActivity";
    private static final String TAG = "myTag";
    private DatabaseReference dbrMessage;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword1);
        FirstName = (EditText) findViewById(R.id.etFirstName);
        LastName = (EditText) findViewById(R.id.etLastName);
        UserName = (EditText) findViewById(R.id.etUsername1);
        Phone = (EditText) findViewById(R.id.etPhoneNumber);
        registerButton = (Button) findViewById(R.id.NewMemberButton);
        loginButton = (Button) findViewById(R.id.AlReadyButton);
        pictureButton = (Button) findViewById(R.id.pictureButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                String password = Password.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                }

                if(password.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                }

                CreateUserWithEmail(email, password);
                startActivity(new Intent(getApplicationContext(),MejnActivity.class));

            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                String password = Password.getText().toString();
                CreateUserWithEmail(email, password);
                startActivity(new Intent(getApplicationContext(),ImageProfileActivity.class));
                Log.d(TAG, "picture button");
            }
        });

        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MejnActivity.class));
        }


    }

    private void CreateUserWithEmail(final String email, String Password){
        firebaseAuth.createUserWithEmailAndPassword(email,Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            firebaseAuth = FirebaseAuth.getInstance();
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            writeNewUser(user.getUid(), getUserData());

                            finish();
                        }
                        else{
                            Log.w(TAG, "createUserWithEmail:failure  " + email, task.getException());
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"E-mail or password is wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    protected User getUserData(){
        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword1);
        FirstName = (EditText) findViewById(R.id.etFirstName);
        LastName = (EditText) findViewById(R.id.etLastName);
        UserName = (EditText) findViewById(R.id.etUsername1);
        Phone = (EditText) findViewById(R.id.etPhoneNumber);
        User result = new User(FirstName.getText().toString(), LastName.getText().toString(), Email.getText().toString(), UserName.getText().toString(), Phone.getText().toString());

        return result;
    }

    private void writeNewUser(String userId, User user) {

        mDatabase.child("Users").child(userId).setValue(user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == CAMERA_REQUEST) {
            MediaScannerConnection.scanFile(this,
                    new String[]{cameraPhotoFile.getAbsolutePath()}, null, null);

        }
    }
}