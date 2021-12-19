package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class StartScreenActivity extends AppCompatActivity {

    ImageView loginImage;
    Button login, createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        loginImage = findViewById( R.id.loginImage );
        login = findViewById( R.id.btnLogin );
        createAccount = findViewById( R.id.btnCreateAccount );

        loginImage.setImageResource( R.drawable.bluedog );

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentLogin = new Intent(StartScreenActivity.this, LoginActivity.class );
                startActivity( intentLogin );
                finish();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentCreateAccount = new Intent( StartScreenActivity.this, CreateAccountActivity.class );
                startActivity( intentCreateAccount );
                finish();
            }
        });
    }
}