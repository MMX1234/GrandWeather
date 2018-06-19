package com.example.grandweather.gson;

import com.google.gson.annotations.SerializedName;
/*
cloud: "95",
cond_code: "104",
cond_txt: "阴",
fl: "16",
hum: "85",
pcpn: "0.0",
pres: "1011",
tmp: "16",
vis: "20",
wind_deg: "259",
wind_dir: "西风",
wind_sc: "1",
wind_spd: "5"
*/
public class Now {

    //天气状况
    @SerializedName("cond_txt")
    public String condTxt;

    //温度
    @SerializedName("tmp")
    public String tmp;

    @SerializedName("cond_code")
    public String condCode;
}
