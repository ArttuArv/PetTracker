package pet.tracker.login;

/************************************************************************
 *                                                                      *
 *   Tää on singleton-tyyppinen olio, joka tuhoutuu vasta kun           *
 *   appi tuhoutuu. Tää vois olla hyvä tiedonkeruuastia, niin ei tarvi  *
 *   haeskella ssh:n kautta koko aika                                   *
 *   Alustetaan DatabaseData "joku nimi" = DatabaseData.getInstance();  *
 *   Oliossa on getterit ja setterit, joten tiedonhaku ja -tallennus    *
 *   onnistuu käyttämällä komentoja esim.                               *
 *   "joku nimi".setUserName( "Kari Tapio"); ja                         *
 *   "joku nimi".getUserName();                                         *
 *                                                                      *
 ***********************************************************************/

public class DatabaseData {

    private int loginID;

    private int userID;
    private String userName = "";
    private String userAddress = "";
    private String userPhonenumber = "";

    private int petID;
    private String petName = "";
    private String petBreed = "";

    private int courseID;
    private String gpsValues = "";
    private String speed_distance = "";

    private DatabaseData() {
        //Estetään olion turha kutsuminen.
    }

    private static DatabaseData mDatabaseData;

    public static DatabaseData getInstance() {

        if ( mDatabaseData == null ) {
            mDatabaseData = new DatabaseData();
        }

        return mDatabaseData;
    }

    public int getLoginID() {
        return loginID;
    }

    public void setLoginID( int loginID) {
        this.loginID = loginID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID( int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserPhonenumber() {
        return userPhonenumber;
    }

    public void setUserPhonenumber(String userPhonenumber) {
        this.userPhonenumber = userPhonenumber;
    }

    public int getPetID() {
        return petID;
    }

    public void setPetID(int petID) {
        this.petID = petID;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public void setPetBreed(String petBreed) {
        this.petBreed = petBreed;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID( int courseID) {
        this.courseID = courseID;
    }

    public String getGpsValues() {
        return gpsValues;
    }

    public void setGpsValues(String gpsValues) {
        this.gpsValues = gpsValues;
    }

    public String getSpeed_distance() {
        return speed_distance;
    }

    public void setSpeed_distance(String speed_distance) {
        this.speed_distance = speed_distance;
    }

}
