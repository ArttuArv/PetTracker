package pet.tracker.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Set;


public class LogsFragment extends Fragment {

    public DatabaseData databaseData = DatabaseData.getInstance();
    ListView list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        Intent selectedTrack = new Intent(getActivity(), RouteTrackerFragment.class);

        list = view.findViewById(R.id.logsList);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int s : databaseData.courseIdArray) {
            arrayList.add(" Lenkki " + s);
            System.out.println(s);
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt("selectedCourseID",position); // Put anything what you want

                System.out.println("Onko tamakin Nolla?" + position);

                RouteTrackerFragment routeTrackerFragment = new RouteTrackerFragment();
                routeTrackerFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, routeTrackerFragment, "")
                        .addToBackStack(null)
                        .commit();

            }
        });


        return view;
    }


}

