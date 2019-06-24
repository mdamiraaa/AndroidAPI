package com.example.apilab;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apilab.Models.WeatherResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {

    private List<String> listCities;
    private MaterialSearchBar materialSearchBar;
    private EditText editText;
    private Button btn;

    ImageView img_weather;
    TextView txt_city_name,txt_humidity,txt_sunrise,txt_sunset,txt_pressure,txt_temp,txt_desc,txt_date_time,txt_geo,txt_wind;
    LinearLayout weather_panel;

    CompositeDisposable compositeDisposable;
    ApiService mService;
    static CityFragment instance;


    public static CityFragment getInstance() {
        if(instance == null)
            instance = new CityFragment();
        return instance;
    }


    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(ApiService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView =  inflater.inflate(R.layout.fragment_city, container, false);

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

        materialSearchBar =(MaterialSearchBar)itemView.findViewById(R.id.searchBar);
        editText = (EditText)itemView.findViewById(R.id.editText);
        btn = (Button)itemView.findViewById(R.id.btn);
        materialSearchBar.setEnabled(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeatherInformation(editText.getText().toString());
                editText.setText("");
            }
        });



        getWeatherInformation(editText.getText().toString());
        new LoadCities().execute();
        return itemView;
    }



    private class LoadCities extends SimpleAsyncTask<List<String>> {
        @Override
        protected List<String> doInBackgroundSimple() {
            listCities = new ArrayList<>();
            try{
                StringBuilder builder = new StringBuilder();
                InputStream is = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);

                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader in = new BufferedReader(reader);


                String readed;
                while ((readed = in.readLine())!= null)
                    builder.append(readed);
                listCities = new Gson().fromJson(builder.toString(),new TypeToken<List<String>>(){}.getType());

            }catch (IOException e ){
                e.printStackTrace();
            }
            return listCities;
        }


        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);

            materialSearchBar.setEnabled(true);
            materialSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<>();
                    for (String search:listCity){
                        if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    materialSearchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());

                    materialSearchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

            materialSearchBar.setLastSuggestions(listCity);

        }

    }

    private void getWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
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
                        txt_wind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).toString());
                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txt_geo.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());
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
