package com.example.weatherjournal2.ui.Models;

import com.example.weatherjournal2.WeatherRVAdapter;
import com.example.weatherjournal2.WeatherRVModal;
import java.util.ArrayList;


public class WeatherManager {
    private static WeatherManager instance;
    private Weather currentWeather;

    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    private WeatherManager() {
    }

    public static synchronized WeatherManager getInstance() {
        if (instance == null) {
            instance = new WeatherManager();
        }
        return instance;
    }

    public Weather getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(Weather weather) {
        this.currentWeather = weather;
    }

}
