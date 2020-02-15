package com.midhun.myweather;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.N)
public class WeatherActivity extends AppCompatActivity {
String TAG="WeatherActivity";

    TextView tv_title;
    TextView tv_temp;
    TextView tv_city;
    TextView tv_description;
    ImageView iv_img;
    RecyclerView rv_recyclerview;

    SimpleDateFormat _24HourSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat _12HourSDF = new SimpleDateFormat("MMM dd hh:mm a");
    ImageView iv_gif;
    WwatherAdapter weatheradapter;
    ArrayList<HashMap<String, String>> arrlist = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    LinearLayout linearLayout;
    GPSTrack gpsTrack;
    String latitide = "", longtitude = "";
    private static final int PERMISSION_REQUEST = 2;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };

    Dialog new_post_alertDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        checkpermisison();
        iv_gif = (ImageView) findViewById(R.id.iv_gif);
     //   linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        rv_recyclerview = (RecyclerView) findViewById(R.id.rv_recyclerview);
        weatheradapter = new WwatherAdapter(arrlist);
        linearLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_recyclerview.setLayoutManager(linearLayoutManager);
        rv_recyclerview.setAdapter(weatheradapter);


        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("Forecast weather");
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        iv_img = (ImageView) findViewById(R.id.iv_img);
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_description = (TextView) findViewById(R.id.tv_description);

        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_gif);
        Glide.with(this).load(R.raw.cloudnewpass).into(imageViewTarget);


        final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsTrack = new GPSTrack(getApplicationContext());
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            GetForecast_defaultlocation("bangalore");
            fetchWeatherDetails("bangalore");
        } else {
            latitide = "" + gpsTrack.getLatitude();
            longtitude = "" + gpsTrack.getLongitude();

            Log.d(TAG,"latitide"+latitide+"longtitude"+longtitude);


            if(latitide!=null&&!latitide.equals("0.0")) {
                GetForecast();
                fetchWeatherDetailsLive();
            }else {
                GetForecast_defaultlocation("bangalore");
                fetchWeatherDetails("bangalore");
            }
        }


        tv_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert();
            }
        });
        tv_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert();
            }
        });


    }

    void showAlert() {


        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(true);
        LayoutInflater inflater = getLayoutInflater();

        View dialog = inflater.inflate(R.layout.location_alert, null);

        final EditText Et_forgotemail = (EditText) dialog.findViewById(R.id.Et_cityname);
        final TextView btn_submit = (TextView) dialog.findViewById(R.id.btn_submit);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetForecast_defaultlocation(Et_forgotemail.getText().toString());
                fetchWeatherDetails(Et_forgotemail.getText().toString());
                new_post_alertDialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new_post_alertDialog.dismiss();
            }
        });


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        alertDialogBuilder.setView(dialog);
        new_post_alertDialog = alertDialogBuilder.create();
        //  new_post_alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        new_post_alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //new_post_alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id
        new_post_alertDialog.show();


    }

    private void checkpermisison() {

        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(WeatherActivity.this, PERMISSIONS, PERMISSION_REQUEST);
            // ActivityCompat.requestPermissions(LoginPage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {

        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void GetForecast() {
        Apiinterface apiinterface = Apiclient.getRetrofitClient().create(Apiinterface.class);

        Call<ResponseBody> call = apiinterface.getforecastcurrentlocation(latitide,longtitude, "ff4b42557656b995507175c3d4ca7256");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "LVresponse" + response.isSuccessful());
                if(response.isSuccessful()) {

                    try {
                          JSONObject jsonOb = new JSONObject(response.body().string());
                        Log.d("LVresponse", jsonOb.toString());
                        JSONArray jsonArray = jsonOb.getJSONArray("list");
                        arrlist.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {


                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                            String dt_txt = jsonObject.getString("dt_txt");

                            JSONObject mainobj = jsonObject.getJSONObject("main");

                            String temp = mainobj.getString("temp");

                            JSONArray weatherarr = jsonObject.getJSONArray("weather");

                            JSONObject jsonObject1 = weatherarr.getJSONObject(0);


                            String text = "" + jsonObject1.get("main");
                            String icon = "" + jsonObject1.get("icon");
                            String description = "" + jsonObject1.get("description");

//
//                            Log.d("texttemp", "" + text);
//                            Log.d("icontemp", "" + icon);
//                            Log.d("descriptiontemp", "" + description);
//                            Log.d("temptemp", "" + temp);


                            HashMap<String, String> map = new HashMap<>();
                            map.put("text", "" + text);
                            map.put("icon", "" + icon);
                            map.put("description", "" + description);
                            map.put("temp", "" + temp);
                            map.put("dt_txt", "" + dt_txt);
                            arrlist.add(map);
                            weatheradapter.notifyDataSetChanged();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void GetForecast_defaultlocation(String cityname) {
        Apiinterface apiinterface = Apiclient.getRetrofitClient().create(Apiinterface.class);

        Call<ResponseBody> call = apiinterface.getforecastCity(cityname, "ff4b42557656b995507175c3d4ca7256");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Log.d(TAG, "responseDefault" + response.isSuccessful());
                if(response.isSuccessful()) {

                    try {
                        JSONObject jsonOb = new JSONObject(response.body().string());
                        Log.d("Defaultresponse", jsonOb.toString());
                        JSONArray jsonArray = jsonOb.getJSONArray("list");
                        arrlist.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {


                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                            String dt_txt = jsonObject.getString("dt_txt");

                            JSONObject mainobj = jsonObject.getJSONObject("main");

                            String temp = mainobj.getString("temp");

                            JSONArray weatherarr = jsonObject.getJSONArray("weather");

                            JSONObject jsonObject1 = weatherarr.getJSONObject(0);


                            String text = "" + jsonObject1.get("main");
                            String icon = "" + jsonObject1.get("icon");
                            String description = "" + jsonObject1.get("description");

//
//                            Log.d("texttemp", "" + text);
//                            Log.d("icontemp", "" + icon);
//                            Log.d("descriptiontemp", "" + description);
//                            Log.d("temptemp", "" + temp);


                            HashMap<String, String> map = new HashMap<>();
                            map.put("text", "" + text);
                            map.put("icon", "" + icon);
                            map.put("description", "" + description);
                            map.put("temp", "" + temp);
                            map.put("dt_txt", "" + dt_txt);
                            arrlist.add(map);
                            weatheradapter.notifyDataSetChanged();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Invalid city name",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void fetchWeatherDetails(String cityname) {
        Apiinterface apiinterface = Apiclient.getRetrofitClient().create(Apiinterface.class);
        Call<ResponseBody> call = apiinterface.getWeatherByCity(cityname, "8512800deb3bdf0ec9c27a904c9a571f");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of WResponse POJO class
                 */
                Log.d(TAG, "weatherresponse" + response.isSuccessful());
                if (response.isSuccessful()) {

                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        Log.d(TAG, "LVresponseweather" + jsonResponse.toString());
                        JSONObject todayObject = jsonResponse.getJSONObject("main");
                        JSONArray weather = jsonResponse.getJSONArray("weather");

                        JSONObject jsonObject1 = weather.getJSONObject(0);

                        String description = jsonObject1.getString("description");
                        String icon = jsonObject1.getString("icon");
                        String name = jsonResponse.getString("name");

                        tv_city.setText(name);
                        tv_description.setText(description);


                        String iconbaseURL = "http://openweathermap.org/img/w/";
                        icon = iconbaseURL + icon + ".png";

                        Glide.with(getApplicationContext()).load(icon).into(iv_img);


                        String temp = todayObject.getString("temp");

                        float maxTemp = Float.parseFloat(temp);

                        float maxCel = (float) (maxTemp - 273.16);


                        String maxRound = String.format("%.2f", maxCel);

                        //  tv_temp.setText(maxRound + (char) 0x00B0);

                        String str = roundToOneDigit(maxCel);

                        String kept = str.substring(0, str.indexOf("."));

                        // tv_temp.setText(kept + (char) 0x00B0 + " \u2103");
                        tv_temp.setText(kept + " \u2103");
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, "Error_response" + t.toString());
            }
        });

    }

    private void fetchWeatherDetailsLive() {
        Apiinterface apiinterface = Apiclient.getRetrofitClient().create(Apiinterface.class);
        Call<ResponseBody> call = apiinterface.getWeatherByLive(latitide,longtitude, "8512800deb3bdf0ec9c27a904c9a571f");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of WResponse POJO class
                 */
                Log.d(TAG, "LVweatherresponse" + response.isSuccessful());
                if (response.isSuccessful()) {

                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        Log.d(TAG, "LVresponseweather" + jsonResponse.toString());
                        JSONObject todayObject = jsonResponse.getJSONObject("main");
                        JSONArray weather = jsonResponse.getJSONArray("weather");

                        JSONObject jsonObject1 = weather.getJSONObject(0);

                        String description = jsonObject1.getString("description");
                        String icon = jsonObject1.getString("icon");
                        String name = jsonResponse.getString("name");

                        tv_city.setText(name);
                        tv_description.setText(description);


                        String iconbaseURL = "http://openweathermap.org/img/w/";
                        icon = iconbaseURL + icon + ".png";

                        Glide.with(getApplicationContext()).load(icon).into(iv_img);


                        String temp = todayObject.getString("temp");

                        float maxTemp = Float.parseFloat(temp);

                        float maxCel = (float) (maxTemp - 273.16);


                        String maxRound = String.format("%.2f", maxCel);

                        //  tv_temp.setText(maxRound + (char) 0x00B0);

                        String str = roundToOneDigit(maxCel);

                        String kept = str.substring(0, str.indexOf("."));

                        // tv_temp.setText(kept + (char) 0x00B0 + " \u2103");
                        tv_temp.setText(kept + " \u2103");
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, "Error_response" + t.toString());
            }
        });

    }

    public static String roundToOneDigit(float paramFloat) {
        return String.format("%.1f%n", paramFloat);
    }

    public class WwatherAdapter extends RecyclerView.Adapter<WwatherAdapter.ViewHolder> {

        ArrayList<HashMap<String, String>> arrayList;

        public WwatherAdapter(ArrayList<HashMap<String, String>> arrayList) {

            this.arrayList = arrayList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_text, tv_date, tv_temp;
            ImageView iv_weather, wfi_bt_more;
            CardView cv_cvrestrarnt;

            public ViewHolder(View itemView) {
                super(itemView);

                tv_text = (TextView) itemView.findViewById(R.id.tv_text);

                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                tv_temp = (TextView) itemView.findViewById(R.id.tv_temp);
                iv_weather = (ImageView) itemView.findViewById(R.id.iv_weather);

            }
        }

        @Override
        public int getItemCount() {
            return arrayList.size();

        }

        @Override
        public void onBindViewHolder(final WwatherAdapter.ViewHolder holder, int position) {
            char tmp = 0x00B0;

           /* map.put("text", "" + text);
            map.put("icon", "" + icon);
            map.put("description", "" + description);*/

            holder.tv_text.setText("" + arrayList.get(position).get("text"));
            // holder.tv_temp.setText("" + arrayList.get(position).get("temp") + tmp);

            String temp = "" + arrayList.get(position).get("temp");


            float maxTemp = Float.parseFloat(temp);

            float maxCel = (float) (maxTemp - 273.16);

            String str = roundToOneDigit(maxCel);

            String kept = str.substring(0, str.indexOf("."));

            // tv_temp.setText(kept + (char) 0x00B0 + " \u2103");
            holder.tv_temp.setText(kept + " \u2103");

            //  String date = arrayList.get(position).get("dt_txt").replace(" ", ",");
            String date = arrayList.get(position).get("dt_txt");

          //  Log.d("newdate", "" + date);


            String dateone[] = date.split(",");


            holder.tv_date.setText(getTime(date));

            String icon = "" + arrayList.get(position).get("icon");

            String iconbaseURL = "http://openweathermap.org/img/w/";
            icon = iconbaseURL + icon + ".png";

            Glide.with(getApplicationContext()).load(icon).into(holder.iv_weather);

        }

        @Override
        public WwatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WwatherAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_report, parent, false));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    String getTime(String _24HourTime) {
        Date _24HourDt = null;
        try {
            // String _24HourTime = "22:15";

            _24HourDt = _24HourSDF.parse(_24HourTime);
            //System.out.println(_24HourDt);
            //System.out.println(_12HourSDF.format(_24HourDt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" + _12HourSDF.format(_24HourDt);
    }
    }
