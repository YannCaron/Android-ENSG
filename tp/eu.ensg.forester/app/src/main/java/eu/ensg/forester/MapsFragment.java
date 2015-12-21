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

public class MapsFragment extends Fragment {

    GoogleMap map;

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
                map = googleMap;
            }
        });
    }

    public void moveTo(Location location) {
        if (map != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            map.moveCamera(CameraUpdateFactory.newLatLng(position));
            map.animateCamera(CameraUpdateFactory.zoomTo(10f));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }

    public void addMarker(Location location, String name) {
        if (map != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            map.addMarker(new MarkerOptions().position(position).title(name));
            map.moveCamera(CameraUpdateFactory.newLatLng(position));
            map.animateCamera(CameraUpdateFactory.zoomTo(15f));

        } else {
            Toast.makeText(getContext(), "Google maps not yet initialized !", Toast.LENGTH_LONG).show();
        }
    }
}
