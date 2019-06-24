package com.example.apilab;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.apilab.Models.WeatherResult;
import com.squareup.picasso.Picasso;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {


    ImageView img_weather;
    TextView txt_city_name,txt_humidity,txt_sunrise,txt_sunset,txt_pressure,txt_temp,txt_desc,txt_date_time,txt_geo,txt_wind;
    LinearLayout weather_panel;

    CompositeDisposable compositeDisposable;
    ApiService mService;
    static TodayWeatherFragment instance;


    public static TodayWeatherFragment getInstance() {
        if(instance == null)
            instance = new TodayWeatherFragment();
        return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(ApiService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);
        img_weather = (ImageView)itemView.findViewById(R.id.img_weather);
        txt_city_name =(TextView)itemView.findViewById(R.id.txt_city_name);
        txt_pressure =(TextView)itemView.findViewById(R.id.txt_pressure);
        txt_humidity =(TextView)itemView.findViewById(R.id.txt_humidity);
        txt_date_time =(TextView)itemView.findViewById(R.id.txt_date_time);
        txt_desc =(TextView)itemView.findViewById(R.id.txt_description);
        txt_geo =(TextView)itemView.findViewById(R.id.txt_geo);
        txt_sunrise =(TextView)itemView.findViewById(R.id.txt_sunrise);
        txt_sunset =(TextView)itemView.findViewById(R.id.txt_sunset);
        txt_temp =(TextView)itemView.findViewById(R.id.txt_temp);
        txt_wind =(TextView)itemView.findViewById(R.id.txt_wind);


        weather_panel =(LinearLayout)itemView.findViewById(R.id.weather_panel);


        getWeatherInformation();

        return itemView;
    }

    private void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metrio")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                        .append(".png").toString()).into(img_weather);

                        txt_city_name.setText(weatherResult.getName());
                        txt_desc.setText(new StringBuilder("Weather in ").append(weatherResult.getName()).toString());
                        txt_temp.setText(new StringBuilder(String.valueOf(Math.round(weatherResult.getMain().getTemp()-273.15))).append(" °C").toString());
                        txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txt_wind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().toString())).toString());
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txt_geo.setText(new StringBuilder("[").append(weatherResult.getCoord().toString()).append("]").toString());
                       // txt_wind.setText(new StringBuilder("Speed: ").append(weatherResult.getWind().toString()).append("last").toString());

                    }
                }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(getActivity(),""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }


    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

}
