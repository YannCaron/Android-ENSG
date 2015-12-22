package eu.ensg.forester;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.maps_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsFragment.this.googleMap = googleMap;
                fireMapReady(googleMap);
            }
        });
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

    public void addMarker(Location location, String name) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.addMarker(new MarkerOptions().position(position).title(name));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public void addMarker(Point location, String name) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getCoordinate().getY(), location.getCoordinate().getX());

            googleMap.addMarker(new MarkerOptions().position(position).title(name));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public com.google.android.gms.maps.model.Polygon addPolygon(Polygon geom, int color) {

        PolygonOptions options = new PolygonOptions();

        XY first = null;
        for (XY xy : geom.getCoordinates().getCoords()) {
            if (first == null) first = xy;
            options.add(new LatLng(xy.getY(), xy.getX()));
        }

        options.fillColor(color).geodesic(true);

        return googleMap.addPolygon(options);
    }

    public void drawPolygon(Polygon geom, int color) {
        clearPolygon();
        currentPolygon = addPolygon(geom, color);
    }

    public void clearPolygon() {
        if (currentPolygon != null) currentPolygon.remove();
    }

    public void clear() {
        googleMap.clear();
    }


    public interface MapReadyListener {

        void onMapReady(GoogleMap googleMap);

    }
}
