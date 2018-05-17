package com.example.ios_razrab.googlemapexperiments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    LocationCallback mLocationCallback;

    private GoogleMap mMap;

    Location location;

    static final int MY_PERMISSIONS_FINE_LOCATION =1111;

    TextView tv_c_lat, tv_c_long, tv_c_status,  tv_f_lat, tv_f_long, tv_f_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_c_lat=(TextView)findViewById(R.id.tv_lat_coarse);
        tv_c_long=(TextView)findViewById(R.id.tv_long_coarse);
        tv_c_status=(TextView)findViewById(R.id.tv_status_coarse);
        tv_f_status=(TextView)findViewById(R.id.tv_status_fine);
        tv_f_lat=(TextView)findViewById(R.id.tv_lat_fine);
        tv_f_long=(TextView)findViewById(R.id.tv_long_fine);

        tv_c_lat .setText("");
        tv_c_long .setText("");
        tv_c_status.setText("Определяем приблизительное местоположение..");
        tv_f_lat .setText("");
        tv_f_long .setText("");
        tv_f_status .setText("Определяем точное местоположение..");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        createLocationCallback();


    }



    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                location = locationResult.getLastLocation();

                Log.d("testingvalue",String.valueOf(location==null));
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                Log .d("location","");

//                notifyDataSetChanged();
            }
        };
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        checkLocation();
        mMap .setMyLocationEnabled(true);

// Here, thisActivity is the current activity

        if (Build.VERSION.SDK_INT >= 23) {

            checkDynamicPerms();

        } else {
            checkDynamicPerms();
        }
    }





    /**
     * Если версия 6 или более,
     * запрашиваем динамические разрешения
     * */
    private void checkDynamicPerms(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            getCoarseLocation();
            // Permission has already been granted
        }

    }





    void checkLocation() {
   // сделал обновление точной пока каждые 2 секунды, но можно любое конечно
      Disposable disposable = Observable .just( "") .delay(2000, TimeUnit .MILLISECONDS) .repeat().subscribeOn(Schedulers .newThread())
                .observeOn(AndroidSchedulers .mainThread())
                .subscribe(new Consumer<String>(){
                    @Override
              public void accept(String s ) throws Exception {

                        try{
                             Location location = mMap .getMyLocation();
                             zoomMapTo(new LatLng( location .getLatitude(), location .getLongitude()), 12 );
                            tv_f_status .setText("Точная локация определена!");
                        }catch (Exception e){
                            tv_f_status .setText("Точная локация пока не определена");
                        }
             }
                     });
                 }






    /**
     * Наводит камеру на местоположение
     * @param latLng геоточка на которую нужно навести зум
     * */
    void zoomMapTo(LatLng latLng, float zoom){

        mMap.moveCamera(CameraUpdateFactory.newLatLng( latLng ));
        mMap .animateCamera(CameraUpdateFactory.zoomTo(zoom), 700, null);

        tv_f_lat .setText(String .valueOf(latLng .latitude));
        tv_f_long .setText(String .valueOf(latLng .longitude));


    }





    public void getCoarseLocation(){
        // Get the location manager
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        // получение приблизительной
        Location location = locationManager.getLastKnownLocation(bestProvider);


        try {
            tv_c_lat .setText("Долгота: "+ String .valueOf(location.getLatitude()));
            tv_c_long .setText("Широта: "+ String .valueOf(location.getLongitude()));

            Log.d( "Прибл шир и долг:"
                    , String .valueOf( location.getLatitude()) + "" +
                            " " + String .valueOf(location .getLongitude()));

            tv_c_status .setText("Приблизительное местоположение обнаружено." +
                    " Перемещаем фокус на него:");

            zoomMapTo(new LatLng(location .getLatitude(), location .getLongitude()), 12f);

            // lat = location.getLatitude();
            // lon = location.getLongitude();
        } catch (NullPointerException e) {
            //  lat = -1.0;
            // lon = -1.0;
           tv_c_status .setText("Приблизительное местоположение обнаружить не удалось. " +
                   "Возможно, вам следует включить геолокацию на устройстве");
            Log .d("Проблемы с гео: %s", e.toString());
            // Вероятно у пользователя отключена геолокация

        }

    }






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getCoarseLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
