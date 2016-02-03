package eu.ensg.forester;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import eu.ensg.commons.io.FileSystem;
import eu.ensg.commons.io.WebServices;
import eu.ensg.forester.businessmodel.WeatherObservation;
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
    private static final String METEO_URL = "http://api.geonames.org/findNearByWeatherJSON?lat=%.4f&lng=%.4f&username=cyann";
    // TODO mettre dans une class à part
    // manager
    private LocationManager locationManager;
    private NavigationManager navigationManager;
    // view
    private MyMapFragment mapFragment;
    private TextView coordLabel;
    private LinearLayoutCompat recordControl;
    // database
    private Database database;
    private SpatialiteOpenHelper helper;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // Geo
    private Location currentLocation;
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
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
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

        mapFragment = (MyMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        coordLabel = (TextView) this.findViewById(R.id.coord_label);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        new AsyncTask<Void, Void, String>() {
            boolean isDbInitialized = false, isMapInitialized = false;
            ProgressDialog dialog;
            String url = null;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(MainActivity.this, "Spatialite initializing", "Please wait ...", true, true);

                mapFragment.setMapReadyListener(new MyMapFragment.MapReadyListener() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                        isMapInitialized = true;
                        tryToInitMap();
                    }
                });
            }

            @Override
            protected String doInBackground(Void... params) {
                // TODO: !!!! Exécuté dans un autre thread

                try {
                    helper = new MySpatialiteHelper(MainActivity.this);
                    database = helper.getDatabase();
                } catch (jsqlite.Exception | IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String res) {
                dialog.dismiss();

                Toast.makeText(getApplicationContext(), database.dbversion(), Toast.LENGTH_LONG).show();
                isDbInitialized = true;
                tryToInitMap();

            }

            private void tryToInitMap() {
                if (isDbInitialized && isMapInitialized) {
                    queryPointOfInterest();
                    querySector();
                    if (currentLocation != null) {
                        mapFragment.moveTo(currentLocation, 12f);
                    }
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    // region application manager

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_clear) {
            clearDatabase();
        } else if (id == R.id.action_save) {
            saveDatabase();
        } else if (id == R.id.action_load) {
            overwriteDatabase();
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
            navigationManager.activityMap();
        } else if (id == R.id.nav_poi_table) {
            navigationManager.activityData();
        } else if (id == R.id.nav_sector_table) {
            navigationManager.activityData();
        } else if (id == R.id.nav_poi) {
            recordPoi();
        } else if (id == R.id.nav_area) {
            startRecordShape();
        } else if (id == R.id.nav_weather) {
            requestMeteo();
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
                            String query = "Delete from " + MySpatialiteHelper.TABLE_SECTOR + ";";
                            helper.exec(query);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, "Data cleared !", Toast.LENGTH_SHORT).show();

                        // reload database
                        mapFragment.clear();

                        queryPointOfInterest();
                        querySector();

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

    private void overwriteDatabase() {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.title_overwrite)
                .setMessage(getString(R.string.msg_overwrite))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        File database = helper.getDatabaseFile();
                        File sdcard = new File(Environment.getExternalStorageDirectory(), helper.getDatabaseName());

                        Log.i(this.getClass().getName(), String.format("Copy file [%s] to [%s].", sdcard, database));

                        try {
                            FileSystem.copyFile(sdcard, database);
                        } catch (IOException e) {
                            Log.e(this.getClass().getName(), String.format("Error during file copy [%s] to [%s].", sdcard, database));
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, String.format("Database copied to: %s", database), Toast.LENGTH_LONG).show();

                        mapFragment.clear();
                    }
                })
                .setCancelable(true).show();
    }

    // endregion

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

    // region location

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
        values.put(MySpatialiteHelper.COLUMN_DESCRIPTION, "A comment");
        values.put(MySpatialiteHelper.COLUMN_COORDINATE, "GeomFromText('POINT(1.01 2.02)', 4326)");

        database.insert(MySpatialiteHelper.TABLE_INTEREST, null, values);*/

        // TODO: Custom dialog http://examples.javacodegeeks.com/android/core/ui/alertdialog/android-prompt-user-input-dialog-example/
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View promptView = layoutInflater.inflate(R.layout.prompt_name_comment, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to be the layout file of the alertdialog builder
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        EditText name = (EditText) promptView.findViewById(R.id.txt_name);
                        EditText comment = (EditText) promptView.findViewById(R.id.txt_comment);

                        try {
                            if (currentLocation == null) {
                                Toast.makeText(MainActivity.this, "Position not available !", Toast.LENGTH_LONG).show();
                                Log.e(this.getClass().getName(), "GPS does not work or is not authorized, current position not available !");
                                return;
                            }

                            mapFragment.addMarker(currentLocation, name.getText().toString(), comment.getText().toString());
                            mapFragment.moveTo(currentLocation, 15f);

                            Point point = new Point(MySpatialiteHelper.coordFactory(currentLocation));
                            helper.exec(
                                    "insert into " + MySpatialiteHelper.TABLE_INTEREST +
                                            "(" + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_DESCRIPTION + ", " + MySpatialiteHelper.COLUMN_COORDINATE + ") " +
                                            " values ('" + name.getText() + "', '" + comment.getText() + "', " + point.toSpatialiteQuery(MySpatialiteHelper.GPS_SRID) + ");");

                            Toast.makeText(MainActivity.this, "Point of interest saved successfully !", Toast.LENGTH_SHORT).show();

                        } catch (jsqlite.Exception e) {
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();

        alert.show();

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
        //String query = "PRAGMA table_info(" + MySpatialiteHelper.TABLE_INTEREST + ");";

        String query = "select " + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_DESCRIPTION +
                ", AsText(" + MySpatialiteHelper.COLUMN_COORDINATE + ") as coord from " +
                MySpatialiteHelper.TABLE_INTEREST + ";";

        Stmt stmt = null;
        try {
            stmt = database.prepare(query);

            while (stmt.step()) {
                String name = stmt.column_string(0);
                String comment = stmt.column_string(1);
                String coordStr = stmt.column_string(2);

                if (coordStr != null) {
                    Point coord = Point.unMarshall(new StringBuilder(coordStr));
                    Log.w(this.getClass().getName(), "Coordinate: " + coordStr);

                    mapFragment.addMarker(coord, name, comment);
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
        String query = "select " + MySpatialiteHelper.COLUMN_NAME + ", " + MySpatialiteHelper.COLUMN_DESCRIPTION + ", AsText(" + MySpatialiteHelper.COLUMN_COORDINATE + ") as coord from " + MySpatialiteHelper.TABLE_SECTOR + ";";

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

                    mapFragment.addPolygon(coord,
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
            mapFragment.drawPolygon(shape,
                    getResources().getColor(R.color.colorStrokePolygon),
                    getResources().getColor(R.color.colorFillPolygon));
            mapFragment.moveTo(location, 15);
        }

        coordLabel.setText(new Point(MySpatialiteHelper.coordFactory(location)).toString());
        //Toast.makeText(MainActivity.this, "GPS Location changed: " + new Point(MySpatialiteHelper.coordFactory(location)).toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Toast.makeText(MainActivity.this, "GPS status changed", Toast.LENGTH_SHORT).show();
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

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View promptView = layoutInflater.inflate(R.layout.prompt_name_comment, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to be the layout file of the alertdialog builder
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (shape == null) {
                            Toast.makeText(MainActivity.this, "No area was recorded !", Toast.LENGTH_LONG).show();
                            return;
                        }

                        EditText name = (EditText) promptView.findViewById(R.id.txt_name);
                        EditText comment = (EditText) promptView.findViewById(R.id.txt_comment);

                        try {

                            helper.exec(
                                    "insert into " + MySpatialiteHelper.TABLE_SECTOR +
                                            "(" + MySpatialiteHelper.COLUMN_NAME + ", "
                                            + MySpatialiteHelper.COLUMN_COORDINATE + ")" +
                                            " values ('" + name.getText() + "', '"
                                            + comment.getText() + "', "
                                            + shape.toSpatialiteQuery(MySpatialiteHelper.GPS_SRID) + ");");

                            Toast.makeText(MainActivity.this, "Shape saved successfully !", Toast.LENGTH_SHORT).show();

                        } catch (jsqlite.Exception e) {
                            e.printStackTrace();
                        }


                        shape = null;

                    }
                });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();

        alert.show();
    }

    public void recordAbort(View view) {
        recordControl.setVisibility(View.INVISIBLE);
        shape = null;
        mapFragment.clearPolygon();
    }

    // endregion

    // region weather

    private void requestMeteo() {

        if (currentLocation == null) {
            Toast.makeText(MainActivity.this, "Cannot get weather, GPS not available !", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO : Classe interne, locale et anonyme
        // Classe locale et anonyme
        new AsyncTask<Location, Void, String>() {

            ProgressDialog dialog;
            String url = null;

            @Override
            protected void onPreExecute() {
                // UI thread
                dialog = ProgressDialog.show(MainActivity.this, "Querying meteo !", "Please wait ...", true, true);
            }

            @Override
            protected String doInBackground(Location... params) {
                // TODO: !!!! Exécuté dans un autre thread

                // Autre thread
                if (params.length != 1) return null;
                Location location = params[0];
                url = String.format(new Locale("en", "US"), METEO_URL, location.getLatitude(), location.getLongitude());
                Log.i(this.getClass().getName(), "Query URL: " + url);

                try {
                    return WebServices.requestContent(url);
                } catch (IOException e) {
                    Log.e(MainActivity.this.getClass().getName(), "Unable to reach URL: " + url);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String res) {
                // UI thread
                dialog.dismiss();

                if (res == null) {
                    XmlPullParser parser = Xml.newPullParser();
                    // TODO: !!!! Exécuté dans le thread UI
                    Toast.makeText(MainActivity.this, "Unable to reach URL: " + url, Toast.LENGTH_LONG).show();
                    return;
                }

                Log.i(this.getClass().getName(), "Webservice response: " + res);

                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONObject jsonObservation = jsonObject.getJSONObject("weatherObservation");
                    String condition = jsonObservation.getString("weatherCondition");
                    String cloud = jsonObservation.getString("clouds");
                    LatLng location = new LatLng(
                            jsonObservation.getDouble("lat"),
                            jsonObservation.getDouble("lng"));
                    int temperature = jsonObservation.getInt("temperature");
                    int windSpeed = jsonObservation.getInt("windSpeed");

                    WeatherObservation weatherObservation = new WeatherObservation(location, condition, cloud, temperature, windSpeed);
                    Log.i(this.getClass().getName(), "Weather observation: " + weatherObservation.toString());

                    mapFragment.applyWeather(weatherObservation);

                } catch (JSONException e) {
                    Log.e(MainActivity.this.getClass().getName(), "Unable to parse JSON string " + res);
                    e.printStackTrace();
                }
            }

            
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentLocation);

    }

    // endregion

}
