package eu.ensg.forester;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import eu.ensg.forester.data.WeatherObservation;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;

/**
 * Created by cyann on 17/01/16.
 */
public class MyMapFragment extends SupportMapFragment {

    private GoogleMap googleMap;
    private MapReadyListener mapReadyListener;
    private com.google.android.gms.maps.model.Polygon currentPolygon;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        View view;
        // ATTENTION !!!! Invoquer le onCreateView de la super class, sinon exception
        // plutot que :     view = inflater.inflate(R.layout.map_fragment, group, false);
        view = super.onCreateView(inflater, group, bundle);

        // TODO : Gérer ici le chargement de la carte googleMap

        return view;
    }

    private void createCustomInfoWindow(final LayoutInflater inflater, GoogleMap googleMap) {

        // TODO : Gérer ici un InfoWindowAdapter personnalité
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = inflater.inflate(R.layout.map_custom_info_window, null);

                WeatherObservation.Condition condition = WeatherObservation.Condition.failSafeValueOf(marker.getTitle());

                if (condition != null) {
                    ImageView image = (ImageView) v.findViewById(R.id.info_image);
                    image.setImageResource(getWeatherImageRessource(condition));
                }

                TextView title = (TextView) v.findViewById(R.id.info_title);
                title.setText(marker.getTitle().toLowerCase());

                TextView snippet = (TextView) v.findViewById(R.id.info_snippet);
                snippet.setText(marker.getSnippet());

                return v;
            }
        });
    }

    // region methods

    public void moveTo(Location location, float zoom) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public void addMarker(Location location, String name, String comment) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(name)
                    .snippet(comment));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public void addMarker(Point location, String name, String comment) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getCoordinate().getY(), location.getCoordinate().getX());

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(name)
                    .snippet(comment));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public com.google.android.gms.maps.model.Polygon addPolygon(Polygon geom, int strokecolor, int fillcolor) {

        PolygonOptions options = new PolygonOptions();

        XY first = null;
        for (XY xy : geom.getCoordinates().getCoords()) {
            if (first == null) first = xy;
            options.add(new LatLng(xy.getY(), xy.getX()));
        }

        options.strokeColor(strokecolor).fillColor(fillcolor).geodesic(true);

        return googleMap.addPolygon(options);
    }

    public void drawPolygon(Polygon geom, int strokecolor, int fillcolor) {
        clearPolygon();
        currentPolygon = addPolygon(geom, strokecolor, fillcolor);
    }

    public void clearPolygon() {
        if (currentPolygon != null) currentPolygon.remove();
    }

    public void clear() {
        googleMap.clear();
    }

    // endregion

    // region weather

    public void applyWeather(WeatherObservation weather) {
        String weatherFormat = "Temperature: %d °C\nWind: %d knots";
        String weatherDescription = String.format(weatherFormat, weather.getTemperature(), weather.getWindSpeed());

        int resImage;

        googleMap.addMarker(
                new MarkerOptions()
                        .position(weather.getLocation())
                        .title(weather.getCondition().name())
                        .snippet(weatherDescription)
                        .icon(BitmapDescriptorFactory.fromResource(getWeatherImageRessource(weather.getCondition())))
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(weather.getLocation()));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10f));
    }

    private int getWeatherImageRessource(WeatherObservation.Condition condition) {
        switch (condition) {
            case SUN:
                return R.mipmap.sun;
            case CLOUD:
                return R.mipmap.cloud;
            case MIST:
            default:
                return R.mipmap.mist;
            case RAIN:
                return R.mipmap.rain;
            case SNOW:
                return R.mipmap.snow;
            case STORM:
                return R.mipmap.storm;
        }
    }

    // endregion

    // region custom event

    // TODO : Créer ici l'évènement personnalisé
    public void setMapReadyListener(MapReadyListener mapReadyListener) { // setter tout ce qu'il y a de plus classique
        this.mapReadyListener = mapReadyListener;
    }

    private void fireMapReady(GoogleMap googleMap) {

        // TODO : créer ici la gestion de la notification

    }

    public interface MapReadyListener {

        // TODO : créer ici le callback

    }

    // endregion
}
