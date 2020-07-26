package com.example.event_handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class RegisterActivity extends AppCompatActivity {
    EditText Email, Password, FirstName, LastName, UserName, Phone;
    Button registerButton, pictureButton;
    TextView loginButton;
    FirebaseAuth firebaseAuth;
    private static final int REQUEST_CAMERA = 1888;
    private static final int SELECT_FILE = 1889;
    File cameraPhotoFile;
    AlertDialog.Builder builder;
    private boolean profilePictureSet = false;
    private final String LOG_TAG = "UserProfileActivity";
    private static final String TAG = "myTag";
    private DatabaseReference dbrMessage;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    ImageView ImgViewProfile;
    Uri imgUri;
    String ImgStorageLink;
    StorageReference mStorageRef;

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
        loginButton = (TextView) findViewById(R.id.Member);
        pictureButton = (Button) findViewById(R.id.pictureButton);
        ImgViewProfile = (ImageView) findViewById(R.id.ImgViewProfilePicture);

        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages");

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
                //final String email = Email.getText().toString();
                //String password = Password.getText().toString();
                //CreateUserWithEmail(email, password);
                //startActivity(new Intent(getApplicationContext(),ImageProfileActivity.class));
                //profilePictureSet = true;
                selectImage();
                Log.d(TAG, "picture button");
            }
        });

//        if(firebaseAuth.getCurrentUser()!=null){
//            startActivity(new Intent(getApplicationContext(),MejnActivity.class));
//        }


    }

    private void selectImage(){
        final CharSequence[] items = { "Take Photo", "Choose from gallery",
                "Cancel" };

        builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Change photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from gallery")) {
                            Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void UploadImageToStorage(){
        ImgStorageLink = System.currentTimeMillis()+".jpg"; //+getExtension(imgUri)
        StorageReference ref = mStorageRef.child(ImgStorageLink);
        ref.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(RegisterActivity.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(RegisterActivity.this, "Profile picture failed to upload", Toast.LENGTH_SHORT).show();
                    }
                });
//        StorageReference ref = mStorageRef.child(ImgStorageLink);
//        Bitmap bitmap = null;
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ByteArrayOutputStream boas = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG,90, boas);
//        ref.putBytes(boas.toByteArray())
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(RegisterActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    private void CreateUserWithEmail(final String email, String Password) {
        if (profilePictureSet) {
            firebaseAuth.createUserWithEmailAndPassword(email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseAuth = FirebaseAuth.getInstance();
                                user = FirebaseAuth.getInstance().getCurrentUser();
                                UploadImageToStorage();
                                writeNewUser(user.getUid(), getUserData());

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(getUserData().firstName + getUserData().lastName)
                                        .build();
                                finish();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure  " + email, task.getException());
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "E-mail or password is wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else
            Toast.makeText(getApplicationContext(), "Please add profile picture", Toast.LENGTH_SHORT).show();
    }


    protected User getUserData(){
        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword1);
        FirstName = (EditText) findViewById(R.id.etFirstName);
        LastName = (EditText) findViewById(R.id.etLastName);
        UserName = (EditText) findViewById(R.id.etUsername1);
        Phone = (EditText) findViewById(R.id.etPhoneNumber);
        User result = new User(FirstName.getText().toString(), LastName.getText().toString(), Email.getText().toString(), UserName.getText().toString(), Phone.getText().toString(), ImgStorageLink);

        return result;
    }

    private void writeNewUser(String userId, User user) {

        mDatabase.child("Users").child(userId).setValue(user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
//                imgUri = data.getData();
//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                ImgViewProfile.setImageBitmap(photo);
//                profilePictureSet = true;
                imgUri = data.getData();
                CropImage.activity(imgUri)
                        .setFixAspectRatio(true)
                        .start(this);
            }
            else if (requestCode == SELECT_FILE) {
                imgUri = data.getData();


                //Bitmap bm = null;
                //String tempPath = UI_helper.getPath(selectedImageUri, ActivitySignUp.this);
                //BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                //bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                //imgbProfile.setImageBitmap(bm);

                //ImgViewProfile.setImageURI(imgUri);

                CropImage.activity(imgUri)
                        .setFixAspectRatio(true)
                        .start(this);
                //profilePictureSet = true;
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    imgUri = result.getUri();
                    ImgViewProfile.setImageURI(imgUri);
                    profilePictureSet = true;
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "uso u onRequestPermissionsResult  da se startuje pick image activity za camera capture");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA);

            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (ImgViewProfile != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if we get permission for read external storage then start crop image after that we show image
                Log.d(TAG, "uso u onRequestPermissionsResult  da se cropuje activity pick image");
                CropImage.activity(imgUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .start(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
    // Stari on activity result
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode != Activity.RESULT_OK) {
//            profilePictureSet = true;
//            return;
//        }
//        if (requestCode == REQUEST_CAMERA) {
//            MediaScannerConnection.scanFile(this,
//                    new String[]{cameraPhotoFile.getAbsolutePath()}, null, null);
//            profilePictureSet = true;
//
//        }
//    }
}