package com.example.event_handler;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseSingleton {

    private static final FirebaseSingleton instance = new FirebaseSingleton();

    public DatabaseReference databaseReference;
    public StorageReference storageReference;

    private FirebaseSingleton() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public static FirebaseSingleton getInstance() { return instance; }

}
