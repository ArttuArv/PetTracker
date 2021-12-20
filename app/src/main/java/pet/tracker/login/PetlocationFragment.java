package pet.tracker.login;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PetlocationFragment extends Fragment {

    static final int STATE_NEW_LOCATION = 1;
    TextView message, status;
    ImageView btStatus;
    Button btConnect;
    ImageButton CenterToDogBtn;
    public static GoogleMap mMap;
    ListView btList;
    public static boolean followDoggo;
    public static Marker txMarker;
    public static CameraPosition cameraPosition;
    static int cameraZoom = 6;

    //Yritetään tallentaa fragmentin tila, jos se tuhoutuu kesken käytön
    private Bundle savedState = null;

    ConnectionsHelper connHelper = new ConnectionsHelper();


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;

            //rxMarker = map.addMarker(new MarkerOptions().position(new LatLng(65.0593425+0.5, 25.4653385))
            //.title("Receiver location"));
            txMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(65.0593425, 25.4653385))
                    .title(DatabaseData.getInstance().getPetName()));
            txMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dogface));

            cameraPosition = new CameraPosition.Builder().target(new LatLng(65.0593425, 25.4653385)).zoom( cameraZoom ).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int reason) {

                    switch (reason) {
                        case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                            cameraZoom = (int) mMap.getCameraPosition().zoom;
                            followDoggo = false;
                            break;
                    }
                }
            });
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState ) {

        View view = inflater.inflate(R.layout.activity_petlocation, container, false);
        btList = view.findViewById(R.id.btList);
        message = view.findViewById(R.id.message);
        status = view.findViewById(R.id.status);
        btStatus = view.findViewById(R.id.btStatus);
        CenterToDogBtn = view.findViewById(R.id.CenterToDogBtn);
        btConnect = view.findViewById(R.id.btConnect);



        btConnect.setText("Start Activity");
        followDoggo = true;

        enableBt();

        return view;
    }

    public void enableBt() {

        CenterToDogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followDoggo = true;
                cameraPosition = new CameraPosition.Builder().target(new LatLng(txMarker.getPosition().latitude, txMarker.getPosition().longitude)).zoom(18).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToDatabase.start();
                btConnect.setText("Stop Activity");
            }
        });


    }

    public static Handler UpdateLocationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STATE_NEW_LOCATION:
                    cameraZoom = 18;
                    txMarker.setPosition(BtData.getLatLng());
                    cameraPosition = new CameraPosition.Builder().target(new LatLng(txMarker.getPosition().latitude, txMarker.getPosition().longitude)).zoom(cameraZoom).build();
                    if (followDoggo) {
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                    break;
            }
            return true;
        }
    });


    Thread sendToDatabase = new Thread(new Runnable() {
        @Override
        public void run() {
            connHelper.connectSSH();
            connHelper.connectToMySql();
        }
    });
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}