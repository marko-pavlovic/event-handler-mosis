package com.example.event_handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.event_handler.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.event_handler.adapters.PendingRequestsAdapter;

import java.util.ArrayList;

public class PendingRequestDialog extends AppCompatDialogFragment {
    RecyclerView mRecyclerView;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD_USER ="users";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        auth=FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view =inflater.inflate(R.layout.pending_request_dialog,null);
        builder.setView(view)
                .setTitle("Pending Requests")
                .setNegativeButton("cancel",(dialog, which) -> {
                   dismiss();
                });

        mRecyclerView=view.findViewById(R.id.pending_request_recycler_view);
        ArrayList<User> users=new ArrayList<User>();
        PendingRequestsAdapter adapter=new PendingRequestsAdapter(getActivity(),users);
        for (String s:Singleton.getInstance().getUser().pendingRequests) {

            database.child(FIREBASE_CHILD_USER).child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    user.uID = s;
                    users.add(user);
                    adapter.notifyItemInserted(users.size()-1);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return builder.create();
    }
}
