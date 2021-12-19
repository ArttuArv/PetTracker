package pet.tracker.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Profile2Fragment extends Fragment {

    EditText userName, userAddress, userPhonenumber, dogName, dogBreed;
    Button saveAlterations, goBack;
    ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_profile2, container, false );

        userName = view.findViewById( R.id.modifyName );
        userAddress = view.findViewById( R.id.modifyAddress );
        userPhonenumber = view.findViewById( R.id.modifyPhone );
        dogName = view.findViewById( R.id.modifyDogName );
        dogBreed = view.findViewById( R.id.modifyDogBreed );
        saveAlterations = view.findViewById( R.id.btnSaveAlter );
        goBack = view.findViewById( R.id.btnGoBack);

        Fragment profile1 = new Profile1Fragment();
        DatabaseData dataStash = DatabaseData.getInstance();

        saveAlterations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uName = userName.getText().toString();
                String uAddress = userAddress.getText().toString();
                String uPhone = userPhonenumber.getText().toString();
                String pName = dogName.getText().toString();
                String pBreed = dogBreed.getText().toString();

                if ( !uName.isEmpty() ) {
                    dataStash.setUserName( userName.getText().toString() );
                }
                if ( !uAddress.isEmpty() ) {
                    dataStash.setUserAddress( userAddress.getText().toString() );
                }
                if ( !uPhone.isEmpty() ) {
                    dataStash.setUserPhonenumber( userPhonenumber.getText().toString() );
                }
                if ( !pName.isEmpty() ) {
                    dataStash.setPetName(dogName.getText().toString());
                }
                if ( !pBreed.isEmpty() ) {
                    dataStash.setPetBreed( dogBreed.getText().toString() );
                }

                replace( profile1 );
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                replace( profile1 );

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