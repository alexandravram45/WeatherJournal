package com.example.weatherjournal2.ui.Models;

public class Weather {
    private double longitude;
    private double latitude;
    private String cityName;
    private String temperature;
    private String conditionIconString;

    public Weather(double longitude, double latitude, String cityName, String temperature, String conditionIconString) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.cityName = cityName;
        this.temperature = temperature;
        this.conditionIconString = conditionIconString;
    }

    public String getConditionIconString() {
        return conditionIconString;
    }

    public void setConditionIconString(String conditionIconString) {
        this.conditionIconString = conditionIconString;
    }

    public Weather() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
