package com.example.grandweather.gson;

import com.google.gson.annotations.SerializedName;

public class Hourly {
    @SerializedName("cond_code")
    public String hourlyCondCode;

    @SerializedName("cond_txt")
    public String hourlyCondText;

    @SerializedName("time")
    public String hourlyTime;

    @SerializedName("tmp")
    public String hourlyTmp;
}
