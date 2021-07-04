package com.example.event_handler;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.event_handler.models.CategoryList;
import com.example.event_handler.models.Event;
import com.example.event_handler.models.User;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends Activity {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    private static final String FIREBASE_CHILD_CAT = "categories";
    public static final long ONE_MEGA_BYTE=1024*1024;
    private static final String TAG ="MainActivity";
    private static final String FIREBASE_CHILD_USER ="users" ;
    private static final String FIREBASE_CHILD_EBD ="events_by_date";
    private static final String FIREBASE_CHILD_EVENT ="events" ;
    private FirebaseAuth auth;
    private Context that=this;
    private MapView map=null;
    private IMapController mapController=null;
    private MyLocationNewOverlay myLocationNewOverlay;
    private ChipGroup chipGroup;
    private FloatingActionButton fabPointer,fabFriends;
    private TextView popupTextView1,popupTextView2,popUpTextView3;
    private MaterialButton cancelBtn;
    private MaterialButton popUpBtn;
    private ImageView popupBookmarkImageView;
    private BottomNavigationView bottomNav;
    private CardView popUpCardView;
    private SearchView searchView;
    private DatabaseReference database;
    private StorageReference storage;
    private ItemizedIconOverlay eventsOverlay;
    private static boolean isFriendsOn;
    private HashMap<String,Bitmap> friendsBM;
    private HashMap<String,User> friendsUsers;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth=FirebaseAuth.getInstance();
        isFriendsOn =false;
        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        bottomNav=findViewById(R.id.bottom_nav_bar);
        popUpCardView=findViewById(R.id.popupWindowMain);
        searchView=findViewById(R.id.searchView);
        popupTextView1=findViewById(R.id.popupText1);
        popupTextView2=findViewById(R.id.popupText2);
        popUpTextView3=findViewById(R.id.popupText3);
        popupBookmarkImageView=findViewById(R.id.popupBookmark);
        cancelBtn=findViewById(R.id.popupCancel);
        popUpBtn=findViewById(R.id.popupBtn);
        bottomNav.setSelectedItemId(R.id.nb_map);
        chipGroup=findViewById(R.id.categories_chip_group);
        final Chip chip = (Chip) MainActivity.this.getLayoutInflater().inflate(R.layout.item_chip_layout, null, false);
        chip.setText(R.string.my_events);
        chip.setChipBackgroundColor(getColorStateList(R.color.colorBlack));
        chip.setTextColor(getColorStateList(R.color.colorAccent));
        chip.setOutlineSpotShadowColor(Color.YELLOW);
        chip.setOutlineAmbientShadowColor(Color.YELLOW);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isFriendsOn)
                showFriendsNew();
            if (isChecked)
            {
                filterMyEvents();
                setUpMapClick();
                map.getOverlays().add(myLocationNewOverlay);
                map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
            }
        });
        chip.setChipIcon(getResources().getDrawable(R.drawable.user,null));
        chipGroup.addView(chip);
        //endregion

        //region BottomNavBar
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nb_profile:{
                    Intent intent=new Intent(that,ProfileActivity.class);
                    intent.putExtra("key",auth.getCurrentUser().getUid());
                    intent.putExtra("type","loggedIn");
                    that.startActivity(intent);
                    break;
                }
                case R.id.nb_events:{
                    Intent intent=new Intent(that,EventsActivity.class);
                    intent.putExtra("Activity","event");
                    that.startActivity(intent);
                    break;
                }
                case R.id.nb_options:{
                    MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(that);
                    dialogBuilder.setTitle(R.string.options).setItems(isMyServiceRunning(MyService.class)?new CharSequence[]{menuIconWithText(getResources().getDrawable(R.drawable.ranking,null),"Leaderboard"),menuIconWithText(getResources().getDrawable(R.drawable.friends,null),"Friends list"),
                                    menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_rss_feed_24,null),"Turn off service"), menuIconWithText(getResources().getDrawable(R.drawable.logout,null),"Logout")}:
                                    new CharSequence[]{menuIconWithText(getResources().getDrawable(R.drawable.ranking,null),"Leaderboard"),menuIconWithText(getResources().getDrawable(R.drawable.friends,null),"Friends list"), menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_rss_feed_24,null),"Turn on service"),
                                            menuIconWithText(getResources().getDrawable(R.drawable.logout,null),"Logout")},
                            (dialog, which) -> {
                                switch (which){
                                    case 0:{
                                        startActivity(new Intent(that,LeaderboardsActivity.class));
                                        break;
                                    }
                                    case 1:{
                                        startActivity(new Intent(that,FriendsActivity.class));
                                        break;
                                    }
                                    case 2:{
                                        Intent intent=new Intent(that, MyService.class);
                                        if(isMyServiceRunning(MyService.class)){
                                            stopService(intent);
                                        }
                                        else {
                                            startService(intent);
                                        }
                                        break;
                                    }
                                    case 3:{
                                        auth.signOut();
                                        Intent intent=new Intent(this,LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            }).show().setOnDismissListener(dialog ->bottomNav.setSelectedItemId(R.id.nb_map));
                }
            }
            return true;
        });
        //endregion

        //region Map
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        map = findViewById(R.id.map);
        map.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                double zoom=event.getZoomLevel();
                if(zoom<13.5){
                    removeOverlays();
                }
                else {
                    if (isFriendsOn){
                        showFriendsOnZoom();
                        return false;
                    }
                    if (chipGroup!=null)
                    {
                        if(chipGroup.getCheckedChipId()!=-1)
                        {
                            int id=chipGroup.getCheckedChipId();
                            id--;
                            Chip chip= (Chip) chipGroup.getChildAt(id);
                            chip.setChipBackgroundColor(getColorStateList(R.color.colorBlack));
                            chip.setTextColor(getColorStateList(R.color.colorBackWhite));
                            String category=chip.getText().toString();
                            if(map.getOverlays().size()>0)
                                filterEvents(category);
                        }
                        else
                            showEvents();
                    }
                    else{
                        if(map.getOverlays().size()!=0)
                            showEvents();
                    }
                    map.getOverlays().add(myLocationNewOverlay);
                }
                setUpMapClick();
                return false;
            }
        });
        map.setMultiTouchControls(true);
        //region Permissions
