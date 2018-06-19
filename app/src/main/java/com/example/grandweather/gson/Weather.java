package com.example.grandweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Tridays on 2018.03.07.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public Update update;

    @SerializedName("daily_forecast")
    public List<DailyForecast> forecastList;

    @SerializedName("hourly")
    public List<Hourly> hourlies;
}
