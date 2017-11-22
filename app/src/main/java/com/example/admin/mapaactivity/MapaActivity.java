package com.example.admin.mapaactivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    //mapa de google
    private GoogleMap mMap;
    //CameraPosition para manejar la camara
    private CameraPosition mCameraPosition;
    //variable para pedir el permiso
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 14;
    //si el permiso fue autorizado
    private boolean mLocationPermissionGranted;
    //ultima ubicacion conocida
    private Location mLastKnownLocation, mTempLocation;
    //Default zoom
    private static final int DEFAULT_ZOOM = 15;
    //acceso al GPS
    private GoogleApiClient mGoogleApiClient;
    //posicion por defecto
    private LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    //identificadores para guardar la localizacion
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private Marcador marcadorUNIVERSIDAD;
    private Marker marcadorMiUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //obtener la ultima posicion conocida
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mDefaultLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        marcadorUNIVERSIDAD = (Marcador) this.getIntent().getExtras().getSerializable("MARCADOR");
        setContentView(R.layout.activity_mapa);
        //poner el menu



        //encargado del GPS
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        //encargado del MAPA
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //llamar al permiso de ubicacion
        permisoUbicacion();
    }

    private void permisoUbicacion() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //control zoom
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //agregando marcadores
        if(marcadorUNIVERSIDAD!=null){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(marcadorUNIVERSIDAD.latitud, marcadorUNIVERSIDAD.longitud))
                    .title(marcadorUNIVERSIDAD.titulo)
                    .icon((BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    private void getDeviceLocation() {
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            if(mLastKnownLocation==null){
                Toast.makeText(this, "GPS desabilitado, porfavor activalo", Toast.LENGTH_SHORT).show();
            }
        }
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            LatLng islamabad = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(islamabad, DEFAULT_ZOOM));
            //agregando el marcador de la posicion actual

            marcadorMiUbicacion = mMap.addMarker(new MarkerOptions().position(islamabad).title("Ubicación Actual")
                    .icon((BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))) );

        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        }
    }

    //resultado del permiso
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();//actualizo la ubicacion
                }
            }
        }
    }

    //TODO implementacion de acceso al GPS
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getDeviceLocation();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    //para el menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LatLng islamabad;
        switch (item.getItemId()) {
            case R.id.item_yo:
                islamabad = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(islamabad, DEFAULT_ZOOM));
                break;
            case R.id.item_universidad:
                islamabad = new LatLng(marcadorUNIVERSIDAD.latitud,marcadorUNIVERSIDAD.longitud);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(islamabad, DEFAULT_ZOOM));
                break;
            case R.id.item_actualizar:
                marcadorMiUbicacion.remove();
                getDeviceLocation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
