package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainPageActivity extends AppCompatActivity {


    /**********************************************************************************
     *                                                                                *
     * TÄÄ ON TESTIACTIVITY ETUSIVUSTA, JONNE LOGIN-TOIMINTA ONNISTUESSAAN PÄÄTTYY    *
     *                                                                                *
     **********************************************************************************/

    private final int ID_HOME = 1;
    private final int ID_LOCATION = 2;
    private final int ID_LOG = 3;
    private final int ID_PROFILE = 4;

    BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();
    Intent enableBtIntent;
    int REQUEST_ENABLE_BT = 1;
    private boolean btCreated = false;

    private Profile1Fragment p1Fragment;
    private final String SIMPLE_FRAGMENT_TAG = "profiili1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
/*
        if ( savedInstanceState != null ) {
            System.out.println( "**************************");
            System.out.println( "fragmentti on jo olemassa!");
            System.out.println( "**************************");
            p1Fragment = (Profile1Fragment) getSupportFragmentManager().findFragmentByTag( SIMPLE_FRAGMENT_TAG );
        } else if ( p1Fragment == null ) {
            System.out.println( "**************************");
            System.out.println( "Luodaan uusi fragmentti!!!");
            System.out.println( "**************************");
            p1Fragment = new Profile1Fragment();
        }
*/
        PetlocationFragment PlFragment = new PetlocationFragment();

        //BT Stuff
        enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        // Upea alapalkki stuff
        MeowBottomNavigation bottomNavigation = findViewById( R.id.bottomNavigation );

        bottomNavigation.add( new MeowBottomNavigation.Model( ID_HOME, R.drawable.ic_baseline_home_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_LOCATION, R.drawable.ic_baseline_location_searching_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_PROFILE, R.drawable.ic_baseline_person_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_LOG, R.drawable.ic_baseline_menu_book_24 ) );

        // Mitä alapalkki tekee kun sitä hiplaa
        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                Toast.makeText( MainPageActivity.this, "Clicked item : " + item.getId(), Toast.LENGTH_SHORT ).show();
            }
        });
        // Mitä alapalkki tekee kun hiplaat sitä toistamiseen
        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
           @Override
           public void onReselectItem(MeowBottomNavigation.Model item) {
               bottomNavigation.show( item.getId(), false );
           }
        });

        // Mitä alapalkki tekee kun sitä on hiplattu
        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {

                switch (item.getId()) {
                    case ID_HOME:
                        replace( new MainePageFragment() );
                        break;

                    case ID_LOCATION:
                        if(!btCreated) {
                            if (BtAdapter == null) {
                                Toast.makeText(MainPageActivity.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();

                            } else {
                                if (!BtAdapter.isEnabled()) {
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                }
                            }
                            if (BtAdapter.isEnabled()) {
                                Toast.makeText(MainPageActivity.this, "Connecting to PetTracker Receiver", Toast.LENGTH_SHORT).show();
                                BtConnection btConnection = new BtConnection();
                                btConnection.bluetoothOn();
                            }
                        }
                        btCreated = true;
                        replace( PlFragment );

                        break;

                    case ID_PROFILE:
                        replace( new Profile1Fragment() );
                        break;

                    case ID_LOG:
                        // Väliaikaisesti tästä kartanpiirtelysivulle
                        //replace( new LogsFragment() );
                        replace( new RouteTrackerFragment() );
                        break;

                    default:
                        //do nothing
                }
            }
        });
        bottomNavigation.show( ID_HOME, true );
    }


    private void replace( Fragment fragment ) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace( R.id.fragment, fragment );
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainPageActivity.this, "BT Connection enabled", Toast.LENGTH_SHORT).show();
                BtConnection btConnection = new BtConnection();
                btConnection.bluetoothOn();
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

}