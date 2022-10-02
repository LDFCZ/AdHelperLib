package com.example.adhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdInitializer {

    private String token;

    private String adId;

    private String geolocation;

    private LocationRequest locationRequest;

    private AppCompatActivity context;

    private WebView webView;

    private boolean isAdIdReady = false;
    private boolean isGeolocationReady = false;

    @SuppressLint("SetJavaScriptEnabled")
    public AdInitializer(AppCompatActivity context, String token, @IdRes int webViewId) {
        this.context = context;
        this.token = token;

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_LOW_POWER);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        this.getAdIdFromDevice();
        //this.getGeolocationFromDevice();

        webView = context.findViewById(webViewId);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("https://interactive-ads-api.herokuapp.com"); // add token geo and adid
        System.out.println("!!!!!!!!!!!" + adId + "!!!!!!" + geolocation);
    }

    private void getAdIdFromDevice() {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            //Background work here
            AdvertisingIdClient.Info adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(AdInitializer.this.context);

            } catch (IOException exception) {
                // Unrecoverable error connecting to Google Play services (e.g.,
                // the old version of the service doesn't support getting AdvertisingId).

            } catch (GooglePlayServicesRepairableException exception) {
                // Encountered a recoverable error connecting to Google Play services.

            } catch (GooglePlayServicesNotAvailableException exception) {
                // Google Play services is not available entirely.
            }
            assert adInfo != null;
            final String id = adInfo.getId();
            final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
            AdInitializer.this.setAdId(id);
            isAdIdReady = true;
            //webViewLoader.notify();
        });
    }

    public void getGeolocationFromDevice() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            //Background work here
            //turnOnGPS();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(AdInitializer.this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.getFusedLocationProviderClient(AdInitializer.this.context).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            LocationServices.getFusedLocationProviderClient(AdInitializer.this.context)
                                    .removeLocationUpdates(this);

                            if (locationResult.getLocations().size() > 0) {

                                int index = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                double longitude = locationResult.getLocations().get(index).getLongitude();
                                Geocoder geocoder = new Geocoder(AdInitializer.this.context, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                String cityName = addresses.get(0).getLocality();
                                AdInitializer.this.setGeolocation(cityName);
                                isGeolocationReady = true;
                                System.out.println("!!!!!!!!!!!" + adId + "!!!!!!" + geolocation);
                                //webViewLoader.notify();
                            }
                        }
                    }, Looper.getMainLooper());

                }
            }
            isGeolocationReady = true;
            //webViewLoader.notify();
        });
    }

    public void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(AdInitializer.this.context)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    //Toast.makeText(AdInitializer.this.context, "GPS is already turned on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(AdInitializer.this.context, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }
    private void setAdId(String adId) {
        this.adId = adId;
    }

    public String getToken() {
        return token;
    }

    public String getAdId() {
        return adId;
    }

    public String getGeolocation() {
        return geolocation;
    }
}