package com.example.ios_razrab.googlemapexperiments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback
{
    private GoogleMap mMap;

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

        tv_c_lat .setText("0");
        tv_c_long .setText("0");
        tv_c_status.setText("Определяем приблизительное местоположение..");
        tv_f_lat .setText("0");
        tv_f_long .setText("0");
        tv_f_status .setText("Определяем точное местоположение..");


        if (checkLocationSwithOn()) {

            if (Build.VERSION.SDK_INT >= 23) {

                if (!checkDynamicPerms()) {
                    callPermissions();
                } else {
                    permsGranted();
                }
            } else {
                permsGranted();
            }
        }
    }



    /**
     * Когда есть разрешения
     * */
    void permsGranted(){
        Log .i("Разрешено!!","");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getCoarseLocation();
    }




  /**
   * Включена ли физически локация на устройстве
   * */
    boolean checkLocationSwithOn(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            // Call your Alert message

            Toast.makeText(this, "Для определения точного местоположения необходимо включить локацию на устройстве!", Toast.LENGTH_SHORT).show();

            return false;
        } else {

            return true;
        }
    }




    /**
     Коллбек вызывается когда карта готова к использованию
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap .setMyLocationEnabled(true);

        // Check if we were successful in obtaining the map.
        if (mMap != null) {

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                /**
                 * Колбек вызывается и при включении гео и при смене местоположения
                 * */
                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub

                    LatLng myFineCurrentLocation = new LatLng(arg0.getLatitude() ,arg0 .getLongitude());

                    tv_f_status .setText("Точная локация определена!");
                    tv_f_lat .setText(String .valueOf(myFineCurrentLocation .latitude));
                    tv_f_long .setText(String .valueOf(myFineCurrentLocation .longitude));

                    mMap.addMarker(new MarkerOptions().position(myFineCurrentLocation).title("It's Me!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myFineCurrentLocation));
                    mMap .animateCamera(CameraUpdateFactory.zoomTo(13), 700, null);
                }
            });
        }
    }






    public void getCoarseLocation(){

        String locationProvider = LocationManager.NETWORK_PROVIDER;
// Or use LocationManager.GPS_PROVIDER

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // получение приблизительной
        // ССЫЛКА НА ОФИЦ ДОКУМЕНТАЦИЮ, ГДЕ ГОВОРЯТ ДЕЛАТЬ ИМЕННО ТАК!:
        // https://developer.android.com/guide/topics/location/strategies#BestPerformance
        Location location = locationManager.getLastKnownLocation(locationProvider);

        try {
            tv_c_lat .setText("Долгота: "+ String .valueOf(location.getLatitude()));
            tv_c_long .setText("Широта: "+ String .valueOf(location.getLongitude()));

            tv_c_status .setText("Приблизительное местоположение обнаружено");

        } catch (NullPointerException e) {

            Log .e("Ошибка приблизительн: ",e .toString());

            //  lat = -1.0;
            // lon = -1.0;
           tv_c_status .setText("Приблизительное местоположение обнаружить не удалось. " +
                   "Возможно, Вам следует включить геолокацию на устройстве");
            Log .d("Проблемы с гео: %s", e.toString());
            // Вероятно у пользователя отключена геолокация

        }
    }




    // region permissions -------------------------------------------

    /**
     * Если версия 6 или более,
     * запрашиваем динамические разрешения
     * */
    private boolean checkDynamicPerms(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            return true;
        }
    }



    void callPermissions(){
        // если нет разрешения еще то запрашиваем
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_FINE_LOCATION);
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
                    permsGranted();

                } else {

                    Toast.makeText(this, "Пользователь не дал разрешения",
                            Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // endregion permissions ------------------------------------------

}
