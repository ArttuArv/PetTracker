package pet.tracker.login;   // Muuttakaa tarvittaessa tämä


import android.annotation.SuppressLint;
import android.os.StrictMode;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import at.favre.lib.crypto.bcrypt.BCrypt;

/***************************************************************************************************
 *
 *    Täs on appin build.gradleen implementaationit:
 *
 *    implementation group: 'at.favre.lib', name: 'bcrypt', version: '0.9.0'
 *    implementation 'com.jcraft:jsch:0.1.54'
 *    implementation 'org.mariadb.jdbc:mariadb-java-client:1.8.0'
 *
 *    Tää pitää sisällään tunnistetietoja servereillä.
 *    Pitää myös sisällään valmiit keinot tulla servereille sisään.
 *    Jättäkää tämä tiedosto gitHubin ulkopuolelle, kun laitatte projektitiedostoja gitHubiin.
 *
 ***************************************************************************************************/

public class ConnectionsHelper {

    private static final String sshUser = "pettracker";
    private static final String sshPword = "pettracker1234";
    private static final String hostname = "84.248.10.238";
    private static final int sshPort = 384;
    private int assignedPort = 0;

    private static final String dbUname = "pettracker";
    private static final String dbPassword = "pettracker1234";
    private static final String dbName = "pettrackkerdb";
    private static final String dbHost = "127.0.0.1";
    private static final String dbPort = "3306";
    private String dbConnUrl = "";

    private Session session;
    private Connection connection;

    //Tässä on Singleton-tyyppinen luokka tiedonkeruuta varten
    DatabaseData dataStash = DatabaseData.getInstance();


    java.sql.Timestamp locationUpdateTime;

    //Parametritön konstruktori
    public ConnectionsHelper() {}

    //SSH-connection and port forwarding
    protected boolean connectSSH() {

        try {
            final JSch jsch = new JSch();
            session = jsch.getSession( sshUser, hostname, sshPort );
            session.setPassword( sshPword );

            final Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            System.out.println("Establishing connection...");
            session.connect();
            System.out.println("Connection established!");
            System.out.println("Doing some portforwarding...");
            assignedPort = session.setPortForwardingL( 0, dbHost, Integer.parseInt(dbPort) );
            System.out.println("Host: " + assignedPort + " -> " + dbHost + ":" + dbPort );
            System.out.println("Port Forwarded!");

            return true;

        } catch ( JSchException je ) {
            System.out.println( "JSCH ERROR: " + je.getMessage() );
            je.printStackTrace();
        }

        return false;
    }

    //SQL-connection to a MariaDB database in Raspberry Pi
    @SuppressLint("NewApi")
    protected boolean connectToMySql() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy( policy );
        String dbDriver = "org.mariadb.jdbc.Driver";

