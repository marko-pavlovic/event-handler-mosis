package com.example.event_handler;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.event_handler.models.CategoryList;

import java.util.ArrayList;

public class CategoryInit extends AppCompatActivity {

    ArrayList<CategoryList> categories;
    private DatabaseReference database;
    public static final String FIREBASE_CHILD="categories";
    private static final String TAG = "Tagggg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popunjavanje);
        database= FirebaseDatabase.getInstance().getReference();
        categories=new ArrayList<CategoryList>();
        final CategoryList c1=new CategoryList();
        c1.categories=new ArrayList<String>();
        c1.categories.add("Sport");
        c1.categories.add("Movie");
        c1.categories.add("Birthday");
        c1.categories.add("Culture");
        c1.categories.add("Education");
        c1.categories.add("Festival");
        c1.categories.add("Gallery");
        c1.categories.add("Theatre");
        c1.categories.add("Movie");
        c1.categories.add("Music");
        c1.categories.add("Party");
        c1.categories.add("Photography");
        c1.categories.add("Religion");
        c1.categories.add("Seminar");
        c1.categories.add("Shopping");




        Button btn =findViewById(R.id.buttonPop);
           btn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   addNewCategory(c1);
               }
           });

    }

    public void addNewCategory(CategoryList c){
        database.child(FIREBASE_CHILD).setValue(c);
    }
}
