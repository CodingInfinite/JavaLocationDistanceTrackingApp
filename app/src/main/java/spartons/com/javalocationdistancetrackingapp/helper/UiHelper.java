package spartons.com.javalocationdistancetrackingapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationRequest;
import spartons.com.javalocationdistancetrackingapp.R;
import spartons.com.javalocationdistancetrackingapp.listener.IPositiveNegativeDialogListener;

public class UiHelper {

    private final Context context;

    public UiHelper(Context context) {
        this.context = context;
    }

    public boolean isPlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == status;
    }

    public boolean isHaveLocationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationProviderEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showPositiveDialogWithListener(Activity activity, String title, String content, final IPositiveNegativeDialogListener positiveNegativeDialogListener, String positiveText, boolean cancelable) {
        buildDialog(activity, title, content)
                .getBuilder()
                .positiveText(positiveText)
                .positiveColor(getPrimaryColor())
                .onPositive((dialog, which) -> positiveNegativeDialogListener.onPositiveClick())
                .cancelable(cancelable)
                .show();
    }

    private MaterialDialog buildDialog(Activity activity, String title, String content) {
        return new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .build();
    }

    private int getPrimaryColor() {
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }

    public LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        return locationRequest;
    }
}
