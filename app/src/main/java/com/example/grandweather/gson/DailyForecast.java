package com.example.grandweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Tridays on 2018.03.07.
 */

public class DailyForecast {

    //天气状况白天
    @SerializedName("cond_txt_d")
    public String condTxtDay;

    @SerializedName("cond_code_d")
    public String condTxtDayCode;

    //天气状况夜晚
    @SerializedName("cond_txt_n")
    public String condTxtNight;

    @SerializedName("date")
    public String dateTime;

    @SerializedName("tmp_max")
    public String tmpMax;

    @SerializedName("tmp_min")
    public String tmpMin;
}
