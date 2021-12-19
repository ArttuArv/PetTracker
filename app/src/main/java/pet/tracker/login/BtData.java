package pet.tracker.login;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class BtData {
    private static JSONObject btJSON = new JSONObject();
    private  static Boolean bl;
    private static LatLng latLng;
    private BtData(){

    }
    private static  BtData mBtData;
    public static BtData getInstance(){
        if (mBtData == null){
            mBtData = new BtData();
        }
        return mBtData;
    }

    public static JSONObject getBtDataJSON()
    {
        return btJSON;

    }

    public void setbtJSON(String string) throws JSONException {
        btJSON = new JSONObject(string);
    }

    public static void setLatLon() throws JSONException {

        latLng= new LatLng(Double.parseDouble(btJSON.getString("lat")),Double.parseDouble(btJSON.getString("lon")));
    }

    public static LatLng getLatLng(){

        return latLng;
    }

    public static void setbtJSON(JSONObject json) {

        btJSON = json;
    }


}
