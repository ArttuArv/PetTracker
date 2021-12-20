package pet.tracker.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LogsFragment extends Fragment {

    ExecutorService executors = Executors.newSingleThreadExecutor();
    ConnectionsHelper connHelper;

    ArrayList<String> gspValueArr = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        connHelper = new ConnectionsHelper();

        executors.execute(new Runnable() {
            @Override
            public void run() {

                connHelper.connectSSH();
                connHelper.connectToMySql();

                gspValueArr = connHelper.getGPSdata( 40 );
                int koko = gspValueArr.size();
                String[] array = new String[ koko ];

                System.out.println( "Fragmentissa tulostettu ArrayList arvot:" );

                for ( int i = 0; i < gspValueArr.size(); i++ ) {

                    System.out.println( gspValueArr.get(i) );
                    array[i] = gspValueArr.get(i);
                }

                try {
                    JSONObject jsonObject = new JSONObject( array[0] );
                    System.out.println( "JSON SISÄLTÖ: " + jsonObject );
                    System.out.println( Double.parseDouble( jsonObject.getString("lat") ));

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        return view;
    }
}