package com.example.apilab;

import com.example.apilab.Models.WeatherForecastResult;
import com.example.apilab.Models.WeatherResult;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    //Routing

    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                                 @Query("lon") String lng,
                                                 @Query("appid")String appid,
                                                 @Query("units") String unit);




    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByLatLng(@Query("lat") String lat,
                                                                 @Query("lon") String lng,
                                                                 @Query("appid")String appid,
                                                                 @Query("units") String unit);



    @GET("weather")
    Observable<WeatherResult> getWeatherByCityName(@Query("q") String cityName,
                                                 @Query("appid")String appid,
                                                 @Query("units") String unit);



}
