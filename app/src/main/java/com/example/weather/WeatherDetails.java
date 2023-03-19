package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WeatherDetails extends AppCompatActivity {
    // View refs
    protected TextView txtCityName;
    protected TextView txtDescription;
    protected TextView txtTemperature;

    protected TextView labelWindSpeed;
    protected TextView txtWindSpeed;
    protected TextView labelFeelsLike;
    protected TextView txtFeelsLike;

    protected TextView labelHumidity;
    protected TextView txtHumidity;
    protected TextView labelPressure;
    protected TextView txtPressure;

    protected TextView labelSunRise;
    protected TextView txtSunRise;
    protected TextView labelSunSet;
    protected TextView txtSunSet;

    protected TextView labelTimeZone;
    protected TextView txtTimeZone;
    protected TextView labelLocalTime;
    protected TextView txtLocalTime;

    // API
    private RequestQueue queue;
    protected String API_URL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        // Init refs
        initRefs();

        // Init queue for fetching API
        queue = Volley.newRequestQueue(this);

        // Get data passed from HomeScreen
        handleDataFromHomeScreen();

        fetchWeatherData();
    }

    protected void initRefs() {
        txtCityName = findViewById(R.id.txtCityName);
        txtDescription = findViewById(R.id.txtDescription);
        txtTemperature = findViewById(R.id.txtTemperature);

        labelWindSpeed = findViewById(R.id.labelWindSpeed);
        txtWindSpeed = findViewById(R.id.txtWindSpeed);
        labelFeelsLike = findViewById(R.id.labelFeelsLike);
        txtFeelsLike = findViewById(R.id.txtFeelsLike);

        labelHumidity = findViewById(R.id.labelHumidity);
        txtHumidity = findViewById(R.id.txtHumidity);
        labelPressure = findViewById(R.id.labelPressure);
        txtPressure = findViewById(R.id.txtPressure);

        labelSunRise = findViewById(R.id.labelSunRise);
        txtSunRise = findViewById(R.id.txtSunRise);
        labelSunSet = findViewById(R.id.labelSunSet);
        txtSunSet = findViewById(R.id.txtSunSet);

        labelTimeZone = findViewById(R.id.labelTimeZone);
        txtTimeZone = findViewById(R.id.txtTimeZone);
        labelLocalTime = findViewById(R.id.labelLocalTime);
        txtLocalTime = findViewById(R.id.txtLocalTime);
    }

    protected void handleDataFromHomeScreen() {

        String type = getIntent().getStringExtra("TYPE");
        if (type.equals("FETCH_BY_CITY_NAME")) {
            String cityName = getIntent().getStringExtra("CITY_NAME");
            if (cityName.length() > 0) {
                API_URL = String.format("https://api.openweathermap.org/data/2.5/weather?units=metric&appidMY_API_KEY&q=%s", cityName);
            }
        }
        else if (type.equals("FETCH_BY_LONG_LAT")) {
            double longitude = getIntent().getDoubleExtra("LONGITUDE", 0);
            double latitude = getIntent().getDoubleExtra("LATITUDE", 0);
            API_URL = String.format("https://api.openweathermap.org/data/2.5/weather?units=metric&appid=MY_API_KEY&lon=%s&lat=%s", longitude, latitude);

        }
    }

    @SuppressLint("DefaultLocale")
    private void parseResponseAndUpdateUI(String response) {
        String city = "";
        String description = "";
        double temperature = 0;

        double windSpeed = 0;
        double feelsLike = 0;

        int humidity = 0;
        int pressure = 0;


        long sunRise = 0;
        long sunSet = 0;
        long timeZone = 0;

        try {
            JSONObject weather = new JSONObject(response);

            city = weather.getString("name");
            description = weather.getJSONArray("weather").getJSONObject(0).getString("main");
            temperature = weather.getJSONObject("main").getDouble("temp");

            windSpeed = weather.getJSONObject("wind").getDouble("speed");
            feelsLike = weather.getJSONObject("main").getDouble("feels_like");

            humidity = weather.getJSONObject("main").getInt("humidity");
            pressure = weather.getJSONObject("main").getInt("pressure");

            sunRise = weather.getJSONObject("sys").getInt("sunrise");
            sunSet = weather.getJSONObject("sys").getInt("sunset");

            timeZone = weather.getInt("timezone");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        calculateTimeAndSetBackground((new Date()).getTime(), sunRise * 1000, sunSet * 1000);

        txtCityName.setText(city);
        txtDescription.setText(description);
        txtTemperature.setText(getString(R.string.temp, (int)temperature ));
        txtWindSpeed.setText(String.format("%s%s", windSpeed, getString(R.string.meter_per_second)));

        txtFeelsLike.setText(String.format("%d%s", (int) feelsLike, getString(R.string.degree_celcius)));

        txtHumidity.setText(String.format("%d%s", humidity, getString(R.string.modulas)));
        txtPressure.setText(String.format("%d%s", pressure, getString(R.string.pascal)));

        txtSunRise.setText(getStringDateFromTimestamp((sunRise + timeZone) * 1000));
        txtSunSet.setText(getStringDateFromTimestamp((sunSet + timeZone) * 1000));

        long localTimestamp = (new Date()).getTime() + timeZone * 1000;
        txtTimeZone.setText(String.format("%s%s", getString(R.string.gmt), getNumberWithSign(timeZone / 3600)));
        txtLocalTime.setText(getStringDateFromTimestamp(localTimestamp));

    }

    protected void calculateTimeAndSetBackground(long currentTimeInMS, long sunRiseInMS, long sunSetInMS) {
        int backgroundImageId;
        int color;

        if (currentTimeInMS < sunRiseInMS) {
            backgroundImageId = R.drawable.midnight_sky; // Mid night
            color = Color.WHITE;
        }
        else if (currentTimeInMS < sunSetInMS) {
            if (currentTimeInMS < sunRiseInMS + 3600 * 1000) {
                backgroundImageId = R.drawable.sunrise_sky; // Sun Rise
                color = Color.WHITE;
            }
            else if (currentTimeInMS + 3600 * 1000 < sunSetInMS) {
                backgroundImageId = R.drawable.day_sky; // During day
                color = Color.BLACK;
            }
            else {
                backgroundImageId = R.drawable.sunset_sky; // Sun set
                color = Color.BLACK;
            }
        }
        else {
            backgroundImageId = R.drawable.evening_sky; // Evening
            color = Color.WHITE;
        }

        View currentView = findViewById(R.id.viewWeatherDetails);
        currentView.getRootView().setBackgroundResource(backgroundImageId);
        setAllTextsColor(color);
    }

    protected void setAllTextsColor(int color) {
        txtCityName.setTextColor(color);
        txtDescription.setTextColor(color);
        txtTemperature.setTextColor(color);

        labelWindSpeed.setTextColor(color);
        txtWindSpeed.setTextColor(color);
        labelFeelsLike.setTextColor(color);
        txtFeelsLike.setTextColor(color);

        labelHumidity.setTextColor(color);
        txtHumidity.setTextColor(color);
        labelPressure.setTextColor(color);
        txtPressure.setTextColor(color);

        labelSunRise.setTextColor(color);
        txtSunRise.setTextColor(color);
        labelSunSet.setTextColor(color);
        txtSunSet.setTextColor(color);

        labelTimeZone.setTextColor(color);
        txtTimeZone.setTextColor(color);
        labelLocalTime.setTextColor(color);
        txtLocalTime.setTextColor(color);
    }

    protected String getStringDateFromTimestamp(long timestampInMS) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone(getString(R.string.gmt)));
        return sdf.format(timestampInMS);
    }

    protected String getNumberWithSign(long number) {
        if (number > 0) return getString(R.string.plus_sign) + number;
        return "" + number;
    }

    protected void fetchWeatherData() {
        if (!(queue != null && API_URL.length() > 0)) return;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_URL,
                this::parseResponseAndUpdateUI,
                error -> txtCityName.setText(getString(R.string.message_city_not_found))
        );
        queue.add(stringRequest);
    }
}
