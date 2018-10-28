package spartons.com.javalocationdistancetrackingapp.helper;

import android.content.res.Resources;
import android.location.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import com.google.maps.GeoApiContext;
import spartons.com.javalocationdistancetrackingapp.R;

import java.text.DecimalFormat;

public class GoogleMapHelper {

    private static final float TILT_LEVEL = 18f;
    private static final float ZOOM_LEVEL = 18f;
    private final Resources resources;

    public GoogleMapHelper(Resources resources) {
        this.resources = resources;
    }

    /**
     * @return the distance map api key.
     */

    private String distanceApi() {
        return resources.getString(R.string.google_distance_matrix_api_key);
    }

    /**
     * The function returns the ${GeoApiContext} with distance api key.
     *
     * @return the geo api context with distance api.
     */

    public GeoApiContext geoContextDistanceApi() {
        return new GeoApiContext.Builder()
                .apiKey(distanceApi())
                .build();
    }

    /**
     * This function sets the default google map settings.
     *
     * @param googleMap to set default settings.
     */

    public void defaultMapSettings(GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.setBuildingsEnabled(true);
    }

    public CameraUpdate buildCameraUpdate(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        return buildCameraUpdate(latLng);
    }

    private CameraUpdate buildCameraUpdate(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(TILT_LEVEL)
                .zoom(ZOOM_LEVEL)
                .build();
        return CameraUpdateFactory.newCameraPosition(cameraPosition);
    }

    public void animateCamera(LatLng latLng, GoogleMap googleMap) {
        if (latLng == null) return;
        CameraUpdate cameraUpdate = buildCameraUpdate(latLng);
        googleMap.animateCamera(cameraUpdate, 10, null);
    }

    public MarkerOptions getCurrentMarkerOptions(LatLng position) {
        MarkerOptions markerOptions = getMarkerOptions(position);
        markerOptions.flat(true);
        return markerOptions;
    }

    private MarkerOptions getMarkerOptions(LatLng position) {
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(position);
    }

    public String getDistanceInKm(double totalDistance) {
        if (totalDistance == 0.0 || totalDistance < -1)
            return "0 Km";
        else if (totalDistance > 0 && totalDistance < 1000)
            return String.valueOf(totalDistance) + " meters";
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(totalDistance / 1000) + " Km";
    }

}
