package com.ogungor.newbook;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    SQLiteDatabase database;
    LocationListener locationListener;
    LocationManager locationManager;
    Intent intent;
    String adress = "";
    String lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }
        };

        database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
        intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")) {
            if (Build.VERSION.SDK_INT >= 21) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15));
                    System.out.println("Son lokasyon: " + userLastLocation);
                }
            }
            mMap.setOnMapLongClickListener(this);
        } else {
            intent = getIntent();
            int idIx = intent.getIntExtra("locationId", 1);

            Cursor cursor = database.rawQuery("select * from location where id=?", new String[]{String.valueOf(idIx)});
            int locationNameIx = cursor.getColumnIndex("locationname");
            int locationLatIx = cursor.getColumnIndex("locatiolat");
            int locationLngIx = cursor.getColumnIndex("locationlng");

            System.out.println("yaz: " + locationNameIx + "+" + locationLatIx + "+" + locationLngIx);


            while (cursor.moveToNext()) {
                String locationAdress = cursor.getString(1);
                String selectAdressLat = cursor.getString(2);
                String selectAdresslng = cursor.getString(3);
                double lat = Double.parseDouble(selectAdressLat);
                double lng = Double.parseDouble(selectAdresslng);
                System.out.println("konum: " + lat + " " + lng);
                LatLng selectAdress = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().title(locationAdress).position(selectAdress));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectAdress, 15));


            }
        }

    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addressList;
        adress = "";
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList.get(0).getThoroughfare() != null) {
                adress += addressList.get(0).getThoroughfare();
                if (addressList.get(0).getSubThoroughfare() != null) {

                    adress += addressList.get(0).getSubThoroughfare();
                }
                lat = String.valueOf(latLng.latitude);
                lng = String.valueOf(latLng.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title(adress));

                database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
                database.execSQL("create table if not exists location (id INTEGER PRIMARY KEY,locationname VARCHAR,locationlat VARCHAR, locationlng VARCHAR)");
                String sqlSting = "INSERT INTO location(locationname,locationlat,locationlng) values(?,?,?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlSting);
                sqLiteStatement.bindString(1, adress);
                sqLiteStatement.bindString(2, lat);
                sqLiteStatement.bindString(3, lng);
                sqLiteStatement.execute();

                Toast.makeText(MapsActivity.this, "New Added Location", Toast.LENGTH_LONG).show();
            }
            else{
                mMap.addMarker(new MarkerOptions().position(latLng).title("Not Location"));
                Toast.makeText(MapsActivity.this, "Not Location", Toast.LENGTH_LONG).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {
        intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }
}