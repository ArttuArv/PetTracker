package pet.tracker.login;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainePageFragment extends Fragment {

    TextView mainPageTxtView;
    ImageView dogePicture;

    Handler handler = new Handler(Looper.getMainLooper());
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean image1Changed = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_main_page, container, false );

        dogePicture = view.findViewById( R.id.dogPic );
        dogePicture.setImageResource( R.drawable.dog1 );

        executor.execute(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dogePicture.setBackgroundResource( R.drawable.spin_animation );
                        AnimationDrawable frameAnimation = (AnimationDrawable) dogePicture.getBackground();
                        frameAnimation.start();
                    }
                });
            }
        });


        dogePicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if ( image1Changed ) {
                    dogePicture.setImageResource( R.drawable.dog2 );
                    image1Changed = false;
                } else {
                    dogePicture.setImageResource( R.drawable.dog5 );
                    image1Changed = true;
                }

                return false;
            }
        });

        
        //return iflated view
        return view;
    }
}