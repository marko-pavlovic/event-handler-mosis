package com.example.event_handler;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import Fragments.EventsFragment;
import Fragments.FriendsFragment;
import Fragments.MainFragment;

public class GlavniActivity extends AppCompatActivity {
    private ActionBar toolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new MainFragment();
                    toolbar.setTitle("Main");
                    loadFragment(fragment);
                    return true;
//                case R.id.navigation_events:
//                    fragment = new EventsFragment();
//                    toolbar.setTitle("Events");
//                    loadFragment(fragment);
//                    return true;
                case R.id.navigation_friends:
                    fragment = new FriendsFragment();
                    toolbar.setTitle("Friends");
                    loadFragment(fragment);
                    return true;
            }

            return true;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.glavni_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.show_map:
                Intent i = new Intent(this, MapActivity.class);
                startActivity(i);
                break;
            case R.id.add_event:
                break;
            case R.id.add_friend:
                break;
            case R.id.show_events:
                //TODO promeni na odgovarajuci nav item
                toolbar.setTitle("Events");
                loadFragment(new EventsFragment());
                break;
            case R.id.show_friends:
                toolbar.setTitle("Friends");
                loadFragment(new FriendsFragment());
                break;
            case R.id.change_interest:
                break;
            case R.id.settings:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glavni);

        toolbar = getSupportActionBar();
        toolbar.setTitle("MAIN");

        BottomNavigationView navView = findViewById(R.id.nav_glavni);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment(new MainFragment());
    }

}
