package spartons.com.javalocationdistancetrackingapp.activities.viewModel;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.os.Looper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import spartons.com.javalocationdistancetrackingapp.helper.GoogleMapHelper;
import spartons.com.javalocationdistancetrackingapp.util.AppRxSchedulers;

import java.util.concurrent.TimeUnit;

public class MainActivityViewModel extends ViewModel {

    private final AppRxSchedulers appRxSchedulers;
    private final LocationRequest locationRequest;
    private final FusedLocationProviderClient locationProviderClient;
    private final GoogleMapHelper googleMapHelper;

    private LocationCallback locationCallback;
    private Location locationTrackingCoordinates;
    private Location currentLocation;

    private MediatorLiveData<Location> _locationLiveData = new MediatorLiveData<>();
    private MediatorLiveData<String> _distanceTracker = new MediatorLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean locationFirstTimeFlag = true;
    private long totalDistance = 0L;

    public MainActivityViewModel(AppRxSchedulers appRxSchedulers, LocationRequest locationRequest, FusedLocationProviderClient locationProviderClient, GoogleMapHelper googleMapHelper) {
        this.appRxSchedulers = appRxSchedulers;
        this.locationRequest = locationRequest;
        this.locationProviderClient = locationProviderClient;
        this.googleMapHelper = googleMapHelper;
        createLocationCallback();
    }

    LiveData<Location> currentLocation() {
        return _locationLiveData;
    }

    LiveData<String> distanceTracker() {
        return _distanceTracker;
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (locationFirstTimeFlag) {
                    currentLocation = location;
                    _locationLiveData.setValue(currentLocation);
                }
                float accuracy = location.getAccuracy();
                if (!currentLocation.hasAccuracy() || accuracy > 10f) return;
                currentLocation = location;
                _locationLiveData.setValue(currentLocation);
            }
        };
    }

    @SuppressLint("MissingPermission")
    void requestLocationUpdates() {
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    void startLocationTracking() {
        locationTrackingCoordinates = currentLocation;
        compositeDisposable.add(Observable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(appRxSchedulers.threadPoolSchedulers())
                .subscribe(__ -> makeDistanceCalculationCall()
                        , t -> startLocationTracking())
        );
    }

    private void makeDistanceCalculationCall() {
        Location tempLocation = currentLocation;
        String destination[] = {String.valueOf(tempLocation.getLatitude()) + ",".concat(String.valueOf(tempLocation.getLongitude()))};
        String origins[] = {String.valueOf(locationTrackingCoordinates.getLatitude()) + ",".concat(String.valueOf(locationTrackingCoordinates.getLongitude()))};
        DistanceMatrixApi.getDistanceMatrix(googleMapHelper.geoContextDistanceApi(), origins, destination)
                .mode(TravelMode.WALKING)
                .setCallback(new PendingResult.Callback<DistanceMatrix>() {
                    @Override
                    public void onResult(DistanceMatrix result) {
                        locationTrackingCoordinates = tempLocation;
                        long temp = result.rows[0].elements[0].distance.inMeters;
                        totalDistance += temp;
                        _distanceTracker.postValue(getDistance());
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private String getDistance() {
        return googleMapHelper.getDistanceInKm(totalDistance);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        locationProviderClient.removeLocationUpdates(locationCallback);
        locationCallback = null;
    }
}

