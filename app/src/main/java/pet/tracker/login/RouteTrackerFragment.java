package pet.tracker.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteTrackerFragment extends Fragment {

    GoogleMap gMap;
    Marker oulu;
    Polyline polyline = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Double> lat = new ArrayList<>();
    List<Double> lng = new ArrayList<>();

    public int selectedCourse = 0;
    int koko = 0;

    ExecutorService executors = Executors.newSingleThreadExecutor();
    Handler handler = new Handler( Looper.getMainLooper() );
    ConnectionsHelper connHelper;

    ArrayList<String> gspValueArr = new ArrayList<>();
    JSONObject jsonObject;
    double lat1, lon1;


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
            LatLng ouluLatLng = new LatLng(65.010036, 25.467453);
            oulu = googleMap.addMarker(new MarkerOptions().position( ouluLatLng ).title("Marker in Oulu"));
            oulu.setIcon( BitmapDescriptorFactory.fromResource( R.drawable.dogface ) );
            //googleMap.addMarker(new MarkerOptions().position( oulu ).title("Marker in Oulu"));
            googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( ouluLatLng, 12 ) );

            gMap = googleMap;

            connHelper = new ConnectionsHelper();

            executors.execute(new Runnable() {
                @Override
                public void run() {

                    connHelper.connectSSH();
                    connHelper.connectToMySql();


                    gspValueArr = connHelper.getGPSdata( selectedCourse );
                    System.out.println("Tassa ont taa sydeemi" + selectedCourse);
                    connHelper.closeConnection();
                    connHelper.disconnectSession();

                    if(gspValueArr != null) {
                        koko = gspValueArr.size();
                        String[] array = new String[koko];

                        System.out.println("Fragmentissa tulostettu ArrayList arvot:");

                        for (int i = 0; i < gspValueArr.size(); i++) {

                            System.out.println(gspValueArr.get(i));
                            array[i] = gspValueArr.get(i);

                            try {

                                jsonObject = new JSONObject(array[i]);
                                System.out.println(Double.parseDouble(jsonObject.getString("lat")));
                                latLngList.add(new LatLng(Double.parseDouble(jsonObject.getString("lat")), Double.parseDouble(jsonObject.getString("lon"))));
                                lat1 = Double.parseDouble(jsonObject.getString("lat"));
                                lon1 = Double.parseDouble(jsonObject.getString("lon"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                                System.out.println("Tuleeko tänne mitää erroria: " + e.getMessage());
                            }
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            System.out.println( "lat: " + lat1 + "\tlon: " + lon1);

                            LatLng oulu1 = new LatLng(  lat1 , lon1 );
                            oulu = googleMap.addMarker(new MarkerOptions().position( oulu1 ).title("Marker in Oulu"));
                            oulu.setIcon( BitmapDescriptorFactory.fromResource( R.drawable.dogface ) );
                            googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( oulu1, 18 ) );
                            /*
                            splitStr2Double( gpsValues );
                            for ( int i = 0; i < lat.size(); i++ ) {

                                latLngList.add( new LatLng( lat.get(i), lng.get(i) ) );
                            } */

                            // Draw Polyline on Map
                            // Polylines are useful to show route or some other connection between points.
                            polyline = gMap.addPolyline( new PolylineOptions().clickable( true ).addAll( latLngList ) );
                            polyline.setWidth( 12 );
                            polyline.setColor( Color.RED );
                            polyline.setJointType( JointType.ROUND );
                        }
                    });
                }
            });

            System.out.println( "lat: " + lat1 + "\tlon: " + lon1);
            /*
            oulu = new LatLng(  lat1 , lon1 );
            googleMap.addMarker(new MarkerOptions().position( oulu ).title("Marker in Oulu"));
            googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom( oulu, 12 ) );
*/
            /*
            splitStr2Double( gpsValues );
            for ( int i = 0; i < lat.size(); i++ ) {

                latLngList.add( new LatLng( lat.get(i), lng.get(i) ) );
            } */

            // Draw Polyline on Map
            // Polylines are useful to show route or some other connection between points.
            polyline = gMap.addPolyline( new PolylineOptions().clickable( true ).addAll( latLngList ) );
            polyline.setWidth( 12 );
            polyline.setColor( Color.RED );
            polyline.setJointType( JointType.ROUND );

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_maps, container, false );

        Bundle bundle = this.getArguments();

        if(bundle != null){
            selectedCourse = bundle.getInt("selectedCourseID");

        }




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