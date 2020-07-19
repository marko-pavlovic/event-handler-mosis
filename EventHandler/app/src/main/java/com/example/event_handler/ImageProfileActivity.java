package com.example.event_handler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageProfileActivity extends AppCompatActivity implements View.OnClickListener{

	private static final int CHOOSE_IMAGE= 1;
	private static final String TAG = "myTag";
	TextView tvChooseImg;
	//TextView tvVerified;
	ProgressBar progressBar;
	ImageView imgProfile;
	Button btnSave;

	Uri uriProfileImage;
	String profileImageUrl;
	private ImagePicker imagePicker;
	private final String LOG_TAG = "UserProfileActivity";
	Boolean createEvent = false;
	private FirebaseAuth.AuthStateListener  authStateListener;
	private FirebaseAuth mAuth;
	private FirebaseUser user;
	FirebaseAuth firebaseAuth;
	private DatabaseReference dbrMessage;
	private DatabaseReference mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "on create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		firebaseAuth = FirebaseAuth.getInstance();
		user = FirebaseAuth.getInstance().getCurrentUser();

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		mDatabase = FirebaseDatabase.getInstance().getReference();

		Bundle extras = getIntent().getExtras();
		if(extras == null) {
			createEvent= false;
		} else {
			createEvent = true;
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

		mAuth = FirebaseAuth.getInstance();

		// tvVerified = (TextView) findViewById(R.id.tvChooseImg);
		tvChooseImg = (TextView) findViewById(R.id.tvChooseImg);
		imgProfile = (ImageView) findViewById(R.id.imgProfile);
		progressBar =(ProgressBar) findViewById(R.id.progressBar);

		btnSave =  (Button) findViewById(R.id.btnSave);

		imgProfile.setOnClickListener(this);
		btnSave.setOnClickListener(this);

		loadUserInformation();

	}

	private void loadUserInformation()
	{
		FirebaseUser user = mAuth.getCurrentUser();
		Log.d(TAG, "loadUserInformation");

//		if(user != null)
//		{
//            if(user.isEmailVerified())
//            {
//                tvVerified.setText("Email Verified");
//            }
//            else
//            {
//                tvVerified.setText("Email Not Verified (Click to verify)");
//                tvVerified.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Toast.makeText(ImageProfileActivity.this, "Verification Email Sent", Toast.LENGTH_LONG  ).show();
//                            }
//                        });
//                    }
//                });
//            }
//		}
	}


	@Override
	protected void onStart() {

		super.onStart();
		Log.d(TAG, "onStart");

		if(mAuth.getCurrentUser() == null)
		{
			Log.d(TAG, "onStart mAuth.getCurrentUser() == null");
			finish();
			Log.d(TAG, "Finish onStart mAuth.getCurrentUser() == null");
			startActivity(new Intent(this, LoginActivity.class));
		}


	}

	@Override
	public void onClick(View v) {

		switch (v.getId())
		{
			case R.id.imgProfile:
				showImageChooser();
				break;
			case R.id.btnSave:
				saveUserInformation();
				break;
		}
	}

	private void saveUserInformation()
	{
		if(!createEvent)
		{
			signUpImageProfile();
		}
		else
		{
			createEventImageProfile();
		}

	}
	private void createEventImageProfile() {


		Intent eventIntent = new Intent();
		eventIntent.putExtra("profileImageUrl", profileImageUrl);
		setResult(Activity.RESULT_OK, eventIntent);
		finish();
	}

	private void signUpImageProfile()
	{
		FirebaseUser user = mAuth.getCurrentUser();
		Log.d(TAG, "id" + user.getUid());


		if(user != null && profileImageUrl != null)
		{
			FirebaseDatabase.getInstance().getReference("Users")
					.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
					.child("imageURL")
					.setValue(profileImageUrl);


			Log.d(TAG, "inside");
			UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
					//  .setDisplayName(displayName)
					.setPhotoUri(Uri.parse(profileImageUrl))
					.build();

			Log.d(TAG, "created request");
			user.updateProfile(profile)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if(task.isSuccessful())
							{
								Log.d(TAG, "successful");
								Toast.makeText(ImageProfileActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
								backToSingIn();
							}
						}
					});

		}
	}
	private void backToSingIn()
	{
		Intent intent = new Intent(ImageProfileActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void showImageChooser()
	{
		if (CropImage.isExplicitCameraPermissionRequired(this)) {
			Log.d(TAG, "trazi permissions");
			requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
		} else {
			Log.d(TAG, "uso show image chooser , start pick image activity");
			CropImage.startPickImageActivity(this);
		}
	}

	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null)
		{
			uriProfileImage = data.getData();


			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
				imgProfile.setImageBitmap(bitmap);

				uploadImgToFirebaseStorage();


			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		super.onActivityResult(requestCode, resultCode, data);
	}
	*/
	@Override
	@SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
			Log.d(TAG, "uso u pick image activity result");
			Uri imageUri = CropImage.getPickImageResultUri(this, data);

			// For API >= 23 we need to check specifically that we have permissions to read external storage,
			// but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
			if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

				// request permissions and handle the result in onRequestPermissionsResult()
				uriProfileImage = imageUri;
				requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
			} else {

				CropImage.activity(imageUri)
						.setGuidelines(CropImageView.Guidelines.ON)
						.setFixAspectRatio(true)
						.start(this);

			}
		}

		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			Log.d(TAG, "uso u crop image activity result");
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK) {
				uriProfileImage = result.getUri();
				try {
					//scale image to 500x500
					Bitmap croppedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uriProfileImage);
					Bitmap scaledImage = Bitmap.createScaledBitmap(croppedImage,500,500,false);


					imgProfile.setImageBitmap(scaledImage);

					uploadImgToFirebaseStorage();

				} catch (IOException e) {
					Log.e(LOG_TAG,e.toString());
				}
				//userImage.setImageURI(imageUri);
				// isImageChanged= true;
			} else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
				Exception error = result.getError();
				Log.e("UserProfileActivity",error.toString());
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				Log.d(TAG, "uso u onRequestPermissionsResult  da se startuje pick image activity za camera capture");
				CropImage.startPickImageActivity(this);

			} else {
				Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
			}
		}
		if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
			if (imgProfile != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//if we get permission for read external storage then start crop image after that we show image
				Log.d(TAG, "uso u onRequestPermissionsResult  da se cropuje activity pick image");
				CropImage.activity(uriProfileImage)
						.setGuidelines(CropImageView.Guidelines.ON)
						.setFixAspectRatio(true)
						.start(this);
			} else {
				Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
			}
		}
	}



	private void uploadImgToFirebaseStorage() {
		Log.d(TAG, "uso u upload to firebase");
		StorageReference profileImgRef;

		if(!createEvent) {
			Log.d(TAG, "upload pic link to database");
			profileImgRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");
			mDatabase.child("Users").child(user.getUid()).child("ProfilePicURI").setValue(uriProfileImage.toString());
		}
		else
			profileImgRef = FirebaseStorage.getInstance().getReference("eventpics/" + System.currentTimeMillis() + ".jpg");

		if(uriProfileImage != null)
		{
			progressBar.setVisibility(View.VISIBLE);

			profileImgRef.putFile(uriProfileImage)
					.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
					{
						@Override
						public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
						{
							progressBar.setVisibility(View.GONE);
							profileImageUrl = taskSnapshot.getDownloadUrl().toString();


						}
					})
					.addOnFailureListener(new OnFailureListener()
					{
						@Override
						public void onFailure(@NonNull Exception e)
						{
							progressBar.setVisibility(View.GONE);
							Toast.makeText(ImageProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});

		}
	}



	private File createImageFile() throws IOException { 		// ne koristi se
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		//mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		//mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}


/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout,menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch(item.getItemId())
        {
            case R.id.itemLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(ImageProfileActivity.this, LoginActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
