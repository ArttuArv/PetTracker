package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAccountActivity extends AppCompatActivity {

    private Button createAccount;
    private EditText name, password, verifyPassword;
    private TextView errorTxt;
    private String uName = "";
    private String pWord = "";
    private String verifyPWord = "";
    private boolean pWordIsValid = false;
    private boolean uNameIsValid = false;

    ConnectionsHelper connHelper = new ConnectionsHelper();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        createAccount = findViewById( R.id.btnCreateAcc );
        name = findViewById( R.id.editName );
        password = findViewById( R.id.editPassword );
        verifyPassword = findViewById( R.id.editRetypePassword );
        errorTxt = findViewById( R.id.accCreateError1 );

        createAccount.setEnabled( false );

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verifyPWord = verifyPassword.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //Asiat, jotka tapahtuu tekstin muuttamisen aikana
                uName = name.getText().toString();
                pWord = password.getText().toString();
                createAccount.setEnabled( false );
                pWordIsValid = validatePass( pWord, verifyPWord);

                if ( pWordIsValid && uName.isEmpty() ) {
                    errorTxt.setTextColor( Color.RED );
                    errorTxt.setText( "GIVE USERNAME!" );

                    createAccount.setEnabled( false );
                    name.clearComposingText();
                    password.clearComposingText();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        verifyPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Asiat, jotka tapahtuu ennen tekstin muuttamista
                pWord = password.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Asiat, jotka tapahtuu tekstin muuttamisen aikana
                uName = name.getText().toString();
                verifyPWord = verifyPassword.getText().toString();
                createAccount.setEnabled( false );
                pWordIsValid = validatePass( pWord, verifyPWord);

                if ( pWordIsValid && uName.isEmpty() ) {
                    errorTxt.setTextColor( Color.RED );
                    errorTxt.setText( "GIVE USERNAME!" );

                    createAccount.setEnabled( false );
                    name.clearComposingText();
                    password.clearComposingText();
                }
                else {
                    createAccount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            executor.execute(new Runnable() {
                                @Override
                                public void run() {

                                    connHelper.connectSSH();
                                    connHelper.connectToMySql();
                                    uNameIsValid = connHelper.checkUsernameAvailability( uName );

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if ( !uNameIsValid ) {
                                                name.getText().clear();
                                                password.getText().clear();
                                                verifyPassword.getText().clear();
                                                errorTxt.setTextColor( Color.RED );
                                                errorTxt.setText( "USERNAME ALREADY TAKEN!" );
                                            } else {

                                                executor.execute(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        //Asetetaan käyttäjätauluun käyttäjänimi ja salasana ja suljetaan yhteydet
                                                        connHelper.setLoginData( uName, verifyPWord );
                                                        connHelper.closeConnection();
                                                        connHelper.disconnectSession();

                                                        Intent createAcc2 = new Intent( CreateAccountActivity.this, CreateAccount2Activity.class );
                                                        createAcc2.putExtra( "username", uName );

                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                name.getText().clear();
                                                                password.getText().clear();
                                                                verifyPassword.getText().clear();

                                                                startActivity( createAcc2 );
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public boolean validatePass ( String password, String verifiedPassword ) {

        System.out.println( "Salasana = " + password + " ja verrokki = " + verifiedPassword );

        if( verifiedPassword.equals( password ) ) {
            createAccount.setEnabled( true );
            return true;
        }
        return false;
    }
}