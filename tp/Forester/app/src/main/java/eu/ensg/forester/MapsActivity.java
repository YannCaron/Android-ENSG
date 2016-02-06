package eu.ensg.forester;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;

public class MapsActivity extends AppCompatActivity implements Constants, OnMapReadyCallback, LocationListener {

    // constants
    public static final float ZOOM_INIT = 10f;
    public static final float ZOOM_POI = 15f;

    // views
    private TextView positionLabel;
    private GoogleMap mMap;
    private View recordLayout;
    private Button recordSave;
    private Button recordAbort;

    // attributs
    private int foresterID;
    private Point currentPosition = new Point(6.2341579, 46.193253);
    private boolean isRecording = false;
    private Polygon currentSector;
    private com.google.android.gms.maps.model.Polygon currentPolygon;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // récupère le foresterID
        foresterID = getIntent().getIntExtra(EXTRA_FORESTER_ID, -1);

        // récupère les vues
        positionLabel = (TextView) findViewById(R.id.position);
        recordLayout = findViewById(R.id.record_layout);
        recordSave = (Button) findViewById(R.id.record_save);
        recordAbort = (Button) findViewById(R.id.record_abort);

        recordSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_onClick(v);
            }
        });

        recordAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abort_onClick(v);
            }
        });
    }

    // callback lorsque la map est chargée
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Zoom à la position courante
        moveTo(currentPosition);
        zoomTo(ZOOM_INIT);

        // Evénement GPS
        GPSUtils.requestLocationUpdates(this, this);

    }

    @Override
    protected void onPause() {
        // Attention, doit être placé avant l'appel à la méthode surchargée
        GPSUtils.removeUpdates(this, this);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        GPSUtils.requestLocationUpdates(this, this);
    }

    // quand on touche le bouton save
    private void save_onClick(View v) {
        isRecording = false;
        recordLayout.setVisibility(View.GONE);

        // TODO : do something
    }

    // quand on touche le bouton abort
    private void abort_onClick(View v) {
        isRecording = false;
        recordLayout.setVisibility(View.GONE);
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
            case (R.id.action_switch_type):
                switchType_onMenu(item);
                return true;
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
    private void switchType_onMenu(MenuItem item) {
        if (mMap.getMapType() == mMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() == mMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(mMap.MAP_TYPE_HYBRID);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        }
    }

    private void addPoi_onMenu(MenuItem item) {
        addPointOfInterest(currentPosition);
        moveTo(currentPosition);
        zoomTo(ZOOM_POI);
    }

    private void addSector_onMenu(MenuItem item) {
        isRecording = true;
        currentSector = new Polygon();
        recordLayout.setVisibility(View.VISIBLE);
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

    private void moveTo(Point position) {
        if (!checkMap()) return;

        // positionnement initial
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position.toLatLng()));

    }

    private void zoomTo(float zoom) {
        if (!checkMap()) return;

        // animation
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);
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

    public void drawPolygon(Polygon geom) {
        // efface le dernier polygone dessiné et le retrace
        if (currentPolygon != null) currentPolygon.remove();
        currentPolygon = addPolygon(geom);
    }

    public com.google.android.gms.maps.model.Polygon addPolygon(Polygon geom) {

        PolygonOptions options = new PolygonOptions();

        XY first = null;
        for (XY xy : geom.getCoordinates().getCoords()) {
            if (first == null) first = xy;
            options.add(new LatLng(xy.getY(), xy.getX()));
        }

        options.strokeColor(ContextCompat.getColor(this, R.color.color_stroke_polygon))
                .fillColor(ContextCompat.getColor(this, R.color.color_fill_polygon)).geodesic(true);

        return mMap.addPolygon(options);
    }

    // endregion

    // region LocationListener

    @Override
    public void onLocationChanged(Location location) {
        currentPosition = new Point(new XY(location));
        positionLabel.setText(currentPosition.toString());

        if (isRecording == true) {
            currentSector.addCoordinate(new XY(location));
            drawPolygon(currentSector);
            moveTo(currentPosition);
        }
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
