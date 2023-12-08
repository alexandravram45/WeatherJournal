package com.example.weatherjournal2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private static MutableLiveData<String> cityNameLiveData = new MutableLiveData<>();

    public static LiveData<String> getCityNameLiveData() {
        return cityNameLiveData;
    }

    public static void updateCityName(String cityName) {
        cityNameLiveData.setValue(cityName);
    }

}
