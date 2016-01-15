package eu.ensg.forester;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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

public class MapsFragment extends Fragment {

    private GoogleMap googleMap;
    private MapReadyListener mapReadyListener;
    private com.google.android.gms.maps.model.Polygon currentPolygon;

    public MapsFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsFragment.this.googleMap = googleMap;

                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = inflater.inflate(R.layout.custom_info_window, null);

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

                fireMapReady(googleMap);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setMapReadyListener(MapReadyListener mapReadyListener) {
        this.mapReadyListener = mapReadyListener;
    }

    private void fireMapReady(GoogleMap googleMap) {
        if (mapReadyListener != null) {
            mapReadyListener.onMapReady(googleMap);
        }
    }

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

    public interface MapReadyListener {

        void onMapReady(GoogleMap googleMap);

    }
}
