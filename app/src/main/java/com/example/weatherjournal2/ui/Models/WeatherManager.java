package com.example.weatherjournal2.ui.Models;
public class WeatherManager {
    private static WeatherManager instance;
    private Weather currentWeather;

    private WeatherManager() {
        // Private constructor to enforce singleton pattern
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
