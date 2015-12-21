package eu.ensg.forester;

import android.Manifest;
import android.content.Context;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;

import eu.ensg.commons.io.FileSystem;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;
import jsqlite.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    TextView textBox;
    LocationManager locationManager;
    MapsFragment mapsFragment;

    // TODO mettre dans une class à part
    Database database;
    SpatialiteOpenHelper helper;

    public static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_COARSE_LOCATION = 2;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO:  Utilisation d'une string (localisation)
                Snackbar.make(view, getResources().getString(R.string.info_record), Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        textBox = (TextView) findViewById(R.id.textBox);
        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        Log.e(this.getClass().getName(), "Maps Fragment: " + mapsFragment);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        try {
            helper = new MySpatialiteHelper(this);
            database = helper.getDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), database.dbversion(), Toast.LENGTH_LONG).show();

        textBox.setText(queryPointInPolygon());
    }

    // region GPS management (API23)

    private boolean checkGPSPermission() {

        // TODO:  Permission management in API 23 see http://stackoverflow.com/questions/33460603/running-targeting-sdk-22-app-in-android-6-sdk-23
        // TODO: 
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_FINE_LOCATION))
            return false;
        if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSIONS_REQUEST_COARSE_LOCATION))
            return false;

        return true;
    }

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

    // endregion

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            // Handle the camera action
        } else if (id == R.id.nav_poi) {
            recordPoi();
        } else if (id == R.id.nav_area) {

        } else if (id == R.id.nav_explorer) {

        } else if (id == R.id.nav_save) {
            saveDatabase();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    // region application manager

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

    // endregion

    // region location
    private Location currentLocation;
    private Polygon shape;

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

            Point point = new Point(MySpatialiteHelper.GPS_SRID, MySpatialiteHelper.coordFactory(currentLocation));
            helper.exec(
                    "insert into " + MySpatialiteHelper.TABLE_INTEREST +
                            "(" + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COORDINATE + ") " +
                            " values ('" + "My coord" + "', " + point.toSpatialiteQuery() + ");");
            textBox.setText(queryPointInPolygon());
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }


    }

    public String queryVersions() throws Exception {
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

    public String queryPointInPolygon() {

        // select * from districts where within(ST_Transform(GeomFromText('POINT(-97.837543 30.418986)', 4326), 2277),districts.Geometry);
        //String query = "select * from " + MySpatialiteHelper.TABLE_INTEREST + ";";
        String query = "select " + MySpatialiteHelper.COLUMN_ID + ", " + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_COMMENT + ", AsText(" + MySpatialiteHelper.COLUMN_COORDINATE + ") as coord from " + MySpatialiteHelper.TABLE_INTEREST + ";";
        //String query = "PRAGMA table_info(" + MySpatialiteHelper.TABLE_INTEREST + ");";

        String result = "";
        try {
            result = helper.dumpQuery(query);
        } catch (jsqlite.Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }

        Log.i(this.getClass().getName(), result);

        return result;
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
        shape = new Polygon(MySpatialiteHelper.GPS_SRID);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        if (shape != null) {
            shape.addCoordinate(new XY(location.getLongitude(), location.getLatitude()));
        }

        Toast.makeText(MainActivity.this, "GPS Location changed: " + new Point(MySpatialiteHelper.GPS_SRID, MySpatialiteHelper.coordFactory(location)).toString(), Toast.LENGTH_SHORT).show();
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

    // endregion

}
