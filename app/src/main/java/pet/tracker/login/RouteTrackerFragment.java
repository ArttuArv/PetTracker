package pet.tracker.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.util.*;

public class RouteTrackerFragment extends Fragment {

    GoogleMap gMap;
    Polyline polyline = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    List<Double> lat = new ArrayList<>();
    List<Double> lng = new ArrayList<>();

    InfoStash gpsStash = new InfoStash();
    String gpsValues = gpsStash.gpsValues;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng oulu = new LatLng(65.010036, 25.467453);
            googleMap.addMarker(new MarkerOptions().position( oulu ).title("Marker in Oulu"));
            googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( oulu, 12 ) );

            gMap = googleMap;

            splitStr2Double( gpsValues );

            for ( int i = 0; i < lat.size(); i++ ) {

                latLngList.add( new LatLng( lat.get(i), lng.get(i) ) );

            }

            // Draw Polyline on Map
            // Polylines are useful to show route or some other connection between points.
            polyline = gMap.addPolyline( new PolylineOptions().clickable( true ).addAll( latLngList ) );
            polyline.setWidth( 12 );
            polyline.setColor( Color.RED );
            polyline.setJointType( JointType.ROUND );

            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    // Create MarkerOptions
                    MarkerOptions markerOptions = new MarkerOptions().position( latLng );
                    // Create Marker
                    Marker marker = gMap.addMarker( markerOptions );
                    // Add Latlng and Marker
                    latLngList.add( latLng );
                    markerList.add( marker );

                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_maps, container, false );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync( callback );
        }
    }

    public void splitStr2Double( String str ) {

        String[] latLngStr = str.split("<>");

        for( int i = 0; i < latLngStr.length; i++ ) {
            String tempStr = latLngStr[i];
            String[] latAndLng = tempStr.split(",");

            for ( int j = 0; j < latAndLng.length; j++ ) {

                if ( j % 2 == 0 ) {
                    lat.add( Double.valueOf( latAndLng[j] ) );
                } else {
                    lng.add( Double.valueOf( latAndLng[j] ) );
                }
            }
        }
    }
}