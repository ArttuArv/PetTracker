package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.w3c.dom.Text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAccount2Activity extends AppCompatActivity {

    private Button btnDone, btnCancel;
    private EditText input_fName_lName, inputAddress, inputPhonenumber;
    private TextView errorTxt;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account2);

        btnDone = findViewById( R.id.btnAccCreationDone );
        btnCancel = findViewById( R.id.btnAccCreationCancel );
        input_fName_lName = findViewById( R.id.editWholeName );
        inputAddress = findViewById( R.id.editAddress );
        inputPhonenumber = findViewById( R.id.editPhonenumber );
        errorTxt = findViewById( R.id.accCreateError2 );

        ConnectionsHelper connHelper = new ConnectionsHelper();

        String loginUName = getIntent().getStringExtra( "username" );
        System.out.println( "LOGIN USERNAME = " + loginUName );

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = input_fName_lName.getText().toString();
                String address = inputAddress.getText().toString();
                String phonenumber = inputPhonenumber.getText().toString();

                if ( name.isEmpty() || address.isEmpty() || phonenumber.isEmpty() ) {
                    errorTxt.setTextColor( Color.RED );
                    errorTxt.setText( "FIELDS CANNOT BE EMPTY!");
                } else {

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            connHelper.connectSSH();
                            connHelper.connectToMySql();
                            connHelper.setOwnerData( loginUName, name, address, phonenumber);
                            connHelper.closeConnection();
                            connHelper.disconnectSession();

                            Intent backToLogin = new Intent( CreateAccount2Activity.this, StartScreenActivity.class );
                            startActivity( backToLogin );
                            finish();
                        }
                    });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        connHelper.connectSSH();
                        connHelper.connectToMySql();
                        connHelper.deleteUserFromLogin( loginUName );
                        connHelper.closeConnection();
                        connHelper.disconnectSession();

                        Intent backToLogin = new Intent( CreateAccount2Activity.this, StartScreenActivity.class );
                        startActivity( backToLogin );
                        finish();
                    }
                });
            }
        });
    }
}