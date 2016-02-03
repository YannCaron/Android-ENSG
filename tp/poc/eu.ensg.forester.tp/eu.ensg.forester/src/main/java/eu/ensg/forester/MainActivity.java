package eu.ensg.forester;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import eu.ensg.forester.data.WeatherObservation;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.Polygon;
import jsqlite.Database;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // views
    private MyMapFragment mapFragment;
    private TextView coordLabel;
    private LinearLayoutCompat recordControl;

    // gps
    private LocationManager locationManager;

    // geo
    // TODO : Ces données doivent être mises à jour à partir des données du GPS
    private Location currentLocation;
    private Polygon shape;

    // database
    private Database database;
    private SpatialiteOpenHelper helper;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // TODO: récupérer l'instance des vues
        /*
        mapFragment = TODO : getSupportFragmentManager()
        recordControl = TODO : findById
        coordLabel = TODO
        */

        // TODO : récupérer l'instance du location manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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

                // TODO :
                /*mapFragment.setMapReadyListener(new MyMapFragment.MapReadyListener() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                        isMapInitialized = true;
                        tryToInitMap();
                    }
                });*/
            }

            @Override
            protected String doInBackground(Void... params) {
                // Attention !!!! Exécuté dans un autre thread

                // TODO [DB_INST1] : Instancier l'open helper et récupérer l'instance de la database
                // TODO [DB_INST2] : Gérer les exceptions

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        } // TODO : Gérer ici le menu principale de l'activity

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO : Gérer le menu du navigation drawer (s'inspirer de la structure if / elseif de la méthode onOptionsItemSelected)

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // region spatial

    public void queryPointOfInterest() {

        // TODO [DB_QUERY1] :

    }

    public void querySector() {

        // TODO [DB_QUERY2] :

    }


    // endregion

    // region weather

    private void requestMeteo() {

        new AsyncTask<Location, Void, String>() {
            ProgressDialog dialog;
            String url = null;

            @Override
            protected void onPreExecute() {
                // crée la fenêtre de dialogue
                dialog = ProgressDialog.show(MainActivity.this, "Querying meteo !", "Please wait ...", true, true);
            }

            @Override
            protected String doInBackground(Location... params) {
                // ATTENTION ! Exécuté dans un autre thread

                /*
                TODO : Gérer l'appel et le chargement du WebService
                    - Construire l'URL du type "http://api.geonames.org/findNearByWeatherJSON?lat=%.4f&lng=%.4f&username=cyann";
                    A partir des données de localisation : Location.getLatiture(), Location.getLongitude
                    - Charger les données grâce à l'utilitaire fournis WebServices.requestContent(String url)

                ATTENTION : Méthode exécuté en dehors du Thread UI
                */

                return null;
            }

            @Override
            protected void onPostExecute(String res) {
                // supprime la fenêtre de dialogue
                dialog.dismiss();

                // gère si la donnée JSon n'est pas correctement formatée
                if (res == null) {
                    // ATTENTION : Méthode exécuté en dehors du Thread UI
                    Toast.makeText(MainActivity.this, "Unable to reach URL: " + url, Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    String condition = null;
                    String cloud = null;
                    LatLng location = null;
                    int temperature = 0;
                    int windSpeed = 0;

                    /*
                    TODO : Gérer le parsing des données JSon
                        - Utiliser la méthode JSONObject.getJSONObject(String name) sur le noeud weatherObservation
                        - Utiliser les méthodes JSONObject.getString(), JSONObject.getInt(), JSONObject.getDouble() sur les éléments weatherCondition, clouds, lat, lng, temperature, windSpeed
                    */

                    WeatherObservation weatherObservation = new WeatherObservation(location, condition, cloud, temperature, windSpeed);

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