//        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_WRITE_EXTERNAL_STORAGE);
//        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            setMyLocationOverlay();
            //region ServiceStart
            Intent intentService=new Intent(that, MyService.class);
            if(!isMyServiceRunning(MyService.class)){
                startService(intentService);
                Toast.makeText(that, "Service started in background", Toast.LENGTH_SHORT).show();
            }
            //endregion
        }
        //endregion
        mapController=map.getController();
        if(mapController!=null){
            mapController.setZoom(15.0);
            mapController.setCenter(new GeoPoint(43.3209,21.8958));
        }
        //endregion

        addCategories();

        //region FAB-s
        fabPointer=findViewById(R.id.fab_pointer);
        fabPointer.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: "+getEvents().size());
            location();
            setMyLocationOverlay();

        });
        fabFriends=findViewById(R.id.fab_friends);
        fabFriends.setOnClickListener(v ->showFriendsNew());
        //endregion


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(that, ""+query, Toast.LENGTH_SHORT).show();
                Event event=getEvent(query);
                if (event==null){
                    Toast.makeText(that, "Nema pronadjenih dogadjaja", Toast.LENGTH_SHORT).show();
                }
                else {
                    GeoPoint geoPoint=new GeoPoint(event.getLat(),event.getLon());
                    goToGeoPoint(geoPoint,17.0);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        showEventsInit();
        setUpMapClick();
        map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
        Singleton.getInstance().loadUser();
    }



    //region Resume/Pause
    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nb_map);
        if(isFriendsOn)
            showFriends();
        else
            showEvents();
        setUpMapClick();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            showMyLocation();
            map.onResume();
        }
        final Chip chip = (Chip) MainActivity.this.getLayoutInflater().inflate(R.layout.item_chip_layout, null, false);
        chip.setChipBackgroundColor(getColorStateList(R.color.colorBlack));
        chip.setTextColor(getColorStateList(R.color.colorAccent));


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            map.onPause();
        }
    }
    //endregion


    //region Overrides
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMyLocationOverlay();
                    //region ServiceStart
                    Intent intentService=new Intent(that, MyService.class);
                    if(!isMyServiceRunning(MyService.class)){
                        startService(intentService);
                        Toast.makeText(that, "Service started in background", Toast.LENGTH_SHORT).show();
                    }
                    //endregion
                }
                else {
                    finish();
                }
                return;
            }
