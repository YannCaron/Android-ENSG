package eu.ensg.forester;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.IOException;

import eu.ensg.commons.io.FileSystem;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    public static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_COARSE_LOCATION = 2;
    // TODO mettre dans une class à part
    Database database;

    // view
    SpatialiteOpenHelper helper;
    // manager
    private NavigationManager navigationManager;
    private LocationManager locationManager;
    private MapsFragment mapsFragment;
    private LinearLayoutCompat recordControl;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Location currentLocation;

    // region GPS management (API23)
    private Polygon shape;

    private boolean checkGPSPermission() {

        // TODO:  Permission management in API 23 see http://stackoverflow.com/questions/33460603/running-targeting-sdk-22-app-in-android-6-sdk-23
        // TODO:
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_FINE_LOCATION))
            return false;
        if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSIONS_REQUEST_COARSE_LOCATION))
            return false;

        return true;
    }

    // endregion

    private boolean checkPermission(String permission, int code) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, code);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_COARSE_LOCATION:
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.info_restart), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.info_not_authorized), Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationManager = new NavigationManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabPoi = (FloatingActionButton) findViewById(R.id.fab_poi);
        fabPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO:  Utilisation d'une string (localisation)
                Snackbar.make(view, getResources().getString(R.string.info_poi), Snackbar.LENGTH_LONG).setAction(R.string.title_action, null).show();
                recordPoi();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // TODO: récupérer les controles
        recordControl = (LinearLayoutCompat) this.findViewById(R.id.record_control);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        Log.e(this.getClass().getName(), "Maps Fragment: " + mapsFragment);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        try {
            helper = new MySpatialiteHelper(this);
            database = helper.getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), database.dbversion(), Toast.LENGTH_LONG).show();

        mapsFragment.setMapReadyListener(new MapsFragment.MapReadyListener() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                queryPointOfInterest();
                querySector();
                mapsFragment.moveTo(currentLocation, 12f);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO: stopper le GPS lorsque l'app est en pause
        stopGPS();
        shape = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // region application manager

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            // Handle the camera action
            navigationManager.activityMap();
        } else if (id == R.id.nav_poi_table) {
            navigationManager.activityData();
        } else if (id == R.id.nav_sector_table) {
            navigationManager.activityData();
        } else if (id == R.id.nav_poi) {
            recordPoi();
        } else if (id == R.id.nav_area) {
            startRecordShape();
        } else if (id == R.id.nav_clear) {
            clearDatabase();
        } else if (id == R.id.nav_save) {
            saveDatabase();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearDatabase() {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.title_delete)
                .setMessage(getString(R.string.msg_delete))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String query = "Delete from " + MySpatialiteHelper.TABLE_INTEREST + ";";
                            helper.exec(query);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            String query = "Delete from " + MySpatialiteHelper.TABLE_INTEREST + ";";
                            helper.exec(query);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, "Data cleared !", Toast.LENGTH_SHORT).show();
                        mapsFragment.clear();

                    }
                })
                .setCancelable(true).show();

    }

    private void saveDatabase() {
        File database = helper.getDatabaseFile();
        File sdcard = new File(Environment.getExternalStorageDirectory(), helper.getDatabaseName());

        Log.i(this.getClass().getName(), String.format("Copy file [%s] to [%s].", database, sdcard));

        try {
            FileSystem.copyFile(database, sdcard);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), String.format("Error during file copy [%s] to [%s].", database, sdcard));
            e.printStackTrace();
        }

        Toast.makeText(this, String.format("Database copied to: %s", sdcard), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        currentLocation = getLastLocation();
        startGPS();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://eu.ensg.forester/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    // endregion

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        // TODO:  Désactive le GPS
        // TODO:  Faire avant le saveInstanceState
        stopGPS();
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://eu.ensg.forester/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // region location

    private Location getLastLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkGPSPermission();
            return null;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        return locationManager.getLastKnownLocation(provider);
    }

    private void recordPoi() {
        /*ContentValues values = new ContentValues();
        values.put(MySpatialiteHelper.COLUMN_NAME, "My first point");
        values.put(MySpatialiteHelper.COLUMN_COMMENT, "A comment");
        values.put(MySpatialiteHelper.COLUMN_COORDINATE, "GeomFromText('POINT(1.01 2.02)', 4326)");

        database.insert(MySpatialiteHelper.TABLE_INTEREST, null, values);*/

        try {
            if (currentLocation == null) {
                Toast.makeText(this, "Position not available !", Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), "GPS does not work or is not authorized, current position not available !");
                return;
            }

            mapsFragment.addMarker(currentLocation, "My coord");
            mapsFragment.moveTo(currentLocation, 15f);

            Point point = new Point(MySpatialiteHelper.coordFactory(currentLocation));
            helper.exec(
                    "insert into " + MySpatialiteHelper.TABLE_INTEREST +
                            "(" + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COORDINATE + ") " +
                            " values ('" + "My coord" + "', " + point.toSpatialiteQuery(MySpatialiteHelper.GPS_SRID) + ");");

            Toast.makeText(this, "Point of interest saved successfully !", Toast.LENGTH_SHORT).show();

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }


    }

    public String queryVersions() throws jsqlite.Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Check versions...\n");

        Stmt stmt01 = database.prepare("SELECT spatialite_version();");
        if (stmt01.step()) {
            sb.append("\t").append("SPATIALITE_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = database.prepare("SELECT proj4_version();");
        if (stmt01.step()) {
            sb.append("\t").append("PROJ4_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = database.prepare("SELECT geos_version();");
        if (stmt01.step()) {
            sb.append("\t").append("GEOS_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }
        stmt01.close();

        sb.append("Done...\n");
        return sb.toString();
    }

    public void queryPointOfInterest() {

        //String query = "select * from " + MySpatialiteHelper.TABLE_INTEREST + ";";
        String query = "select " + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COMMENT + ", AsText(" + MySpatialiteHelper.COLUMN_COORDINATE + ") as coord from " + MySpatialiteHelper.TABLE_INTEREST + ";";
        //String query = "PRAGMA table_info(" + MySpatialiteHelper.TABLE_INTEREST + ");";

        Stmt stmt = null;
        try {
            stmt = database.prepare(query);

            while (stmt.step()) {
                String name = stmt.column_string(0);
                // String comment = stmt.column_string(1);
                String coordStr = stmt.column_string(2);

                if (coordStr != null) {
                    Point coord = Point.unMarshall(new StringBuilder(coordStr));
                    Log.w(this.getClass().getName(), "Coordinate: " + stmt.column_string(2));

                    mapsFragment.addMarker(coord, name);
                }
            }

        } catch (jsqlite.Exception e) {
            Log.e(this.getClass().getName(), String.format("Cannot execute query %s ", query));
            e.printStackTrace();
        }

    }

    public void querySector() {

        // select * from districts where within(ST_Transform(GeomFromText('POINT(-97.837543 30.418986)', 4326), 2277),districts.Geometry);
        //String query = "select * from " + MySpatialiteHelper.TABLE_INTEREST + ";";
        String query = "select " + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COMMENT + ", AsText(" + MySpatialiteHelper.COLUMN_COORDINATE + ") as coord from " + MySpatialiteHelper.TABLE_SECTOR + ";";

        Stmt stmt = null;
        try {
            stmt = database.prepare(query);

            while (stmt.step()) {
                String name = stmt.column_string(0);
                // String comment = stmt.column_string(1);
                String coordStr = stmt.column_string(2);

                if (coordStr != null) {
                    Polygon coord = Polygon.unMarshall(new StringBuilder(coordStr));
                    Log.w(this.getClass().getName(), "Coordinate: " + coord.toString());

                    mapsFragment.addPolygon(coord,
                            getResources().getColor(R.color.colorStrokePolygon),
                            getResources().getColor(R.color.colorFillPolygon));
                }
            }

        } catch (jsqlite.Exception e) {
            Log.e(this.getClass().getName(), String.format("Cannot execute query %s ", query));
            e.printStackTrace();
        }

    }

    private void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkGPSPermission();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, MainActivity.this);
    }

    private void stopGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkGPSPermission();
            return;
        }
        locationManager.removeUpdates(MainActivity.this);
    }

    private void startRecordShape() {

        Toast.makeText(MainActivity.this, "GPS recording started", Toast.LENGTH_SHORT).show();
        shape = new Polygon();
        recordControl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        if (shape != null) {
            shape.addCoordinate(new XY(location.getLongitude(), location.getLatitude()));
            mapsFragment.drawPolygon(shape,
                    getResources().getColor(R.color.colorStrokePolygon),
                    getResources().getColor(R.color.colorFillPolygon));
            mapsFragment.moveTo(location, 15);
        }

        Toast.makeText(MainActivity.this, "GPS Location changed: " + new Point(MySpatialiteHelper.coordFactory(location)).toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(MainActivity.this, "GPS status changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(MainActivity.this, "GPS Provider Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "GPS Disabled", Toast.LENGTH_SHORT).show();
    }

    public void recordSave(View view) {
        recordControl.setVisibility(View.INVISIBLE);

        try {
            helper.exec(
                    "insert into " + MySpatialiteHelper.TABLE_SECTOR +
                            "(" + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COORDINATE + ")" +
                            " values ('" + "My Sector" + "', " + shape.toSpatialiteQuery(MySpatialiteHelper.GPS_SRID) + ");");

            Toast.makeText(this, "Shape saved successfully !", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        shape = null;

    }

    public void recordAbort(View view) {
        recordControl.setVisibility(View.INVISIBLE);
        shape = null;
        mapsFragment.clearPolygon();
    }

    // endregion

}
