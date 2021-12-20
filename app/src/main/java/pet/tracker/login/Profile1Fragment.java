package pet.tracker.login;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class Profile1Fragment extends Fragment {

    TextView nimi, osoite, puhelin, nimiKoira, rotuKoira;
    Button alterInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile1, container, false);

        nimi = view.findViewById( R.id.nimi );
        osoite = view.findViewById( R.id.osoite );
        puhelin = view.findViewById( R.id.puhelin );
        nimiKoira = view.findViewById( R.id.koiranNimi );
        rotuKoira = view.findViewById( R.id.rotu );
        alterInfo = view.findViewById( R.id.btnInfo2);

        Fragment profile2 = new Profile2Fragment();
        DatabaseData dataStash = DatabaseData.getInstance();

        System.out.println("Koiradataa: " + dataStash.getPetName() + " " + dataStash.getPetBreed() + " " + dataStash.getPetID() );

        nimi.setText( dataStash.getUserName() );
        osoite.setText( dataStash.getUserAddress() );
        puhelin.setText( dataStash.getUserPhonenumber() );
        nimiKoira.setText( dataStash.getPetName() );
        rotuKoira.setText( dataStash.getPetBreed() );

        alterInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replace( profile2 );
            }
        });

        return view;
    }

    private void replace( Fragment fragment ) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace( R.id.fragment, fragment );
        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
    }
}