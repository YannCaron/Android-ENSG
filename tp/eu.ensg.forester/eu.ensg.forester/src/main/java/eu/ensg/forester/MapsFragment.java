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

import eu.ensg.spatialite.geom.Point;

public class MapsFragment extends Fragment {

    GoogleMap googleMap;
    MapReadyListener mapReadyListener;

    public interface MapReadyListener {

        void onMapReady(GoogleMap googleMap);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.maps_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SupportMapFragment fragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.maps);
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

    public void moveTo(Location location) {
        if (googleMap != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f));

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
}
