package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Toast;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

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
    private int kierros = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        MeowBottomNavigation bottomNavigation = findViewById( R.id.bottomNavigation );

        bottomNavigation.add( new MeowBottomNavigation.Model( ID_HOME, R.drawable.ic_baseline_home_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_LOCATION, R.drawable.ic_baseline_location_searching_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_PROFILE, R.drawable.ic_baseline_person_24 ) );
        bottomNavigation.add( new MeowBottomNavigation.Model( ID_LOG, R.drawable.ic_baseline_menu_book_24 ) );

        DatabaseData dataStash = DatabaseData.getInstance();
        Bundle bundle = new Bundle();


        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                Toast.makeText( MainPageActivity.this, "Clicked item : " + item.getId(), Toast.LENGTH_SHORT ).show();
            }
        });

        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
           @Override
           public void onReselectItem(MeowBottomNavigation.Model item) {
               bottomNavigation.show( item.getId(), false );
           }
        });

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {

                switch (item.getId()) {
                    case ID_HOME:
                        replace( new MainePageFragment() );
                        break;

                    case ID_LOCATION:
                        replace( new RouteTrackerFragment() );
                        break;

                    case ID_PROFILE:
                        replace( new Profile1Fragment() );
                        break;

                    case ID_LOG:
                        replace( new LogsFragment() );
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
}