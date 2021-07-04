package com.example.event_handler.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.example.event_handler.EventActivity;
import com.example.event_handler.R;
import com.example.event_handler.Singleton;
import com.example.event_handler.models.Event;

import java.util.List;

public class EventsRecyclerAdapter extends RecyclerView.Adapter<EventsRecyclerAdapter.ViewHolder> {
    private List<Event> events;
    private Context ctx;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD_USER ="users";
    private String key;

    public EventsRecyclerAdapter(Context ctx,List<Event> events,String key)  {
        this.events = events;
        this.ctx = ctx;
        this.key=key;
        auth=FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.events_rv_card_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {
        Picasso.with(ctx).load(R.drawable.event_sample_photo).fit().into(holder.eventImage);
        holder.eventTitle.setText(events.get(position).title);
        holder.eventAddress.setText(events.get(position).address);
        holder.eventDate.setText(events.get(position).dateTime);
        holder.eventAttendees.setText(events.get(position).getAttendeesID().size()+"");
        if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(events.get(position).key))
            holder.bookmarkImageView.setImageResource(R.drawable.bookmark);

        else
            holder.bookmarkImageView.setImageResource(R.drawable.bookmarkx);

        holder.bookmarkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Singleton.getInstance().getUser().bookmarkedEventsID.contains(events.get(position).key)) {
                    Singleton.getInstance().getUser().bookmarkedEventsID.add(events.get(position).key);
                    database.child(FIREBASE_CHILD_USER).child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").child("" + (Singleton.getInstance().getUser().bookmarkedEventsID.size() - 1)).setValue(events.get(position).key);
                    holder.bookmarkImageView.setImageResource(R.drawable.bookmarkx);
                }
                else{
                    Singleton.getInstance().getUser().bookmarkedEventsID.remove(events.get(position).key);
                    database.child(FIREBASE_CHILD_USER).child(auth.getCurrentUser().getUid()).child("bookmarkedEventsID").setValue(Singleton.getInstance().getUser().bookmarkedEventsID);
                    holder.bookmarkImageView.setImageResource(R.drawable.bookmark);
                    if(key=="bookmark") {
                        events.remove(position);
                        notifyItemRemoved(position);
                        //notifyItemRangeChanged(position, events.size());
                    }
                }
            }
        });
        holder.cardView.setOnClickListener(v -> {
            Toast.makeText(ctx, ""+events.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ctx, EventActivity.class);
            intent.putExtra("key",events.get(position).getKey());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        TextView eventDate;
        TextView eventAddress;
        TextView eventAttendees;
        ImageView bookmarkImageView;
        ImageView eventImage;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            eventTitle = itemView.findViewById(R.id.rv_cv_event_title);
            cardView = itemView.findViewById(R.id.rv_events_card_view);
            eventDate = itemView.findViewById(R.id.rv_cv_event_date);
            eventAddress = itemView.findViewById(R.id.rv_cv_event_address);
            eventAttendees = itemView.findViewById(R.id.rv_cv_event_attendee_count);
            bookmarkImageView=itemView.findViewById(R.id.rv_cv_bookmark_image);
            eventImage=itemView.findViewById(R.id.rv_cv_event_image);
        }
    }
}


