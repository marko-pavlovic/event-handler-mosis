package com.example.event_handler;

import com.google.firebase.database.Exclude;

public class Event {

    public String eventName, category, startDate, endDate, description;
    public String latitude, longitude;
    public String imgURL, creator;

    @Exclude
    public String key;

    public Event() {
    }

    public Event(String evName, String cat, String startD, String endD,String desc, String lat, String lng, String url, String creat)
    {
        this.eventName = evName;
        this.category = cat;
        this.startDate = startD;
        this.endDate = endD;
        this.description = desc;
        this.latitude = lat;
        this.longitude = lng;
        this.creator = creat;
    }

}
