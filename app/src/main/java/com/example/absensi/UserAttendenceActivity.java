package com.example.absensi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.absensi.helper.GPSTracker;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserAttendenceActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    public final static String TAG = "UserAttendenceActivity";

    //init google map
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker mCurrLocationMarker;
    private Location mLastLocation;
    private GPSTracker gpsTracker;

    //init component
    private AppCompatTextView tv_lokasi, tv_masuk, tv_keluar;
    private SwipeButton swipeButton;
    private AppCompatImageView btn_back;

    //init dialog & take camera setting
    Dialog dialog;
    private AppCompatImageView take_photo,img_selfie;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 98;
    public static final int CAMERA_REQUEST = 99;

    private static boolean masuk;
    private static boolean keluar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_attendence);
        tv_lokasi = findViewById(R.id.tv_lokasi);
        swipeButton = findViewById(R.id.swipe_btn);
        tv_masuk = findViewById(R.id.tv_masuk);
        tv_keluar = findViewById(R.id.tv_keluar);
        btn_back = findViewById(R.id.btn_back);
        checkLocationPermission();
        masuk = false;
        keluar = false;

//        dialog
        dialog = new Dialog(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        swipeButton.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
            //Toast.makeText(getApplicationContext(), "Activated", Toast.LENGTH_SHORT).show();
                showDialog();
                if (!masuk) {
                    tv_masuk.setText(getDateTime());
                    tv_masuk.setTextColor(Color.rgb(0, 255, 0));
                    masuk = true;
                    swipeButton.setInnerText("Geser Untuk Keluar");
                } else {
                    tv_keluar.setText(getDateTime());
                    tv_keluar.setTextColor(Color.rgb(0, 255, 0));
                    keluar = true;
                }
                if (masuk && keluar) {
                    Toast.makeText(UserAttendenceActivity.this, "Anda telah absen", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String id = getDeviceId(this);

        btn_back.setOnClickListener(this);

        gpsTracker = new GPSTracker(this);


    }

    public static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    public static String getDateTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String dataTime = String.valueOf(hour) + " : " + String.valueOf(minute);
        return dataTime;
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
        LatLng latLng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latLng.latitude, latLng.longitude)).zoom(16).build();

////        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLng.latitude, latLng.longitude)));
////        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Memulai Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.d(TAG, "message: " + e.getMessage());
            e.printStackTrace();
        }

        if (geocoder != null) {
            tv_lokasi.setText(addresses.get(0).getAddressLine(0));
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(addresses.get(0).getAddressLine(0)));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latLng.latitude, latLng.longitude)).zoom(16).build();

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLng.latitude, latLng.longitude)));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //menghentikan pembaruan lokasi
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Izin diberikan.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Izin ditolak.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //take selfie setting
    public void showDialog(){
        dialog.setContentView(R.layout.take_photo);
        take_photo = dialog.findViewById(R.id.take_photo);
        img_selfie = dialog.findViewById(R.id.imgSelfie);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkCameraPermission()){
                    Intent intentToCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intentToCamera, CAMERA_REQUEST);
                }
            }
        });
        dialog.show();
    }

    //request to access camera
    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            return false;
        } else {
            return true;
        }
    }

    //activity result on camera
    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
             if (requestCode == CAMERA_REQUEST) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                Uri uri = getImageUri(UserAttendenceActivity.this, imageBitmap);
//                String imagepath = getRealPathFromURI(uri, this);
//                File imageFile = new File(imagepath);
                 img_selfie.setImageBitmap(imageBitmap);
//                Picasso.get().load(uri).centerCrop().fit().into(img_selfie);

            }
        }
    }

    //convert image from storage to uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "selfie", null);
        return Uri.parse(path);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }
}