package com.example.event_handler;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.event_handler.models.Event;
import com.example.event_handler.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.example.event_handler.adapters.GridViewImagesAdapter;
import com.example.event_handler.models.CategoryList;
import com.example.event_handler.models.NewEvent;
import com.example.event_handler.rest_api.IService;

import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateEventActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 5;
    private static final int SELECT_PICTURE_GALLERY = 6;
    private static final long ONE_DAY_MS = 86400000;
    private static final String TAG = "CreateEvent";
    private static final String FIREBASE_CHILD_CAT = "categories";
    private static final String FIREBASE_CHILD_USER = "users";
    private static final String FIREBASE_CHILD_EBD = "events_by_date";
    public static final String FIREBASE_CHILD="events";
    private FirebaseAuth auth;
    EditText title;
    EditText date;
    EditText time;
    EditText address;
    EditText description;
    EditText price;
    EditText specialReq;
    EditText maxOccupancy;
    EditText citySpinner;
    MaterialSpinner categoriesSpinner;
    ImageView dateImg;
    ImageView  timeImg;
    ImageView coverImage;
    Context that=this;
    //DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    DatabaseReference database;
    StorageReference storage;
    Event event;
    Uri coverImageUri;
    private String currEventkey;
    private GridView gridView;
    ArrayList<Uri> galleryImages;
    GridViewImagesAdapter imagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Inits
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        auth=FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        title=findViewById(R.id.TitleCE);
        date=findViewById(R.id.DateCE);
        time=findViewById(R.id.TimeCE);
        address=findViewById(R.id.AddressCE);
        description=findViewById(R.id.DescriptionCE);
        price=findViewById(R.id.PriceCE);
        specialReq=findViewById(R.id.SpecialReqCE);
        maxOccupancy=findViewById(R.id.OccupancyCE);
        citySpinner =findViewById(R.id.CityCE);
        categoriesSpinner =findViewById(R.id.spinnerCat);
        dateImg=findViewById(R.id.DateImgCE);
        timeImg=findViewById(R.id.TimeImgCE);

        //region CoverImage
        coverImage=findViewById(R.id.AddCoverImg);
        Picasso.with(that).load(R.drawable.event_sample_photo).fit().into(coverImage);
        coverImage.setOnClickListener(v -> {
            pickImage(SELECT_PICTURE);
        });
        //endregion

        //region Gallery
        galleryImages=new ArrayList<>();
        galleryImages.add(Uri.parse("android.resource://com.example.event_handler/" + R.drawable.plus_blue));
        gridView=findViewById(R.id.grid_create_event);
        imagesAdapter=new GridViewImagesAdapter(galleryImages,this);
        gridView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) menuInfo;
            Uri uri= galleryImages.get(info.position);
            if(info.position>0)
            menu.add(0,0,1,menuIconWithText(getDrawable(R.drawable.bin),"Delete image"));
        });
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if(position==0){
                pickImage(SELECT_PICTURE_GALLERY);
            }
        });
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {

            return false;
        });
        gridView.setAdapter(imagesAdapter);
        //endregion

        event=new Event();
        //endregion

        //region GET CATEGORIES
        if (getCategories().isEmpty())
        {
            database.child(FIREBASE_CHILD_CAT).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Singleton.getInstance().categories= dataSnapshot.getValue(CategoryList.class);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateEventActivity.this,
                            android.R.layout.simple_spinner_item, getCategories());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    adapter.notifyDataSetChanged();
                    categoriesSpinner.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateEventActivity.this,
                    android.R.layout.simple_spinner_item, getCategories());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapter.notifyDataSetChanged();
            categoriesSpinner.setAdapter(adapter);
        }
        //endregion

        //region DateDialog
        dateImg.setOnClickListener(v -> {
            Calendar calendar=Calendar.getInstance();
            int year=calendar.get(Calendar.YEAR);
            int month=calendar.get(Calendar.MONTH);
            int day=calendar.get(Calendar.DAY_OF_MONTH);
             DatePickerDialog datePickerDialog=new DatePickerDialog(CreateEventActivity.this,new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.setText(year+"/"+(month+1)+"/"+dayOfMonth);
                }
            }, year,month, day);
            datePickerDialog.show();
        });
        //endregion

        //region TimeDialog
        timeImg.setOnClickListener(v -> {
            timePickerDialog=new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if(hourOfDay<10&&minute<10)
                        time.setText("0"+hourOfDay+":0"+minute);
                    else if(hourOfDay<10)
                        time.setText("0"+hourOfDay+":"+minute);
                    else if(minute<10)
                        time.setText(hourOfDay+":0"+minute);
                    else
                        time.setText(hourOfDay+":"+minute);
                }
            },0,0,true);
            timePickerDialog.show();
        });
        //endregion

        //region Intent
        Intent intGet= getIntent();
        Bundle bundle=intGet.getExtras();
        if(bundle!=null){
            event.lon=bundle.getDouble("lon");;
            event.lat=bundle.getDouble("lat");
            try {
                getAddressFromLonAndLat(event.lon,event.lat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //endregion



    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getItemId()==0){
            galleryImages.remove(info.position);
            imagesAdapter.notifyDataSetChanged();
            Log.d(TAG, "onContextItemSelected: "+galleryImages.size());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_event_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case R.id.okMenu:{
                //region If-ELSE
                if(title.getText().toString().isEmpty()){

                    title.setError("Polje naziv je prazan");
                    break;
                }
                else if(date.getText().toString().isEmpty()){
                    date.setError("Datum nije postavljen");
                    break;
                }
                else if(time.getText().toString().isEmpty()){
                    time.setError("Vreme nije postavljeno");
                    break;
                }
                else if(address.getText().toString().isEmpty()){
                    address.setError("Polje adresa je prazno");
                    break;
                }
                else if(address.getText().toString().isEmpty()){
                    address.setError("Polje adresa je prazno");
                    break;
                }
                else if(maxOccupancy.getText().toString().isEmpty()){
                    maxOccupancy.setError("Nije postavljen broj gostiju");
                    break;
                }
                else if(description.getText().toString().isEmpty()){
                    description.setError("Polje opis je prazno");
                    break;
                }
                else if(price.getText().toString().isEmpty()){
                    price.setError("Polje cena je prazno");
                    break;
                }
                //endregion
                GeoPoint gp;
                try {
                    if(event.lon==0) {
                        gp = getLocationFromAddress(address.getText().toString() + ", " + citySpinner.getText().toString());
                        event.lon = gp.getLongitude();
                        event.lat = gp.getLatitude();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                event.title=title.getText().toString();
                event.dateTime=date.getText().toString()+" "+time.getText().toString();
                event.address=address.getText().toString()+", "+citySpinner.getText().toString();
                event.maxOccupancy=Integer.parseInt(maxOccupancy.getText().toString());
                event.description=description.getText().toString();
                event.specialRequirement=specialReq.getText().toString();
                event.rating=0;
                event.price=Double.parseDouble(price.getText().toString());
                event.category =getCategories().get(categoriesSpinner.getSelectedIndex());
                event.setCreatorID(auth.getCurrentUser().getUid());
                if(event.getAttendeesID()==null){
                    event.attendeesID=new ArrayList<>();
                }
                event.attendeesID.add(auth.getCurrentUser().getUid());
                try {
                    addNewEvent();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                sendNotification(new NewEvent(event.key,event.title,event.lat,event.lon,auth.getCurrentUser().getUid()));
                uploadImage();
                uploadGallery();
                Intent eventIntent=new Intent();
                setResult(Activity.RESULT_OK, eventIntent);
                finish();
                break;
            }
            case android.R.id.home:{
                finish();
                break;
                }
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if (requestCode==SELECT_PICTURE){
                Picasso.with(that).load(data.getData()).resize(1000,1000).onlyScaleDown().centerInside().into(coverImage);
                coverImageUri=data.getData();
            }
            else if(requestCode==SELECT_PICTURE_GALLERY){
                galleryImages.add(data.getData());
                imagesAdapter.notifyDataSetChanged();
            }
        }
    }

    public GeoPoint getLocationFromAddress(String strAddress) throws IOException {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new GeoPoint((double) (location.getLatitude()),
                    (double) (location.getLongitude()));

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getAddressFromLonAndLat(double lon,double lat) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lon, 1);

        address.setText(addresses.get(0).getAddressLine(0).substring(0,addresses.get(0).getAddressLine(0).indexOf(",")));
        citySpinner.setText(addresses.get(0).getLocality());

    }

    private ArrayList<String> getCategories(){
        return Singleton.getInstance().getCategories();
    }

    public void addNewEvent() throws ParseException {
        currEventkey=database.push().getKey();
        event.setKey(currEventkey);
        updateUserAttendedEventsID(currEventkey);
        database.child(FIREBASE_CHILD).child(currEventkey).setValue(event);
        updateUserCreatedEventID(currEventkey);
        String eventDate=date.getText().toString();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd");
        Date date=simpleDateFormat.parse(eventDate);
        Toast.makeText(this, ""+simpleDateFormat.format(date), Toast.LENGTH_SHORT).show();
        database.child(FIREBASE_CHILD_EBD).child(simpleDateFormat.format(date)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> data= (ArrayList<String>) dataSnapshot.getValue();
                if(data!=null)
                    database.child(FIREBASE_CHILD_EBD).child(simpleDateFormat.format(date)).child(data.size()+"").setValue(currEventkey);
                else
                    database.child(FIREBASE_CHILD_EBD).child(simpleDateFormat.format(date)).child("0").setValue(currEventkey);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateUserCreatedEventID(String s){
        FirebaseUser userFB= auth.getCurrentUser();
        String userID=userFB.getUid();
        database.child(FIREBASE_CHILD_USER).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                database.child(FIREBASE_CHILD_USER).child(userID).child("createdEventsID").child(String.valueOf(user.createdEventsID.size())).setValue(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pickImage(int type){
        Intent i=new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select cover image"),type);
    }

    private void uploadImage(){
        coverImage.setDrawingCacheEnabled(true);
        coverImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storage.child(FIREBASE_CHILD).child(currEventkey).child("cover").child("cover").putBytes(data);
    }

    private void uploadGallery() {
        int i=0;
        for (Uri uri:galleryImages) {
            if(i!=0){
                storage.child(FIREBASE_CHILD).child(currEventkey).child(String.valueOf(i)).putFile(uri);
            }
            i++;
        }
    }

    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, 50,50);
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }
    public void updateUserAttendedEventsID(String s){
        FirebaseUser userFB= auth.getCurrentUser();
        String userID=userFB.getUid();
        database.child(FIREBASE_CHILD_USER).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                database.child(FIREBASE_CHILD_USER).child(userID).child("attendedEventsID").child(String.valueOf(user.createdEventsID.size())).setValue(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(NewEvent event){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://us-central1-eventhandler-acb52.cloudfunctions.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IService iService =retrofit.create(IService.class);

        Call<String> call=iService.newEvent(event);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "onResponse: Works");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "onFailure: Dead");
            }
        });

    }
}