        try {
            Class.forName( dbDriver );
            dbConnUrl = "jdbc:mariadb://" + dbHost + ":" + assignedPort + "/" + dbName;
            System.out.println( "Connecting to selected database..." );
            connection = DriverManager.getConnection( dbConnUrl, dbUname, dbPassword );
            System.out.println( "Connected to database succesfully!" );

            return true;
        } catch (SQLException se ) {
            System.out.println( "Error connection to SQLserver: " + se.getMessage() );
            return false;
        } catch( Exception e ) {
            System.out.println( "Error in connectToMySql function: " + e.getMessage() );
            return false;
        }
    }

    protected StringBuilder showOwner() {
        Statement statement;
        ResultSet result;
        StringBuilder queryResult = new StringBuilder();

        System.out.println("Gathering database data to a string..");

        try {
            statement = connection.createStatement();
            result = statement.executeQuery( "SELECT * FROM owner");
            result.first();

            if ( !result.first() ) {
                System.out.println( "No data in the table");
                return queryResult.append( "No data in the table!" );
            } else {

                while (!result.isAfterLast()) {
                    int id = result.getInt("idowner");
                    int idLogin = result.getInt("login_idlogin");
                    String firstname = result.getString("firstname");
                    String lastname = result.getString("lastname");
                    String address = result.getString("address");
                    String phonenumber = result.getString("phone");

                    queryResult.append( id ).append( " " ).append( idLogin ).append( " " ).append( firstname ).append( " " )
                            .append( lastname ).append( " " ).append( address ).append( " " ).append( phonenumber ).append( "\n" );
                    result.next();
                }

                System.out.println("Data collected succesfully!");
                connection.close();
                session.disconnect();
                return queryResult;
            }

        } catch ( Exception ex ) {
            System.out.println( "Error during access to owner: " + ex.getMessage());
            return null;
        }
    }

    //Metodi hakee tietokannan login-taulusta vastaavuutta käyttäjätunnukselle ja palauttaa
    //sieltä joko tunnusta vastaavan salasanan tai vastauksen, ettei käyttäjätunnusta ole olemassa.
    protected String checkUsernameGetPassword( String uName ) {

        ResultSet result;
        String sqlResult;
        //Sql-kyselymerkkijono
        String sqlQuery = "SELECT password FROM login WHERE username = ?";

        //Debuggaustulostuksia
        System.out.println( "SQL-kysely = " + sqlQuery );

        try {
            // sql-api-kirjaston muuttujien alustuksia
            PreparedStatement p = connection.prepareStatement( sqlQuery );
            p.setString(1, uName);
            result = p.executeQuery();

            System.out.println("Gathering data to a string..");
            // Eka if palauttaa tyhjän rivin tietokannasta, jos annettua käyttäjätunnusta ei ole tietokannassa
            if ( !result.first() ) {
                System.out.println( "No data in the table");
                sqlResult = "Väärä käyttäjätunnus";
                return sqlResult;
            } else {
                // Else-haarassa palautuksessa on käyttäjätunnusta vastaava salasana, joka palautetaan pääohjelmaan
                return result.getString( "password" );
            }
        } catch ( SQLException se ) {
            // Kaikki sql-syntaksiin liittyvät virheet tulevat catchin kautta
            System.out.println( "Error during access to login: " + se.getMessage());
            return "Mahdollinen SQL-injektio";
        }
    }

    //Tarkastaa käyttäjätunnuksen olemassaolon
    protected boolean checkUsernameAvailability( String uName ) {
        ResultSet result;

        //Sql-kysely-merkkkijono
        String query = "SELECT username FROM login WHERE username='" + uName + "'";
        String sqlQuery = "SELECT username FROM login WHERE username = ?";

        //Tulostus debuggaamista varten
        System.out.println( "Given uName = " + uName + " SQL query = " + sqlQuery );

        try {
            // Luodaan sql-kirjaston mukaiset alustukset, joiden sielunelämästä mulla ei ole
            // edes hirveästi vielä tietoa
            PreparedStatement p = connection.prepareStatement( sqlQuery );
            p.setString(1, uName );
            result = p.executeQuery();

            // Eka if katsoo tuleeko tietokannasta tyhjä rivi kertoen, että käyttäjänimeä ei ole olemassa
            // jolloin käyttäjänimi on vapaata riistaa
            if ( !result.first() ) {
                // Debuggaustulostuksia
                System.out.println( "Result.first() = " + result.first() );
                System.out.println( "Username free to use");
                return true;
                // Jos tietokannasta tulee annettua käyttäjätunnusta vastaava käyttäjätunnus, funktio
                // palauttaa falsen kertoen, että käyttäjänimi on käytössä
            } else {
                // Debuggaustulostuksia
                System.out.println( "Result.first() = " + result.first() );
                System.out.println( "Username taken!");
                return false;

            }
        } catch ( SQLException se ) {
            // catch tulostaa mahdolliset virheet sql-kyselysyntaksissa
            System.out.println( "Error during access to login: " + se.getMessage());
            return false;
        }
    }

    //Metodi laittaa login-tauluun käyttäjätunnuksen ja salatun salasanan
    protected void setLoginData( String username, String password ) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray() );
        String sqlQuery = "INSERT INTO login(username, password) VALUES(?,?)";

        try {
            PreparedStatement p = connection.prepareStatement( sqlQuery );
            p.setString(1, username );
            p.setString(2, hashedPassword );
            int i = p.executeUpdate();

            if( i != 0) {
                System.out.println( "Update succesful!");
            } else {
                System.out.println( "Update failed!" );
            }

        } catch ( SQLException se ) {
            System.out.println( "Error during inserting data to login: " + se.getMessage() );
        }
    }

    //Asettaa käyttäjän henkilötietoja tietokantaan
    protected void setOwnerData( String loginName, String fname_lname, String address, String phonenumber ) {
        PreparedStatement p;
        ResultSet result;
        int idLogin = 0;
        String sqlLogin = "SELECT idlogin FROM login WHERE username = ?";
        String sqlOwner = "INSERT INTO owner(login_idlogin, name, address, phone) VALUES(?, ?, ?, ?)";

        //Hetaan login-taulusta idlogin-arvo owner-taulua varten
        try {
            p = connection.prepareStatement( sqlLogin );
            p.setString(1, loginName );
            result = p.executeQuery();

            if ( !result.first() ) {
                System.out.println( "No data in the table");
            } else {
                idLogin = result.getInt( "idlogin" );
                System.out.println( "idlogin = " + idLogin );
            }
        } catch ( SQLException se ) {
            System.out.println( "Error during access to login: " + se.getMessage() );
        }

        //Syötetään uuden käyttäjän tiedot owner-tauluun
        try {
            //Viedään kysely tietokantaan
            p = connection.prepareStatement( sqlOwner );
            p.setInt(1, idLogin );
            p.setString(2, fname_lname );
            p.setString(3, address );
            p.setString(4, phonenumber );
            int i = p.executeUpdate();

            if ( i != 0 ) {
                System.out.println( "Owner table succesfully updated!" );
            }

        } catch ( SQLException se ) {
            System.out.println( "Error during access to owner: " + se.getMessage() );
            se.printStackTrace();
        }
    }

    protected boolean startWalk(){
        ResultSet result;
        PreparedStatement p;
        String sqlQuery = "INSERT INTO course(pets_idpets, gpsvalues, speed_distance) VALUES(?, ?, ?)";
        try {
            p = connection.prepareStatement( sqlQuery );
            p.setInt(1, dataStash.getPetID() );
            p.setString( 2, "Uusi lenkki" );
            p.setString( 3, null );
            //Viedään kysely tietokantaan
            int i = p.executeUpdate();

            if ( i > 0 ) {
                try {
                    sqlQuery = "SELECT LAST_INSERT_ID() FROM course WHERE pets_idpets = ?";
                    p = connection.prepareStatement(sqlQuery);
                    p.setInt(1,dataStash.getPetID());
                    result = p.executeQuery();
                    if(!result.first()){
                        System.out.println("No last Walk found from course table");
                        return false;
                    }
                    else {
                        dataStash.setLatestCourseID(result.getInt("LAST_INSERT_ID()"));
                        System.out.println( "Course table succesfully updated!" );
                        return true;
                    }

                } catch ( SQLException se ) {
                    return false;
                }
            } else {
                System.out.println( "Creating a new course failed" );
                return false;
            }

        } catch ( SQLException se ) {
            System.out.println("Error inserting data to course table: " + se.getMessage());
            return false;
        }
    }

    //Tuhoaa käyttäjätunnuksen tietokannasta
    protected void deleteUserFromLogin( String username ) {

        PreparedStatement p;
        String sqlQuery = "DELETE FROM login WHERE username = ?";

        System.out.println( "Username to be deleted = " + username );

        try {
            p = connection.prepareStatement( sqlQuery );
            p.setString(1, username );
            int i = p.executeUpdate();

            if ( i > 0 ) {
                System.out.println( "Username deleted from the login table!" );
            } else {
                System.out.println( "Username was not deleted from the login table");
            }

        } catch ( SQLException se ) {
            System.out.println( "Error deleting data from login: " + se.getMessage());
        }
    }

    //Tallenetaan gps ja muut tiedot tietokantaan
    protected boolean setCourseData() {
        PreparedStatement p;
        String sqlQuery = "INSERT INTO gsp(course_idcourse, latlongspeed, time) VALUES(?, ?, ?)";

        //Syötetään course-tauluun sitä daddelia
        try {
            locationUpdateTime = new java.sql.Timestamp(new java.util.Date().getTime());
            p = connection.prepareStatement( sqlQuery );
            p.setInt(1, dataStash.getLatestCourseID() );
            p.setString( 2, BtData.getBtDataJSON().toString() );
            p.setTimestamp(3,locationUpdateTime);

            //Viedään kysely tietokantaan
            int i = p.executeUpdate();

            if ( i > 0 ) {
                System.out.println( "New gsp data updated!" );
                return true;
            } else {
                System.out.println( "Updating gsp table failed!" );
                return false;
            }

        } catch ( SQLException se ) {

            System.out.println( "Error inserting data to gsp table: " + se.getMessage() );
            return false;
        }

    }

    //Haetaan kaikki käyttäjään liittyvä tieto tietokannasta ja tallennetaan singletoniin
    protected boolean getAllData( String username ) {
        PreparedStatement p;
        ResultSet result;
        int courseArraySize = 0;

        //SQL-kyselyt eri tauluihin
        String sqlGetLogin = "SELECT idlogin FROM login WHERE username = ?";
        String sqlGetOwner = "SELECT * FROM owner WHERE login_idlogin = ?";
        String sqlGetPets = "SELECT * FROM pets WHERE owner_idowner = ? AND idpets = ?";
        String sqlGetCourse = "SELECT * FROM course WHERE pets_idpets = ?";
        String sqlGetAllCourses = "SELECT COUNT(idcourse) FROM course WHERE pets_idpets = ?";

        System.out.println( "Gathering all data related to username " + username );

        //Haetaan ensin idlogin käyttäjän syöttämän käyttäjätunnuksen avulla
        try {
            p = connection.prepareStatement( sqlGetLogin );
            p.setString(1, username );
            result = p.executeQuery();

            System.out.println( "Gathering data from the login table..." );

            if ( !result.first() ) {
                System.out.println( "No data in the table");
            } else {

                dataStash.setLoginID( result.getInt( "idLogin") );
                System.out.println( "Login table done!");
            }

        } catch ( SQLException e ) {
            System.out.println( "Error accessing login data: " + e.getMessage() );
            return false;
        }

        //Haetaan owner-taulun tiedot juuri haetun idloginin avulla
        try {
            p = connection.prepareStatement( sqlGetOwner );
            p.setInt(1, dataStash.getLoginID() );
            result = p.executeQuery();

            System.out.println( "Gathering data from owner table..." );

            if ( !result.first() ) {
                System.out.println( "No data in the table");
            } else {
                //Jos tulee rivejä, niin tallennetaan ne singletonin paremetreiksi
                dataStash.setUserID( result.getInt( "idowner" ) );
                dataStash.setUserName( result.getString( "name" ) );
                dataStash.setUserAddress( result.getString( "address" ) );
                dataStash.setUserPhonenumber( result.getString( "phone" ) );

                System.out.println( "Owner table done!");
            }
        } catch ( SQLException e ) {
            System.out.println( "Error accessing owner table: " + e.getMessage() );
            return false;
        }

        //Haetaan lemmikin tiedot
        try {
            /*********************************************************************
             *                                                                   *
             *  Haen varmuuden vuoksi vain yhden koiran tiedot. Jos halutaan     *
             *  kaikkien koirien tiedot, niin pitää keksiä joku taulukko- tai    *
             *  lista-tyyppinen ratkaisu tiedon tallentamiseen singletoniin      *
             *                                                                   *
             *********************************************************************/

            p = connection.prepareStatement( sqlGetPets );
            p.setInt(1, dataStash.getUserID() );
            p.setInt(2,1 );
            result = p.executeQuery();

            System.out.println( "Gathering data from pets table...");

            if ( !result.first() ) {
                System.out.println( "No data in the table");
            } else {
                //Jos tulee rivejä, niin tallennetaan ne singletonin paremetreiksi
                dataStash.setPetID( result.getInt( "idpets" ) );
                dataStash.setPetName( result.getString( "name" ) );
                dataStash.setPetBreed( result.getString( "breed" ) );

                System.out.println( "Pets table done!" );
            }
        } catch ( SQLException e ) {
            System.out.println( "Error accessing pets table: " + e.getMessage() );
            return false;
        }

        //Haetaan kaikki lenkkeihin liittyvä tieto
        try {
            p = connection.prepareStatement(sqlGetAllCourses);
            p.setInt( 1, dataStash.getPetID() );
            result = p.executeQuery();


            if ( !result.first() ) {
                System.out.println( "No data in the table");
                return true;
            } else {
                dataStash.setAllCourseIDsize(courseArraySize = result.getInt("COUNT(idcourse)"));
                System.out.println("All Walks gathered");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            p = connection.prepareStatement( sqlGetCourse );
            p.setInt( 1, dataStash.getPetID() );
            result = p.executeQuery();

            System.out.println( "Gathering data from course table..." );

            if ( !result.first() ) {
                System.out.println( "No data in the table");
                return true;
            } else {
                int kierros = 0;
                while (!result.isAfterLast()){
                    dataStash.setAllCourseID(kierros, result.getInt("idcourse"));
                    result.next();
                }
                //Jos tulee rivejä, niin tallennetaan ne singletonin paremetreiksi
                //dataStash.setCourseID( result.getInt( "idcourse" ) );
                // dataStash.setGpsValues( result.getString( "gpsvalues" ) );
                //dataStash.setSpeed_distance( result.getString( "speed_distance" ) );

                System.out.println( "Course table done!");
                System.out.println( "Data gathered succesfully!" );
                return true;
            }
        } catch ( SQLException e ) {
            System.out.println( "Error accessing course table: " + e.getMessage() );
            return false;
        }
    }

    protected ArrayList getGPSdata( int courseID ) {
        PreparedStatement p;
        ResultSet result;
        ArrayList<String> gpsValues = new ArrayList<>();
        int kierros = 0;
        String sqlGPSdata = "SELECT latlongspeed FROM gsp WHERE course_idcourse = ?";

        //Haetaan Kaikki gpstieto liittyen tiettyyn course-taulun id:hen
        try {
            p = connection.prepareStatement( sqlGPSdata );
            p.setInt(1, courseID );
            result = p.executeQuery();

            if ( !result.first() ) {
                System.out.println( "No data in the table" );
                return null;
            } else {
                while ( !result.isAfterLast() ) {
                    gpsValues.add( result.getString("latlongspeed") );
                    System.out.println("Testitulostus Arraylist sisältä per rivi( "+ kierros + " ) = " + gpsValues.get(kierros) );
                    kierros++;
                    result.next();
                }
                return gpsValues;
            }
        } catch ( SQLException se ) {
            System.out.println( "Error trying to print data from gsp: " + se.getMessage() );
            return null;
        }
    }

    //Sulkee sql-yhteyden
    protected void closeConnection() {
        try {
            connection.close();
            System.out.println( "Connection closed!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //Sulkee SSH-yhteyden
    protected void disconnectSession(){
        session.disconnect();
        System.out.println( "Session disconnected!");
    }
}