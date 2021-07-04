package com.example.event_handler.rest_api;

import com.example.event_handler.models.AddFriend;
import com.example.event_handler.models.NewEvent;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IService {

    @Headers({

            "Content-type: application/json"

    })
    @POST("new_event")
    Call<String> newEvent(@Body NewEvent event);

    @Headers({

            "Content-type: application/json"

    })
    @POST("friend/request")
    Call<String> addFriend(@Body AddFriend addFriend);
}
