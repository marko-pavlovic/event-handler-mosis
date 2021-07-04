package com.example.event_handler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.event_handler.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.example.event_handler.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventActivity extends AppCompatActivity {
    private static final String FIREBASE_EVENT_CHILD = "events";
    private static final String TAG = "EventActivity";
    private static final String FIREBASE_CHILD_USER ="users";
    //region Members
    private Event event;
    private Context that=this;
    private TextView eAddressTextView,eDateTextView,eRatingTextView,eDaysLeftTextView,eHoursLeftTextView,eMinutesLeftTextView,eSecondsLeftTextView,eCreatorTextView,eDescriptionTextView,eSpecReqTextView,galleryViewAllTextView,attendeesViewAllTextView,ePriceTextView,eOccupancyTextView;
    private ImageView coverImage,expandImage;
    private MaterialButton eventButton;
    private LinearLayout galleryHorizontalScrollView, attendeesHorizontalScrollView,bottomSheetLinearLayout;
    private RatingBar ratingBar;
    private StorageReference storage;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private BottomSheetBehavior bottomSheetBehavior;
    private ArrayList<Uri> uris;
    private ArrayList<Uri> attendeesImagesUris;
    private ArrayList<User> attendees;
    private boolean isAnAttendee;

    //endregion
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        //region Init
        eAddressTextView=findViewById(R.id.event_address);
        eDateTextView=findViewById(R.id.event_date);
        eRatingTextView=findViewById(R.id.event_rating);
        coverImage=findViewById(R.id.event_cover_image);
        eDaysLeftTextView=findViewById(R.id.event_time_left_days);
        eHoursLeftTextView=findViewById(R.id.event_time_left_hours);
        eMinutesLeftTextView=findViewById(R.id.event_time_left_minutes);
        eSecondsLeftTextView=findViewById(R.id.event_time_left_seconds);
        expandImage=findViewById(R.id.expand_image);
        ratingBar=findViewById(R.id.event_rating_stars);
        eDescriptionTextView=findViewById(R.id.event_description);
        eCreatorTextView=findViewById(R.id.event_creator);
        eSpecReqTextView=findViewById(R.id.event_spec_req);
        ePriceTextView=findViewById(R.id.event_price);
        eventButton=findViewById(R.id.event_button);
        eOccupancyTextView=findViewById(R.id.event_occupancy);
        bottomSheetLinearLayout=findViewById(R.id.bottom_sheet);
        bottomSheetBehavior=BottomSheetBehavior.from(bottomSheetLinearLayout);
        galleryViewAllTextView=findViewById(R.id.gallery_view_all);
        galleryHorizontalScrollView=findViewById(R.id.gallery_hsv);
        attendeesHorizontalScrollView =findViewById(R.id.atendees_hsv);
        attendeesViewAllTextView=findViewById(R.id.attendees_view_all);
        storage= FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
        uris=new ArrayList<>();
        isAnAttendee=false;
        //endregion

        //region GetEvent
        Intent eventIntent=getIntent();
        String eventkey=eventIntent.getStringExtra("key");
        int index=Singleton.getInstance().getEventKeyIndexer().get(eventkey);
        event=Singleton.getInstance().getEvents().get(index);
        //endregion


        //region Action bar
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(event.getTitle());
        }
        //endregion

        //region SetText
        eAddressTextView.setText(event.getAddress());
        eDateTextView.setText(event.getDateTime());
        eRatingTextView.setText(String.format("%.2f", event.getRating()));
        if(event.getPrice()>0)
        ePriceTextView.setText(getText(R.string.price).toString()+" "+event.getPrice());
        else {
            ePriceTextView.setText(getText(R.string.price).toString()+" Free");
        }
        eOccupancyTextView.setText(getText(R.string.occupancy)+" "+event.attendeesID.size()+"/"+event.getMaxOccupancy());
        if(Singleton.getInstance().getUser().uID.compareTo(event.getCreatorID())==0){
            eCreatorTextView.setText(Singleton.getInstance().getUser().firstName+" "+Singleton.getInstance().getUser().lastName);
        }
        else
        database.child(FIREBASE_CHILD_USER).child(event.getCreatorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                eCreatorTextView.setText(user.firstName+" "+user.lastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //endregion

        //region Cover image
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        storage.child(FIREBASE_EVENT_CHILD).child(eventkey).child("cover").child("cover").getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.with(that).load(uri).resize(width,width).centerCrop().into(coverImage);
        });
        //endregion

        setUpEventButton();

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if(fromUser){
                if(event.getCreatorID().compareTo(auth.getCurrentUser().getUid())==0){
                    Toast.makeText(that, "Can't rate your own event", Toast.LENGTH_SHORT).show();
                    ratingBar.setRating((float) event.rating);
                    return;
                }
                String userId=auth.getCurrentUser().getUid();
                database.child(FIREBASE_CHILD_USER).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User u=dataSnapshot.getValue(User.class);
                        boolean flag=false;
                        for (String s : u.ratedEventsID) {
                            if(s.compareTo(event.key)==0){
                                flag=true;
                                Toast.makeText(that, "Vec ste ocenili ovaj dogadjaj", Toast.LENGTH_SHORT).show();
                                database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).child("rating").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double rating=dataSnapshot.getValue(Double.class);
                                        ratingBar.setRating((float) rating);
                                        eRatingTextView.setText(String.format("%.2f", rating));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        if (!flag){
                            database.child(FIREBASE_CHILD_USER).child(auth.getCurrentUser().getUid()).child("ratedEventsID").child(""+u.ratedEventsID.size()).setValue(event.getKey());
                            database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Event e=dataSnapshot.getValue(Event.class);
                                    double newRating=e.rating*e.ratedByID.size();
                                    newRating+=rating;
                                    newRating/=(e.ratedByID.size()+1);
                                    database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).child("rating").setValue(newRating);
                                    database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).child("ratedByID").child(e.getRatedByID().size()+"").setValue(userId);
                                    ratingBar.setRating((float) newRating);
                                    eRatingTextView.setText(String.format("%.2f", newRating));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            database.child(FIREBASE_CHILD_USER).child(event.getCreatorID()).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    double points=dataSnapshot.getValue(Double.class);
                                    double newpoints=points+rating;
                                    database.child(FIREBASE_CHILD_USER).child(event.getCreatorID()).child("points").setValue(newpoints);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        //region Countdown
        Runnable helloRunnable = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            try {
                Date end = sdf.parse(event.getDateTime());
                Date now=new Date();
                long difference= TimeUnit.MILLISECONDS.toSeconds(end.getTime() - now.getTime());
                if(difference>0){
                long days=difference/(60*60*24);
                difference-=(days*60*60*24);
                long hours=difference/(60*60);
                difference-=(hours*60*60);
                long minutes=difference/(60);
                difference-=(minutes*60);
                eDaysLeftTextView.setText(String.valueOf(days));
                eHoursLeftTextView.setText(String.valueOf(hours));
                eMinutesLeftTextView.setText(String.valueOf(minutes));
                eSecondsLeftTextView.setText(String.valueOf(difference));
                }
                else {
                    eDaysLeftTextView.setText(String.valueOf(0));
                    eHoursLeftTextView.setText(String.valueOf(0));
                    eMinutesLeftTextView.setText(String.valueOf(0));
                    eSecondsLeftTextView.setText(String.valueOf(0));
                    
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);
        //endregion

        expandImage.setOnClickListener(v -> {
            TextView textView=findViewById(R.id.txv_spec_req);
            if(textView.getVisibility()==View.GONE &&eSpecReqTextView.getVisibility()==View.GONE){
                textView.setVisibility(View.VISIBLE);
                eSpecReqTextView.setVisibility(View.VISIBLE);
                eSpecReqTextView.setText(event.specialRequirement);
                expandImage.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24));
            }
            else {
                textView.setVisibility(View.GONE);
                eSpecReqTextView.setVisibility(View.GONE);
                expandImage.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24));
            }
        });

        loadGallery();

        galleryViewAllTextView.setOnClickListener(v -> {
            Intent intent=new Intent(that,EventGalleryActivity.class);
            intent.putExtra("uris",uris);
            intent.putExtra("key",event.getKey());
            intent.putExtra("creator",event.getCreatorID());
            startActivity(intent);
        });

        loadAttendees();

        attendeesViewAllTextView.setOnClickListener(v -> {
            ArrayList<String> attendeeNames=new ArrayList<>();
            for (User u:attendees) {
                attendeeNames.add(u.firstName+" "+u.lastName);
            }
            MaterialAlertDialogBuilder materialAlertDialogBuilder=new MaterialAlertDialogBuilder(that);
            materialAlertDialogBuilder.setTitle("Attendees of "+event.getTitle()).setItems(attendeeNames.toArray(new CharSequence[]{}),(dialog, which) -> {
                Intent intent=new Intent(that,ProfileActivity.class);
                if(attendees.get(which).uID.compareTo(Singleton.getInstance().getUser().uID)==0)
                    intent.putExtra("type","loggedIn");
                else
                    intent.putExtra("type","other");
                intent.putExtra("key",attendees.get(which).uID);
                startActivity(intent);
            }).show();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        loadGallery();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case android.R.id.home:{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadGallery() {
        uris=new ArrayList<>();
        galleryHorizontalScrollView.removeAllViews();
        storage.child(FIREBASE_EVENT_CHILD).child(event.getKey()).listAll().addOnSuccessListener(listResult -> {
            listResult.getItems().forEach(storageReference -> {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    if(!uris.contains(uri))
                    {
                        uris.add(uri);
                        //ImageView Setup
                        ImageView imageView = new ImageView(this);

                        //setting image resource
                        Picasso.with(that).load(uri).resize(200,200).centerCrop().into(imageView);

                        //setting image position
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        galleryHorizontalScrollView.addView(imageView);
                    }
                });

            });
        });
    }

    public void updateUserAttendedEventsID(String s){
        FirebaseUser userFB= auth.getCurrentUser();
        String userID=userFB.getUid();
        database.child(FIREBASE_CHILD_USER).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                database.child(FIREBASE_CHILD_USER).child(userID).child("attendedEventsID").child(""+user.attendedEventsID.size()).setValue(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateEventAttendeesID(String key) {
        String uid=auth.getCurrentUser().getUid();
        database.child(FIREBASE_EVENT_CHILD).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Event event=dataSnapshot.getValue(Event.class);
                database.child(FIREBASE_EVENT_CHILD).child(key).child("attendeesID").child(String.valueOf(event.getAttendeesID().size())).setValue(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadEvent();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadAttendees() {
        attendeesHorizontalScrollView.removeAllViews();
        attendees= new ArrayList<>();
        attendeesImagesUris=new ArrayList<>();
        event.getAttendeesID().forEach(uid -> {
            database.child(FIREBASE_CHILD_USER).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user=dataSnapshot.getValue(User.class);
                    user.uID=uid;
                    attendees.add(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            storage.child(FIREBASE_CHILD_USER).child(uid).child("profile").getDownloadUrl().addOnSuccessListener(uri -> {
                attendeesImagesUris.add(uri);
                CircleImageView imageView = new CircleImageView(this);
                Picasso.with(that).load(uri).resize(200,200).centerCrop().into(imageView);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                attendeesHorizontalScrollView.addView(imageView);
            });

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void changeButtonIfAttendee(){
        event.getAttendeesID().forEach(uid -> {
            if(uid.compareTo(auth.getCurrentUser().getUid())==0){
                isAnAttendee=true;
                eventButton.setText(R.string.am_out);
                eventButton.setOnClickListener(v -> {
                    database.child(FIREBASE_CHILD_USER).child(auth.getCurrentUser().getUid()).child("attendedEventsID").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ArrayList<String> attendees= (ArrayList<String>) dataSnapshot.getValue();
                            attendees.remove(event.getKey());
                            database.child(FIREBASE_CHILD_USER).child(auth.getCurrentUser().getUid()).child("attendedEventsID").setValue(attendees);

                            database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).child("attendeesID").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ArrayList<String> attendees= (ArrayList<String>) dataSnapshot.getValue();
                                    attendees.remove(auth.getCurrentUser().getUid());
                                    database.child(FIREBASE_EVENT_CHILD).child(event.getKey()).child("attendeesID").setValue(attendees).addOnSuccessListener(aVoid -> loadEvent());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                });

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadEvent(){
        int index=Singleton.getInstance().getEventKeyIndexer().get(event.getKey());
        event=Singleton.getInstance().getEvents().get(index);
        loadAttendees();
        setUpEventButton();
        eOccupancyTextView.setText(getText(R.string.occupancy)+" "+event.attendeesID.size()+"/"+event.getMaxOccupancy());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpEventButton(){
        eventButton.setText(R.string.count_me);
        if(event.getCreatorID().compareTo(auth.getCurrentUser().getUid())==0){
            eventButton.setEnabled(false);
            eventButton.setText(R.string.creator);
        }
        else if(event.getMaxOccupancy()==event.attendeesID.size()){
            eventButton.setEnabled(false);
            eventButton.setText(R.string.event_full);
        }
        else {
            changeButtonIfAttendee();
        }
        if (!isAnAttendee) {
            eventButton.setOnClickListener(v -> {
                updateUserAttendedEventsID(event.getKey());
                updateEventAttendeesID(event.getKey());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            });
        }
    }

}