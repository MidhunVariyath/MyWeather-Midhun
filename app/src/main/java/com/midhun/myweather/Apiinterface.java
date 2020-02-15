package com.midhun.myweather;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Apiinterface {

    /*
    Get request to fetch city weather.Takes in two parameter-city name and API key.
    */
    @GET("/data/2.5/weather")
    Call<ResponseBody> getWeatherByCity(@Query("q") String city, @Query("appid") String apiKey);

    @GET("/data/2.5/weather")
    Call<ResponseBody> getWeatherByLive(@Query("lat") String lat, @Query("lon") String lon,@Query("appid") String apiKey);

    @GET("/data/2.5/forecast")
    Call<ResponseBody> getforecastCity(@Query("q") String city, @Query("appid") String apiKey);

    @GET("/data/2.5/forecast")
    Call<ResponseBody> getforecastcurrentlocation(@Query("lat") String lat,@Query("lon") String longitude, @Query("appid") String apiKey);

}
