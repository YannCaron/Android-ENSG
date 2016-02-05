package eu.ensg.spatialite;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.cyann.euensgspatialite.R;

/**
 * Created by cyann on 04/02/16.
 */
public class GPSUtils {

    public static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_COARSE_LOCATION = 2;

    public static void requestLocationUpdates(Activity activity, LocationListener listener) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkGPSPermission(activity);
            return;
        }

        // Notez que les paramètres sont placés dans le fichier /res/values/constants.xml de la librairie
        // Peuvent être surchargés dans le projet (à vérifier)
        Resources res = activity.getResources();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, res.getInteger(R.integer.gps_refresh_min_time_ms), res.getInteger(R.integer.gps_refresh_min_dist_m), listener);
    }

    public static void removeUpdates(Activity activity, LocationListener listener) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkGPSPermission(activity);
            return;
        }

        locationManager.removeUpdates(listener);
    }

    private static boolean checkGPSPermission(Activity activity) {

        // TODO:  Permission management in API 23 see http://stackoverflow.com/questions/33460603/running-targeting-sdk-22-app-in-android-6-sdk-23
        // TODO:
        if (!checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_FINE_LOCATION))
            return false;
        if (!checkPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSIONS_REQUEST_COARSE_LOCATION))
            return false;

        return true;
    }

    private static boolean checkPermission(Activity activity, String permission, int code) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
                return false;
            }
        }
        return true;
    }


}