//            case PERMISSION_WRITE_EXTERNAL_STORAGE:{
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                }else {
//                    finish();
//                }
//            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Toast.makeText(that, "Location is on", Toast.LENGTH_SHORT).show();
            setMyLocationOverlay();
        }
        else {
            Toast.makeText(that, "Location declined", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region My Functions
    private void addCategories(){
        if (getCategories().isEmpty())
        {
            database.child(FIREBASE_CHILD_CAT).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Singleton.getInstance().categories = dataSnapshot.getValue(CategoryList.class);
                    for (String category : getCategories()) {
                        final Chip chip = (Chip) MainActivity.this.getLayoutInflater().inflate(R.layout.item_chip_layout, null, false);
                        chip.setText(category);
                        chip.setChipBackgroundColor(getColorStateList(R.color.colorBlack));
                        chip.setTextColor(getColorStateList(R.color.colorAccent));
                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if(isFriendsOn)
                            {
                                showFriendsNew();
                                map.getOverlays().add(myLocationNewOverlay);
                            }
                            if (isChecked)
                            {
                                filterEvents(category);
                                setUpMapClick();
                                map.getOverlays().add(myLocationNewOverlay);
                            }
                        });
                        storage.child(FIREBASE_CHILD_CAT).child(category.toLowerCase()+".png").getBytes(ONE_MEGA_BYTE).addOnCompleteListener(task -> {
                            byte[] data = task.getResult();
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            chip.setChipIcon(new BitmapDrawable(getResources(),bmp));
                            chipGroup.addView(chip);
                        });
                    }
                    chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        if(checkedId==-1){
                            showEvents();
                            setUpMapClick();
                            map.getOverlays().add(myLocationNewOverlay);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            for (String s : getCategories()) {
                final Chip chip = (Chip) MainActivity.this.getLayoutInflater().inflate(R.layout.item_chip_layout, null, false);
                chip.setText(s);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isFriendsOn)
                    {
                        showFriendsNew();
                        map.getOverlays().add(myLocationNewOverlay);
                    }
                    if (isChecked)
                    {
                        filterEvents(s);
                        setUpMapClick();
                        map.getOverlays().add(myLocationNewOverlay);
                    }
                });
                storage.child(FIREBASE_CHILD_CAT).child(s.toLowerCase()+".png").getBytes(ONE_MEGA_BYTE).addOnCompleteListener(task -> {
                    byte[] data = task.getResult();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    chip.setChipIcon(new BitmapDrawable(getResources(),bmp));
                    chipGroup.addView(chip);
                });
            }
            chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if(checkedId==-1){
                    showEvents();
                    setUpMapClick();
                    map.getOverlays().add(myLocationNewOverlay);
                }
            });
        }
    }

    private void setMyLocationOverlay(){
        myLocationNewOverlay=new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        showMyLocation();
    }

    private void showMyLocation(){
        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(15.0);
            myLocationNewOverlay.enableFollowLocation();
        }
    }

    private ArrayList<String> getCategories(){
        return Singleton.getInstance().getCategories();
    }

    private void showEvents() {
        final ArrayList<OverlayItem> items = new ArrayList<>();
        removeOverlays();
        if (!getEvents().isEmpty())
        {
            for (Event e:getEvents()) {
                OverlayItem item = new OverlayItem(e.title, e.key,new GeoPoint(e.lat,e.lon));
                item.setMarker(getResources().getDrawable(R.drawable.map_pointer_small,null));

                items.add(item);
                Marker m= new Marker(map);
                m.setOnMarkerClickListener((marker, mapView) -> {
                    m.closeInfoWindow();
                    return false;
                });
                m.setTextLabelBackgroundColor(Color.GRAY);
                m.setTextIcon(e.getTitle());

                m.setPosition(new GeoPoint(e.lat,e.lon));
                map.getOverlays().add(m);
            }
            eventsOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int index, OverlayItem item) {
                    Address address;
                    IGeoPoint p = item.getPoint();
                    popUpBtn.setText("Pogledaj");
                    cancelBtn.setOnClickListener(v -> {
                        popUpCardView.setVisibility(View.INVISIBLE);
                    });
                    popUpBtn.setOnClickListener(v -> {
                        popUpCardView.setVisibility(View.INVISIBLE);
                        Intent intent=new Intent(that,EventActivity.class);
                        intent.putExtra("key",getEvents().get(index).getKey());
                        startActivity(intent);
                    });

                    try {
                        address= getAddressFromLonAndLat(p.getLatitude(),p.getLongitude());
                        popupTextView1.setText(item.getTitle());
                        popupTextView2.setText(address.getAddressLine(0).substring(0,address.getAddressLine(0).indexOf(",")));
                        Event e=getEvents().get(index);
                        popUpTextView3.setText(e.getAttendeesID().size()+"/"+e.getMaxOccupancy());
                        if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(e.key)){
                            popupBookmarkImageView.setImageResource(R.drawable.bookmark);
                        }
                        else{
                            popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
                        }
                        popupBookmarkImageView.setOnClickListener(v -> {
                            if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(e.key)){
                                Singleton.getInstance().getUser().bookmarkedEventsID.add(e.key);
                                database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").child("" + (Singleton.getInstance().getUser().bookmarkedEventsID.size() - 1)).setValue(e.key);
                                popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
                            }
                            else{
                                Singleton.getInstance().getUser().bookmarkedEventsID.remove(e.key);
                                database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").setValue(Singleton.getInstance().getUser().bookmarkedEventsID);
                                popupBookmarkImageView.setImageResource(R.drawable.bookmark);
                            }
                            Toast.makeText(that, "nesto", Toast.LENGTH_SHORT).show();
                        });
                        popUpCardView.setVisibility(View.VISIBLE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return false;
                }

                @Override
                public boolean onItemLongPress(int index, OverlayItem item) {
                    GeoPoint u = getUserLocation();
                    IGeoPoint p = item.getPoint();
                    return true;
                }
            }, getApplicationContext());
            map.getOverlays().add(eventsOverlay);
            map.invalidate();
        }
    }

    private GeoPoint getUserLocation(){
        myLocationNewOverlay=new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        return myLocationNewOverlay.getMyLocation();

    }

    private void showEventsInit(){
        final ArrayList<OverlayItem> items = new ArrayList<>();
        removeOverlays();
        if (getEvents().isEmpty())
        {
//            database.child(FIREBASE_CHILD_EVENT).addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    Event e=dataSnapshot.getValue(Event.class);
//                    if (!Singleton.getInstance().getEventKeyIndexer().containsKey(e.key))
//                        {Singleton.getInstance().events.add(e);
//                        Singleton.getInstance().getEventKeyIndexer().put(e.getKey(),Singleton.getInstance().events.size()-1);}
//                    OverlayItem item = new OverlayItem(e.title, e.description,new GeoPoint(e.lat,e.lon));
//                    item.setMarker(getResources().getDrawable(R.drawable.map_pointer_small,null));
//                    items.add(item);
//                    Marker m= new Marker(map);
//                    m.setOnMarkerClickListener((marker, mapView) -> {
//                        m.closeInfoWindow();
//                        return false;
//                    });
//                    m.setTextLabelBackgroundColor(Color.TRANSPARENT);
//                    m.setTextIcon(e.getTitle());
//                    m.setPosition(new GeoPoint(e.lat,e.lon));
//                    map.getOverlays().add(m);
//                    eventsOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
//                        @Override
//                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
//                            Address address;
//                            IGeoPoint p = item.getPoint();
//                            popUpBtn.setText("View Event");
//                            cancelBtn.setOnClickListener(v -> {
//                                popUpCardView.setVisibility(View.INVISIBLE);
//                            });
//                            popUpBtn.setOnClickListener(v -> {
//                                popUpCardView.setVisibility(View.INVISIBLE);
//                                Intent intent=new Intent(that,EventActivity.class);
//                                intent.putExtra("key",e.getKey());
//                                startActivity(intent);
//                            });
//
//                            try {
//                                address= getAddressFromLonAndLat(p.getLatitude(),p.getLongitude());
//                                if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(e.key)){
//                                    popupBookmarkImageView.setImageResource(R.drawable.bookmark);
//                                }
//                                else{
//                                    popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
//                                }
//                                popupBookmarkImageView.setOnClickListener(v -> {
//                                    if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(e.key)){
//                                        Singleton.getInstance().getUser().bookmarkedEventsID.add(e.key);
//                                        database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").child("" + (Singleton.getInstance().getUser().bookmarkedEventsID.size() - 1)).setValue(e.key);
//                                        popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
//                                    }
//                                    else{
//                                        Singleton.getInstance().getUser().bookmarkedEventsID.remove(e.key);
//                                        database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").setValue(Singleton.getInstance().getUser().bookmarkedEventsID);
//                                        popupBookmarkImageView.setImageResource(R.drawable.bookmark);
//                                    }
//                                    Toast.makeText(that, "nesto", Toast.LENGTH_SHORT).show();
//                                });
//                                popupTextView1.setText(item.getTitle());
//                                popupTextView2.setText(address.getAddressLine(0).substring(0,address.getAddressLine(0).indexOf(",")));
//                                popUpCardView.setVisibility(View.VISIBLE);
//                                Event e=getEvents().get(index);
//                                popUpTextView3.setText(e.getAttendeesID().size()+"/"+e.getMaxOccupancy());
//                                popUpCardView.setVisibility(View.VISIBLE);
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onItemLongPress(int index, OverlayItem item) {
////                Intent i = new Intent(MyPlacesMapsActivity.this, ViewMyPlaceActivity.class);
////                i.putExtra("position", index);
////                startActivityForResult(i, 5);
//                            return true;
//                        }
//                    }, getApplicationContext());
//                    map.getOverlays().add(eventsOverlay);
//                    map.invalidate();
//
//                }
//
//                @Override
//                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    Event event=dataSnapshot.getValue(Event.class);
//                    int index=Singleton.getInstance().getEventKeyIndexer().get(event.key);
//                    getEvents().set(index,event);
//                }
//
//                @Override
//                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                    Event event=dataSnapshot.getValue(Event.class);
//                    int index=Singleton.getInstance().getEventKeyIndexer().get(event.key);
//                    getEvents().remove(index);
//                    Singleton.getInstance().resetEventKeyIndexer();
//                }
//
//                @Override
//                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });

            database.child(FIREBASE_CHILD_EBD).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Date date=new Date();
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    snapshot.getChildren().forEach(dataSnapshot -> {
                        if(String.valueOf(year).compareTo(dataSnapshot.getKey())==0)
                        {
                            dataSnapshot.getChildren().forEach(dataMonth -> {
                                if(Integer.parseInt(dataMonth.getKey())>=month){
                                    dataMonth.getChildren().forEach(dataDay->{
                                        if(month==Integer.parseInt(dataMonth.getKey()) && Integer.parseInt(dataDay.getKey())>=day-1){
                                            dataDay.getChildren().forEach(eventKey->{
                                                Log.d(TAG, "onDataChange: Key of"+year+"/"+dataMonth.getKey()+"/"+dataDay.getKey()+" :"+eventKey.getValue());
                                                database.child(FIREBASE_CHILD_EVENT).child((String) eventKey.getValue()).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Event event=snapshot.getValue(Event.class);
                                                        Log.d(TAG, "onDataChange: "+event);
                                                        if (!Singleton.getInstance().getEventKeyIndexer().containsKey(event.key))
                                                        {
                                                            Singleton.getInstance().getEvents().add(event);
                                                            Singleton.getInstance().getEventKeyIndexer().put(event.getKey(),Singleton.getInstance().getEvents().size()-1);
                                                        }
                                                        else {
                                                            int id=Singleton.getInstance().getEventKeyIndexer().get(event.getKey());
                                                            Singleton.getInstance().getEvents().set(id,event);
                                                        }
                                                        showEvents();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            });
                                        }
                                        else if(month<Integer.parseInt(dataMonth.getKey())){
                                            dataDay.getChildren().forEach(eventKey->{
                                                Log.d(TAG, "onDataChange: Key of"+year+"/"+dataMonth.getKey()+"/"+dataDay.getKey()+" :"+eventKey.getValue());
                                                database.child(FIREBASE_CHILD_EVENT).child((String) eventKey.getValue()).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Event event=snapshot.getValue(Event.class);
                                                        Log.d(TAG, "onDataChange: "+event);
                                                        if (!Singleton.getInstance().getEventKeyIndexer().containsKey(event.key))
                                                        {
                                                            Singleton.getInstance().getEvents().add(event);
                                                            Singleton.getInstance().getEventKeyIndexer().put(event.getKey(),Singleton.getInstance().getEvents().size()-1);
                                                        }
                                                        else {
                                                            int id=Singleton.getInstance().getEventKeyIndexer().get(event.getKey());
                                                            Singleton.getInstance().getEvents().set(id,event);
                                                        }
                                                        showEvents();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            });
                                        }
                                    });
                                }
                                else
                                    Log.d(TAG, "onDataChange:"+dataMonth.getKey()+"="+month);
                            });
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private ArrayList<Event> getEvents(){
        return Singleton.getInstance().getEvents();
    }

    private void filterEvents(String category){
        final ArrayList<OverlayItem> items = new ArrayList<>();
        removeOverlays();
        for (Event e:getEvents()) {
            if(e.category.compareTo(category)==0)
            {
                OverlayItem item = new OverlayItem(e.title, e.key,new GeoPoint(e.lat,e.lon));
                item.setMarker(getResources().getDrawable(R.drawable.map_pointer_small,null));
                items.add(item);
                Marker m= new Marker(map);
                m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        m.closeInfoWindow();
                        return false;
                    }
                });
                m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                m.setTextIcon(e.getTitle());
                m.setPosition(new GeoPoint(e.lat,e.lon));
                map.getOverlays().add(m);
            }
        }
        eventsOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Address address;
                IGeoPoint p = item.getPoint();
                popUpBtn.setText(R.string.view_event);
                cancelBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);

                });
                popUpBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);
                    Intent intent=new Intent(that,EventActivity.class);
                    intent.putExtra("key",item.getSnippet());
                    startActivity(intent);
                });

                try {
                    address= getAddressFromLonAndLat(p.getLatitude(),p.getLongitude());
                    if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(item.getSnippet())){
                        popupBookmarkImageView.setImageResource(R.drawable.bookmark);
                    }
                    else{
                        popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
                    }
                    popupBookmarkImageView.setOnClickListener(v -> {
                        if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(item.getSnippet())){
                            Singleton.getInstance().getUser().bookmarkedEventsID.add(item.getSnippet());
                            database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").child("" + (Singleton.getInstance().getUser().bookmarkedEventsID.size() - 1)).setValue(item.getSnippet());
                            popupBookmarkImageView.setImageResource(R.drawable.bookmarkx);
                        }
                        else{
                            Singleton.getInstance().getUser().bookmarkedEventsID.remove(item.getSnippet());
                            database.child("users").child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").setValue(Singleton.getInstance().getUser().bookmarkedEventsID);
                            popupBookmarkImageView.setImageResource(R.drawable.bookmark);
                        }
                        Toast.makeText(that, "nesto", Toast.LENGTH_SHORT).show();
                    });
                    popupTextView1.setText(item.getTitle());
                    popupTextView2.setText(address.getAddressLine(0).substring(0,address.getAddressLine(0).indexOf(",")));
                    popUpCardView.setVisibility(View.VISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
//                Intent i = new Intent(MyPlacesMapsActivity.this, ViewMyPlaceActivity.class);
//                i.putExtra("position", index);
//                startActivityForResult(i, 5);
                return true;
            }
        }, getApplicationContext());
        map.getOverlays().add(eventsOverlay);
        map.invalidate();
    }

    private void removeOverlays(){
        if ( eventsOverlay!= null) {
            this.map.getOverlays().clear();
            map.invalidate();
        }
    }

    private void location(){

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response =
                        task.getResult(ApiException.class);
            } catch (ApiException ex) {
                switch (ex.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException =
                                    (ResolvableApiException) ex;
                            resolvableApiException
                                    .startResolutionForResult(MainActivity.this,
                                            1);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }


    public Address  getAddressFromLonAndLat(double lat,double lon) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lon, 1);

        return addresses.get(0);
    }

    private void goToGeoPoint(GeoPoint geoPoint,double zoom){
        mapController = map.getController();
        if (mapController != null) {
            mapController.setZoom(zoom);
            mapController.animateTo(geoPoint);
        }
    }

    private void setUpMapClick(){
        MapEventsReceiver mapEventsReceiver=new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Address address;
                popUpBtn.setText(getResources().getText(R.string.create_event));
                cancelBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);
                });
                popUpBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);
                    Intent locationIntent = new Intent(MainActivity.this,CreateEventActivity.class);
                    locationIntent.putExtra("lon", p.getLongitude());
                    locationIntent.putExtra("lat", p.getLatitude());
                    startActivityForResult(locationIntent,1);
                });

                try {
                    address= getAddressFromLonAndLat(p.getLatitude(),p.getLongitude());
                    popupTextView1.setText(address.getAddressLine(0).substring(0,address.getAddressLine(0).indexOf(",")));
                    popupTextView2.setText(address.getLocality());
                    popUpTextView3.setText("");
                    popUpCardView.setVisibility(View.VISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mapEventsReceiver);
        map.getOverlays().add(OverlayEvents);
    }

    private void setUpPointerOverlay(GeoPoint geoPoint){
        ArrayList<OverlayItem> items=new ArrayList<>();
        OverlayItem item = new OverlayItem("Address", "Searched address",geoPoint);
        item.setMarker(getResources().getDrawable(R.drawable.red_pointer_small,null));
        items.add(item);
        map.getOverlays().add(new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        },that));
        map.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Event getEvent(String filter){
        List<Event> events = getEvents().stream().filter(e->e.title.toLowerCase().compareTo(filter.toLowerCase())==0).collect(Collectors.toList());
        if(events.size()>0)
            return events.get(0);
        else
            return null;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, 50,50);
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }

    private void filterMyEvents(){
        final ArrayList<OverlayItem> items = new ArrayList<>();
        removeOverlays();
        for (Event e:getEvents()) {
            if(e.attendeesID.contains(auth.getCurrentUser().getUid()))
            {
                OverlayItem item = new OverlayItem(e.title, e.key,new GeoPoint(e.lat,e.lon));
                item.setMarker(getResources().getDrawable(R.drawable.map_pointer_small,null));
                items.add(item);
                Marker m= new Marker(map);
                m.setOnMarkerClickListener((marker, mapView) -> {
                    m.closeInfoWindow();
                    return false;
                });
                m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                m.setTextIcon(e.getTitle());
                m.setPosition(new GeoPoint(e.lat,e.lon));
                map.getOverlays().add(m);
            }
        }
        eventsOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Address address;
                IGeoPoint p = item.getPoint();
                popUpBtn.setText(R.string.view_event);
                cancelBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);

                });
                popUpBtn.setOnClickListener(v -> {
                    popUpCardView.setVisibility(View.INVISIBLE);
                    Intent intent=new Intent(that,EventActivity.class);
                    intent.putExtra("key",item.getSnippet());
                    startActivity(intent);
                });

                try {
                    address= getAddressFromLonAndLat(p.getLatitude(),p.getLongitude());
                    popupTextView1.setText(item.getTitle());
                    popupTextView2.setText(address.getAddressLine(0).substring(0,address.getAddressLine(0).indexOf(",")));
                    popUpCardView.setVisibility(View.VISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
//                Intent i = new Intent(MyPlacesMapsActivity.this, ViewMyPlaceActivity.class);
//                i.putExtra("position", index);
//                startActivityForResult(i, 5);
                return true;
            }
        }, getApplicationContext());
        map.getOverlays().add(eventsOverlay);
        map.invalidate();
    }
    private void showFriends() {

        if(isFriendsOn){
            isFriendsOn = false;
            Drawable drawable=getResources().getDrawable(R.drawable.friends,null);
            drawable.setTint(getColor(R.color.colorPrimary));
            fabFriends.setImageDrawable(drawable);
            showEvents();
            setUpMapClick();
            map.getOverlays().add(myLocationNewOverlay);
            return;
        }
        removeOverlays();
        ArrayList<String> friends=Singleton.getInstance().getUser().friends;
        Drawable drawable=getResources().getDrawable(R.drawable.close,null);
        drawable.setTint(getColor(R.color.colorPrimary));
        fabFriends.setImageDrawable(drawable);
        isFriendsOn =true;
        if (friendsBM==null)
            friendsBM=new HashMap<>();
        if (friendsUsers==null)
            friendsUsers=new HashMap<>();
        if (!friends.isEmpty())
        {
            for (String uid:friends) {
                ArrayList<OverlayItem> items = new ArrayList<>();
                if(friendsUsers.containsKey(uid)){
                    User user=friendsUsers.get(uid);
                    if(user.locLat==0 || user.locLon==0)
                        return;
                    OverlayItem item = new OverlayItem(user.firstName+" "+user.lastName, user.uID,new GeoPoint(user.locLat,user.locLon));
                    Bitmap bitmap1= friendsBM.get(uid);
                    item.setMarker(new BitmapDrawable(getResources(),bitmap1));
                    items.add(item);
                    Overlay overlay= new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
                            Intent intent=new Intent(that, ProfileActivity.class);
                            intent.putExtra("type","other");
                            intent.putExtra("key",item.getSnippet());
                            startActivity(intent);
                            return false;
                        }

                        @Override
                        public boolean onItemLongPress(int index, OverlayItem item) {
                            return false;
                        }
                    }, that);
                    map.getOverlays().add(overlay);
                    Marker m= new Marker(map);
                    m.setOnMarkerClickListener((marker, mapView) -> {
                        m.closeInfoWindow();
                        return false;
                    });
                    m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                    m.setTextIcon(user.firstName+" "+user.lastName);
                    m.setPosition(new GeoPoint(user.locLat,user.locLon));
                    map.getOverlays().add(m);
                    map.invalidate();
                }
                else
                    storage.child(FIREBASE_CHILD_USER).child(uid).child("profile").getBytes(5*ONE_MEGA_BYTE).addOnSuccessListener(bytes ->
                            database.child(FIREBASE_CHILD_USER).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user=dataSnapshot.getValue(User.class);
                                    user.uID=uid;
                                    if(user.locLat==0 || user.locLon==0)
                                        return;
                                    OverlayItem item = new OverlayItem(user.firstName+" "+user.lastName, user.uID,new GeoPoint(user.locLat,user.locLon));
                                    Bitmap bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap bitmap1= Bitmap.createScaledBitmap(bitmap,100,100,false);
                                    friendsBM.put(uid,bitmap1);
                                    friendsUsers.put(uid,user);
                                    item.setMarker(new BitmapDrawable(getResources(),bitmap1));
                                    items.add(item);
                                    Overlay overlay= new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                        @Override
                                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
                                            Intent intent=new Intent(that, ProfileActivity.class);
                                            intent.putExtra("type","other");
                                            intent.putExtra("key",item.getSnippet());
                                            startActivity(intent);
                                            return false;
                                        }

                                        @Override
                                        public boolean onItemLongPress(int index, OverlayItem item) {
                                            return false;
                                        }
                                    }, that);
                                    map.getOverlays().add(overlay);
                                    Marker m= new Marker(map);
                                    m.setOnMarkerClickListener((marker, mapView) -> {
                                        m.closeInfoWindow();
                                        return false;
                                    });
                                    m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                                    m.setTextIcon(user.firstName+" "+user.lastName);
                                    m.setPosition(new GeoPoint(user.locLat,user.locLon));
                                    map.getOverlays().add(m);
                                    map.invalidate();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }));
            }
        }
    }

    private void showFriendsOnZoom(){
        final ArrayList<OverlayItem> items = new ArrayList<>();
        removeOverlays();
        ArrayList<String> friends=Singleton.getInstance().getUser().friends;
        if (!friends.isEmpty()) {
            for (String uid : friends) {
                if (friendsUsers.containsKey(uid)) {
                    User user = friendsUsers.get(uid);
                    if (user.locLat == 0 || user.locLon == 0)
                        return;
                    OverlayItem item = new OverlayItem(user.firstName + " " + user.lastName, user.uID, new GeoPoint(user.locLat, user.locLon));
                    Bitmap bitmap1 = friendsBM.get(uid);
                    item.setMarker(new BitmapDrawable(getResources(), bitmap1));
                    items.add(item);
                    Overlay overlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
                            Toast.makeText(MainActivity.this, "" + item.getTitle(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(that, ProfileActivity.class);
                            intent.putExtra("type", "other");
                            intent.putExtra("key", uid);
                            startActivity(intent);
                            return false;
                        }

                        @Override
                        public boolean onItemLongPress(int index, OverlayItem item) {
                            return false;
                        }
                    }, that);
                    map.getOverlays().add(overlay);
                    Marker m = new Marker(map);
                    m.setOnMarkerClickListener((marker, mapView) -> {
                        m.closeInfoWindow();
                        return false;
                    });
                    m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                    m.setTextLabelForegroundColor(Color.WHITE);
                    m.setTextIcon(user.firstName + " " + user.lastName);
                    m.setPosition(new GeoPoint(user.locLat, user.locLon));
                    map.getOverlays().add(m);
                    map.invalidate();
                }
            }
        }
    }

    private void showFriendsNew() {

        if(isFriendsOn){
            isFriendsOn = false;
            Drawable drawable=getResources().getDrawable(R.drawable.friends,null);
            drawable.setTint(getColor(R.color.colorPrimary));
            fabFriends.setImageDrawable(drawable);
            showEvents();
            setUpMapClick();
            map.getOverlays().add(myLocationNewOverlay);
            return;
        }
        removeOverlays();
        ArrayList<String> friends=Singleton.getInstance().getUser().friends;
        Drawable drawable=getResources().getDrawable(R.drawable.close,null);
        drawable.setTint(getColor(R.color.colorPrimary));
        fabFriends.setImageDrawable(drawable);
        isFriendsOn =true;
        if (friendsBM==null)
            friendsBM=new HashMap<>();
        if (friendsUsers==null)
            friendsUsers=new HashMap<>();
        if (!friends.isEmpty())
        {
            for (String uid:friends) {
                ArrayList<OverlayItem> items = new ArrayList<>();
                if(friendsBM.containsKey(uid)){
                    database.child(FIREBASE_CHILD_USER).child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user=snapshot.getValue(User.class);
                            user.uID=uid;
                            friendsUsers.put(uid,user);
                            if(user.locLat==0 || user.locLon==0)
                                return;
                            reloadFriends();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                    storage.child(FIREBASE_CHILD_USER).child(uid).child("profile").getBytes(5*ONE_MEGA_BYTE).addOnSuccessListener(bytes ->
                            database.child(FIREBASE_CHILD_USER).child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user=dataSnapshot.getValue(User.class);
                                    user.uID=uid;
                                    if(user.locLat==0 || user.locLon==0)
                                        return;
                                    Bitmap bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap bitmap1= Bitmap.createScaledBitmap(bitmap,100,100,false);
                                    friendsBM.put(uid,bitmap1);
                                    friendsUsers.put(uid,user);
                                    reloadFriends();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }));
            }
        }
    }

    private void reloadFriends(){
        removeOverlays();
        ArrayList<OverlayItem> items=new ArrayList<>();
        ArrayList<String> friends=Singleton.getInstance().getUser().friends;
        for (String uid:friends) {
            if(friendsUsers.containsKey(uid)) {
                User user=friendsUsers.get(uid);
                OverlayItem item = new OverlayItem(user.firstName + " " + user.lastName, user.uID, new GeoPoint(user.locLat, user.locLon));
                Bitmap bitmap1 = friendsBM.get(user.uID);
                item.setMarker(new BitmapDrawable(getResources(), bitmap1));
                items.add(item);
                Overlay overlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        Intent intent = new Intent(that, ProfileActivity.class);
                        intent.putExtra("type", "other");
                        intent.putExtra("key", item.getSnippet());
                        startActivity(intent);
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }, that);
                map.getOverlays().add(overlay);
                Marker m = new Marker(map);
                m.setOnMarkerClickListener((marker, mapView) -> {
                    m.closeInfoWindow();
                    return false;
                });
                m.setTextLabelBackgroundColor(Color.TRANSPARENT);
                m.setTextIcon(user.firstName + " " + user.lastName);
                m.setPosition(new GeoPoint(user.locLat, user.locLon));
                map.getOverlays().add(m);
            }
        }
        map.invalidate();
    }
    //endregion
}
