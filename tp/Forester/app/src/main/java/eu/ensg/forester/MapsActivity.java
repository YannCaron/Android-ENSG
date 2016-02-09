package eu.ensg.forester;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.File;
import java.io.IOException;

import eu.ensg.commons.io.FileSystem;
import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;
import jsqlite.Stmt;

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
    private Polygon currentDistrict;
    private com.google.android.gms.maps.model.Polygon currentPolygon;

    // database
    private SpatialiteOpenHelper helper;
    private SpatialiteDatabase database;

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

        // init database
        initDatabase();
        databaseBackup_onMenu(null);
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

        loadPointOfInterests();
        loadDistricts();

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

        storeDistrict("District", currentDistrict.toString(), currentDistrict);
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
            case (R.id.action_add_district):
                addDistrict_onMenu(item);
                return true;
            case R.id.database_backup:
                databaseBackup_onMenu(item);
                return true;
            case R.id.database_restore:
                databaseRestore_onMenu(item);
                return true;
            case R.id.database_clear:
                databaseClear_onMenu(item);
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
        addPointOfInterest("Point of interest", currentPosition.toString(), currentPosition);
        moveTo(currentPosition);
        zoomTo(ZOOM_POI);

        storePointOfInterest("Point of interest", currentPosition.toString(), currentPosition);
    }

    private void addDistrict_onMenu(MenuItem item) {
        isRecording = true;
        currentDistrict = new Polygon();
        currentDistrict.addCoordinate(currentPosition.getCoordinate());

        recordLayout.setVisibility(View.VISIBLE);
    }

    private void copyFiles(File from, File to) {
        Log.i(this.getClass().getName(), String.format("Copy file [%s] to [%s].", from, to));

        try {
            FileSystem.copyFile(from, to);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), String.format("Error during file copy [%s] to [%s].", from, to));
            e.printStackTrace();
        }

        Toast.makeText(this, String.format("Database copied to: %s", to), Toast.LENGTH_LONG).show();
    }

    private void databaseBackup_onMenu(MenuItem item) {
        File database = helper.getDatabaseFile();
        File sdcard = new File(Environment.getExternalStorageDirectory(), helper.getDatabaseName());

        copyFiles(database, sdcard);
    }

    private void databaseRestore_onMenu(MenuItem item) {
        File database = helper.getDatabaseFile();
        File sdcard = new File(Environment.getExternalStorageDirectory(), helper.getDatabaseName());

        copyFiles(sdcard, database);
    }

    private void databaseClear_onMenu(MenuItem item) {
        try {
            database.exec("DELETE FROM PointOfInterest where foresterID = " + foresterID);
            database.exec("DELETE FROM District where foresterID = " + foresterID);
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
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

    private void addPointOfInterest(String name, String description, Point position) {
        if (!checkMap()) return;

        // ajoute un marqueur
        mMap.addMarker(new MarkerOptions()
                        .position(position.toLatLng())
                        .title(name)
                        .snippet(description)
        );
    }

    public void drawPolygon(Polygon geom) {
        // efface le dernier polygone dessiné et le retrace
        if (currentPolygon != null) currentPolygon.remove();
        currentPolygon = addPolygon(geom);
    }

    public com.google.android.gms.maps.model.Polygon addPolygon(Polygon geom) {

        PolygonOptions options = new PolygonOptions();

        for (XY xy : geom.getCoordinates().getCoords()) {
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
            currentDistrict.addCoordinate(new XY(location));
            drawPolygon(currentDistrict);
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

    // region database

    private void initDatabase() {

        try {
            helper = new ForesterSpatialiteOpenHelper(this);
            database = helper.getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    private void loadPointOfInterests() {
        try {
            Stmt stmt = database.prepare("SELECT name, description, ST_asText(position) FROM PointOfInterest WHERE foresterID = " + foresterID);
            while (stmt.step()) {
                String name = stmt.column_string(0);
                String description = stmt.column_string(1);
                Point position = Point.unMarshall(stmt.column_string(2));

                addPointOfInterest(name, description, position);
            }
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Sql Error !!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void loadDistricts() {
        try {
            Stmt stmt = database.prepare("SELECT name, ST_asText(area) as area FROM District WHERE foresterID = " + foresterID);
            while (stmt.step()) {

                Log.w(Polygon.class.getName(), "LOAD POLYGON " + stmt.column_string(1));

                Polygon polygon = Polygon.unMarshall(stmt.column_string(1));

                Log.w(Polygon.class.getName(), "PARSE POLYGON " + polygon);

                addPolygon(polygon);
            }
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Sql Error !!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void storePointOfInterest(String name, String description, Point position) {
        try {
            database.exec("INSERT INTO PointOfInterest (foresterID, name, description, position) VALUES (" + foresterID + ", '" + name + "', '" + description + "', " + position.toSpatialiteQuery(ForesterSpatialiteOpenHelper.GPS_SRID) + ")");
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Sql Error !!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void storeDistrict(String name, String description, Polygon area) {
        try {
            database.exec("INSERT INTO District (foresterID, name, description, area) VALUES (" + foresterID + ", '" + name + "', '" + description + "', " + area.toSpatialiteQuery(ForesterSpatialiteOpenHelper.GPS_SRID) + ")");
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Sql Error !!!!", Toast.LENGTH_LONG).show();
        }
    }

    // endregion

}
