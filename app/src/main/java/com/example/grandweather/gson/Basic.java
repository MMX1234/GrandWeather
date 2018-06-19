package com.example.grandweather.gson;

import com.google.gson.annotations.SerializedName;
//https://free-api.heweather.com/s6/weather?key=482f6359e6604305a094f43774fce965&location=连云港

public class Basic {
    @SerializedName("cid")
    public String cityId;

    @SerializedName("location")
    public String cityName;

}
