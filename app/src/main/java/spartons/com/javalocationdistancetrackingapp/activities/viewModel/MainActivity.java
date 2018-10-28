package spartons.com.javalocationdistancetrackingapp.activities.viewModel;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import spartons.com.javalocationdistancetrackingapp.R;
import spartons.com.javalocationdistancetrackingapp.helper.GoogleMapHelper;
import spartons.com.javalocationdistancetrackingapp.helper.MarkerAnimationHelper;
import spartons.com.javalocationdistancetrackingapp.helper.UiHelper;
import spartons.com.javalocationdistancetrackingapp.util.AppRxSchedulers;
import spartons.com.javalocationdistancetrackingapp.util.LatLngInterpolator;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2161;

    private GoogleMapHelper googleMapHelper;
    private AppRxSchedulers appRxSchedulers = new AppRxSchedulers();
    private UiHelper uiHelper;
    private MainActivityViewModel viewModel;
    private GoogleMap googleMap;

    private TextView distanceCoveredTextView;

    private boolean firstTimeFlag = true;

    private Marker marker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FusedLocationProviderClient providerClient = LocationServices.getFusedLocationProviderClient(this);
        googleMapHelper = new GoogleMapHelper(getResources());
        uiHelper = new UiHelper(this);
        MainActivityViewModelFactory factory = new MainActivityViewModelFactory(appRxSchedulers, uiHelper.getLocationRequest(), providerClient, googleMapHelper);
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
        distanceCoveredTextView = findViewById(R.id.distanceCoveredTextView);
        if (!uiHelper.isPlayServicesAvailable()) {
            Toast.makeText(this, "Play Services did not installed!", Toast.LENGTH_SHORT).show();
            finish();
        } else checkLocationPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            googleMapHelper.defaultMapSettings(googleMap);
            this.googleMap = googleMap;
            startListenNewLocation();
        });
        findViewById(R.id.currentLocationImageButton).setOnClickListener(view -> {
            if (marker == null || googleMap == null) return;
            googleMapHelper.animateCamera(marker.getPosition(), googleMap);
        });
    }

    private void checkLocationPermission() {
        if (!uiHelper.isHaveLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        if (uiHelper.isLocationProviderEnabled())
            uiHelper.showPositiveDialogWithListener(this, getResources().getString(R.string.need_location), getResources().getString(R.string.location_content), () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), "Turn On", false);
        viewModel.requestLocationUpdates();
    }

    private void startListenNewLocation() {
        viewModel.currentLocation()
                .observe(this, location -> {
                    assert location != null;
                    Log.e("Location Received -> ", location.getLatitude() + " , " + location.getLongitude());
                    if (firstTimeFlag) {
                        firstTimeFlag = false;
                        googleMapHelper.animateCamera(new LatLng(location.getLatitude(), location.getLongitude()), googleMap);
                        startDistanceTracking();
                    }
                    showOrAnimateMarker(location);
                });
    }

    private void startDistanceTracking() {
        viewModel.startLocationTracking();
        viewModel.distanceTracker()
                .observe(this, distance -> {
                    Log.e("Total distance -> ", distance);
                    distanceCoveredTextView.setText(distance);
                });
    }

    private void showOrAnimateMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (marker == null)
            marker = googleMap.addMarker(googleMapHelper.getCurrentMarkerOptions(latLng));
        else MarkerAnimationHelper.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Spherical());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            int value = grantResults[0];
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            } else if (value == PackageManager.PERMISSION_GRANTED) viewModel.requestLocationUpdates();
        }
    }
}
