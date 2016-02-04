package eu.ensg.forester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.geom.Point;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    // views
    private TextView positionLabel;
    private GoogleMap mMap;

    // attributs
    private Point currentPosition = new Point(6.2341579, 46.193253);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // récupère les vues
        positionLabel = (TextView)findViewById(R.id.position);
    }


    // callback lorsque la map est chargée
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Zoom à la position courante
        zoomTo(currentPosition);

        // Evénement GPS
        GPSUtils.requestLocationUpdates(this, this);

    }

    // region menu

    // charge le menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;

    }

    // gère les actions du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case (R.id.action_add_poi):
                addPoi_onMenu(item);
                return true;
            case (R.id.action_add_sector):
                addSector_onMenu(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // les événements
    private void addPoi_onMenu(MenuItem item) {
        addPointOfInterest(currentPosition);
    }

    private void addSector_onMenu(MenuItem item) {

    }

    // endregion

    // region map method

    // vérifie que la map soit bien chargé, sinon emet un toast
    private boolean checkMap() {
        if (mMap == null) {
            Toast.makeText(this, R.string.toast_google_map_unavailable, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void zoomTo(Point position) {
        if (!checkMap()) return;

        // positionnement initial
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position.toLatLng(), 15));

        // animation
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private void addPointOfInterest(Point position) {
        if (!checkMap()) return;

        // ajoute un marqueur
        mMap.addMarker(new MarkerOptions()
                        .position(position.toLatLng())
                        .title("Point of interest")
                        .snippet(position.toString())
        );
    }

    // endregion

    // region LocationListener

    @Override
    public void onLocationChanged(Location location) {
        currentPosition = new Point(location.getLongitude(), location.getLatitude());
        positionLabel.setText(currentPosition.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // endregion

}
