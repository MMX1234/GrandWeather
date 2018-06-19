package com.example.grandweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.grandweather.gson.DailyForecast;
import com.example.grandweather.gson.Hourly;
import com.example.grandweather.gson.Weather;
import com.example.grandweather.service.AutoUpdateService;
import com.example.grandweather.util.HttpUtil;
import com.example.grandweather.util.Utility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends Activity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView nowCondText;
    private ImageView nowCondImageView;
    private TextView tmpText;

    private LinearLayout hourlyLayout;
    private LineChart hourlyLineChart;

    private LinearLayout forecastLayout;
    private LineChart forecastLineChart;

    public SwipeRefreshLayout swipeRefresh;
    private ImageView bingImgView;
    private String weatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //状态栏沉浸
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //初始化各控件
        weatherLayout = findViewById(R.id.weatherLayout);
        titleCity = findViewById(R.id.titleCityTextView);
        titleUpdateTime = findViewById(R.id.titleUpdateTimeTextView);
        nowCondText = findViewById(R.id.nowCondTextView);
        nowCondImageView = findViewById(R.id.nowCondImageView);
        tmpText = findViewById(R.id.nowTmpTextView);

        bingImgView = findViewById(R.id.bingPicImageView);

        hourlyLayout = findViewById(R.id.hourlyLayout);
        hourlyLineChart = findViewById(R.id.hourlyLineChart);

        forecastLayout = findViewById(R.id.forecastLayout);
        forecastLineChart = findViewById(R.id.forecastLineChart);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = findViewById(R.id.drawerLayout);
        navButton = findViewById(R.id.navButton);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);

        swipeRefresh.setRefreshing(true);
        swipeRefresh.isRefreshing();
        swipeRefresh.setRefreshing(false);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPicStr = preferences.getString("bing_pic", null);
        if (bingPicStr != null) {
            Glide.with(this).load(bingPicStr).into(bingImgView);
        } else loadBingPic();

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String weatherString = preferences.getString("weather", null);
                Weather weather = Utility.handleWeatherResponse(weatherString);
                weatherId = weather.basic.cityId;
                requestWeather(weatherId);
            }
        });
    }


    //加载必应每日一图
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingImgView);
                    }
                });
            }
        });
    }

    //根据天气id请求城市天气信息
    public void requestWeather(String weatherId) {

        String weatherUrl = "https://free-api.heweather.com/s6/weather?key=482f6359e6604305a094f43774fce965&location=" + weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.i("responseText", responseText);


                WeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "onResponse获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "onFailure获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = (weather.update.updateTime).substring(11);
        String nowTmp = weather.now.tmp + " ℃";
        String nowCondCode = "cond_code" + weather.now.condCode + ".png";
        String nowCond = weather.now.condTxt;
        titleCity.setText(cityName);
        tmpText.setText(nowTmp);
        titleUpdateTime.setText(updateTime);

        formatPic(200, nowCondCode, nowCondImageView);
        nowCondText.setText(nowCond);


        // 千里眼(时)EX
        hourlyLayout.removeAllViews();
        for (Hourly hourly : weather.hourlies) {
            View view = LayoutInflater.from(this).inflate(R.layout.hourly_item, hourlyLayout, false);
            TextView hourlyTimeView = view.findViewById(R.id.hourlyTimeView);
            ImageView hourlyCondCodeImageView = view.findViewById(R.id.hourlyCondCodeImageView);
            TextView hourlyCondView = view.findViewById(R.id.hourlyCondView);
            TextView hourlyTmpView = view.findViewById(R.id.hourlyTmpView);

            String timeStr = (hourly.hourlyTime).substring(11);
            hourlyTimeView.setText(timeStr);

            String code = "cond_code" + hourly.hourlyCondCode + ".png";
            formatPic(200, code, hourlyCondCodeImageView);

            hourlyCondView.setText(hourly.hourlyCondText);
            hourlyTmpView.setText(hourly.hourlyTmp + " ℃");

            hourlyLayout.addView(view);
        }


        // 温度图表
        LineChartManage chartManage = new LineChartManage();
        int color = Color.WHITE;
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (Hourly hourly : weather.hourlies) {
            entries.add(new Entry(i++, Float.valueOf(hourly.hourlyTmp)));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries, "温度");
        LineData data = new LineData(lineDataSet);
        hourlyLineChart.setData(data);
        chartManage.initChart(hourlyLineChart);
        chartManage.initLineDataSet(lineDataSet, color, LineDataSet.Mode.CUBIC_BEZIER);

        // 千里眼(日)EX
        forecastLayout.removeAllViews();
        for (DailyForecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);

            TextView dateText = view.findViewById(R.id.dateTextView);
            TextView condDayText = view.findViewById(R.id.condTextView);
            ImageView forecastCondDayCodeImageView = view.findViewById(R.id.forecastCondDayCodeImageView);
            TextView tmpMaxText = view.findViewById(R.id.tmpMaxTextView);
            TextView tmpMinText = view.findViewById(R.id.tmpMinTextView);

            String dateStr = (forecast.dateTime).substring(5);
            dateText.setText(dateStr);

            String code = "cond_code" + forecast.condTxtDayCode + ".png";
            formatPic(200, code, forecastCondDayCodeImageView);

            condDayText.setText(forecast.condTxtDay);
            tmpMaxText.setText(forecast.tmpMax + " ℃");
            tmpMinText.setText(forecast.tmpMin + " ℃");

            forecastLayout.addView(view);
        }
        // 温度图表
        chartManage = new LineChartManage();
        color = Color.WHITE;
        List<Entry> entriesMax = new ArrayList<>();
        List<Entry> entriesMin = new ArrayList<>();
        i = 0;
        for (DailyForecast forecast : weather.forecastList) {
            entriesMax.add(new Entry(i++, Float.valueOf(forecast.tmpMax)));
            i--;
            entriesMin.add(new Entry(i++, Float.valueOf(forecast.tmpMin)));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSetMax = new LineDataSet(entriesMax, "温度");
        LineDataSet lineDataSetMin = new LineDataSet(entriesMin, "温度");
        data = new LineData(lineDataSetMax, lineDataSetMin);
        forecastLineChart.setData(data);
        chartManage.initChart(forecastLineChart);
        chartManage.initLineDataSet(lineDataSetMax, color, LineDataSet.Mode.CUBIC_BEZIER);
        chartManage.initLineDataSet(lineDataSetMin, color, LineDataSet.Mode.CUBIC_BEZIER);

        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    // 显示天气状态图片
    private void formatPic(int newHeight, String code, ImageView imageView) {
        Bitmap originalBitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/" + code));
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        int newWidth = 200;

        float scale = ((float) newHeight) / originalHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true);
        imageView.setImageBitmap(bitmap);
    }
}
