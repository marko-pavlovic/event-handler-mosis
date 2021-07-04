package com.example.event_handler;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.event_handler.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class CreateAccountActivity extends Activity {
    private static final String TAG ="CreateAccountActivity"  ;
    public static final Integer SELECT_PICTURE=1;
    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText confirmPassword;
    RadioGroup gender;
    EditText date;
    ImageView image;
    ImageView dateImage;
    Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;
    public static final String USER_CHILD="users";
    private Context ctx =this;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        firstName=findViewById(R.id.FirstNameCA);
        lastName=findViewById(R.id.LastNameCA);
        email=findViewById(R.id.EmailCA);
        password=findViewById(R.id.PasswordCA);
        confirmPassword=findViewById(R.id.ConfirmPasswordCA);
        gender=findViewById(R.id.GenderCA);
        date= findViewById(R.id.DateCA);
        image=findViewById(R.id.ImageEdit);
        dateImage=findViewById(R.id.imageDateCA);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        Button btnCreateAccount=findViewById(R.id.CreateAccount);
        //endregion

        dateImage.setOnClickListener(v -> {
            Calendar calendar=Calendar.getInstance();
            int year=calendar.get(Calendar.YEAR);
            int month=calendar.get(Calendar.MONTH);
            int day=calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog=new DatePickerDialog(CreateAccountActivity.this,new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.setText(year+"/"+(month+1)+"/"+dayOfMonth);
                }
            }, year,month, day);
            datePickerDialog.show();
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region IF-ELSE Checks
                if(firstName.getText().toString().isEmpty()){

                    firstName.setError("Polje ime je prazno");
                    return;
                }
                else if(lastName.getText().toString().isEmpty()){
                    lastName.setError("Polje prezime je prazno");
                    return;
                }
                else if(email.getText().toString().isEmpty()){
                    email.setError("Polje email je prazno");
                    return;
                }
                else if(password.getText().toString().isEmpty()){
                    password.setError("Polje lozinka je prazno");
                    return;
                }
                else if(confirmPassword.getText().toString().isEmpty()){
                    confirmPassword.setError("Lozinka nije potvrdjena");
                    return;
                }
                else if(date.getText().toString().isEmpty()){
                    date.setError("Datum rodjenja nije postavljen");
                    return;
                }
                //endregion

                //region User init with data
                user=new User();
                user.firstName=firstName.getText().toString();
                user.lastName=lastName.getText().toString();
                user.email=email.getText().toString();
                if(password.getText().toString().equals(confirmPassword.getText().toString())){
                    user.password=password.getText().toString();
                }
                else {
                    confirmPassword.setError("Lozinke se ne poklapaju");
                    password.setError("Lozinke se ne poklapaju");
                    return;
                }
                int selectedId=gender.getCheckedRadioButtonId();
                RadioButton genderRB=findViewById(selectedId);
                user.gender=genderRB.getText().toString();
                user.birthDate=date.getText().toString();
                user.bookmarkedEventsID =new ArrayList<>();
                user.createdEventsID=new ArrayList<>();
                user.ratedEventsID=new ArrayList<>();
                user.attendedEventsID =new ArrayList<>();
                //endregion

                //region CreateUserWithEmailAndPassword
                auth.createUserWithEmailAndPassword(user.email,user.password).addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser currentUser=auth.getCurrentUser();
                            database.child(USER_CHILD).child(currentUser.getUid()).setValue(user);
                            uploadImage();
                            Toast.makeText(CreateAccountActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(ctx,MainActivity.class);
                            ctx.startActivity(i);
                        }
                        else {
                            Toast.makeText(CreateAccountActivity.this, "Creating account failed,email might be already in use", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //endregion

            }
        });

    }
    private void pickImage(){
        Intent i=new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select profile picture"),SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if (requestCode==SELECT_PICTURE){
                Picasso.with(CreateAccountActivity.this).load(data.getData()).resize(500,500).onlyScaleDown().into(image);
                imageUri=data.getData();
            }
        }
    }
    private void uploadImage(){
        FirebaseUser currentUser=auth.getCurrentUser();
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storage.child(USER_CHILD).child(currentUser.getUid()).child("profile").putBytes(data);
//        storage.child(USER_CHILD).child(currentUser.getUid()).child("profile").putFile(imageUri);
    }
}
