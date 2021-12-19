package pet.tracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private EditText uName, pWord;
    private TextView errorTxt;
    private boolean isValid = false;
    private boolean dataIsValid = false;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById( R.id.btnLogin2 );
        uName = findViewById( R.id.loginName );
        pWord = findViewById( R.id.enterPassword );
        errorTxt = findViewById( R.id.loginError );

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = uName.getText().toString();
                String password = pWord.getText().toString();

                System.out.println( "Username SQLinject result: " + sql_injection_tester( username ) );

                if ( username.isEmpty() || password.isEmpty() ) {

                    uName.getText().clear();
                    pWord.getText().clear();

                    errorTxt.setTextColor( Color.RED );
                    errorTxt.setText( "FIELDS CANNOT BE EMPTY!" );
                } else {

                    ConnectionsHelper connHelper = new ConnectionsHelper();

                    DatabaseData data = DatabaseData.getInstance();

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {

                            if ( !connHelper.connectSSH() ) {

                                System.out.println( "Check you connection!");
                            } else {

                                if ( !connHelper.connectToMySql() ) {

                                    System.out.println( "No SQL-connection ");
                                } else {

                                    String dbPassword = connHelper.checkUsernameGetPassword( username );

                                    System.out.println( "SQL-kyselyn tulos = " + dbPassword );

                                    if ( dbPassword.equals( "Väärä käyttäjätunnus" ) || dbPassword.equals( "Mahdollinen SQL-injektio" )) {
                                        uName.getText().clear();
                                        pWord.getText().clear();
                                        errorTxt.setTextColor( Color.RED );
                                        errorTxt.setText( "WRONG USERNAME!" );
                                    } else {

                                        isValid = validate( password, dbPassword );
                                        System.out.println( "isValid: " + isValid );

                                        if ( !isValid ) {

                                            uName.getText().clear();
                                            pWord.getText().clear();

                                            errorTxt.setTextColor( Color.RED );
                                            errorTxt.setText( "WRONG PASSWORD!" );

                                        } else {

                                            System.out.println( "DataIsValid = " + dataIsValid );

                                            dataIsValid = connHelper.getAllData( username );
                                            connHelper.closeConnection();
                                            connHelper.disconnectSession();

                                            System.out.println( "DataIsValid = " + dataIsValid );

                                            if ( !dataIsValid ) {
                                                System.out.println( "Something profound went wrong!");
                                            } else {

                                                Intent intentMainPage = new Intent( LoginActivity.this, MainPageActivity.class );
                                                startActivity( intentMainPage );
                                                finish();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean validate ( String givenPword, String dbPword ) {

        BCrypt.Result result = BCrypt.verifyer().verify( givenPword.toCharArray(), dbPword );

        if ( result.verified ) {
            System.out.println("Oikea salasana");
            return true;
        }
        return false;
    }

    private static boolean sql_injection_tester( String input ) {
        String inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|drop|;|or|-|+|,|AND|EXEC|INSERT|SELECT|DELETE|UPDATE|COUNT|CHR|MID|MASTER|TRUNCATE|CHAR|DECLARE|DROP|OR";

        // Regex to check valid inputs and to prevent SQL injections
        String regex = "^[A-Za-z]\\w{5,29}$";

        // Compile the ReGex
        Pattern pattern = Pattern.compile( regex );

        // If the input is empty
        if ( input.isEmpty() ) {
            return false;
        }

        Matcher matcher = pattern.matcher( input );

        if ( matcher.matches() ) {

            System.out.println( "Matcher result = " + matcher.matches() );

            //You can add things here yourself
            String[] inj_stra = inj_str.split("\\|");

            System.out.println( "Testing for fishy words..");
            for (int i=0 ; i< inj_stra.length ; i++ ) {

                if ( input.contains( inj_stra[i] ) ) {
                    System.out.println( "SQL-injektion mahdollisuus!");
                    return false;
                }
            }
        } else
        {
            System.out.println( "Matcher result = " + matcher.matches() );
            return false;
        }

        return true;
    }

}