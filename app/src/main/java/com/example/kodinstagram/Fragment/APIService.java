package com.example.kodinstagram.Fragment;

import com.example.kodinstagram.Notifications.MyResponse;
import com.example.kodinstagram.Notifications.Sender;
import com.example.kodinstagram.Notifications.MyResponse;
import com.example.kodinstagram.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAPLcv-6w:APA91bH_gx67l3SFVRdwNqiWIHV5OJZDlBOLrwaTl_VD1ZFzinGlTNakNr6ZrJD7pHbnZ2_HIrKmhoTn7h9iDbEoku7e3tytqHHccJp5H4-pC3NGeZZXCBVI07hLfPmhYYgCnsotmWIh"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
