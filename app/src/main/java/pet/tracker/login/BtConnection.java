package pet.tracker.login;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class BtConnection extends AppCompatActivity {


    BluetoothDevice[] btDeviceArray;
    String [] strings;
    ClientClass btClientClass;

    BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();

    static final int STATE_NEW_LOCATION = 1;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_RECEIVED = 5;

    private static final UUID MOBILE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    SendReceive sendReceive;

    ConnectionsHelper connHelper = new ConnectionsHelper();
    DatabaseData databaseData = DatabaseData.getInstance();
    public boolean dataSentToDatabase = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void listBtDevices() {
        Set<BluetoothDevice> bt =BtAdapter.getBondedDevices();
        strings = new String[bt.size()];
        btDeviceArray = new BluetoothDevice[bt.size()];
        int index = 0;

        if (bt.size()>0)
        {
            for(BluetoothDevice device:bt) {
                btDeviceArray[index] = device;
                strings[index] = device.getName();
                index++;
            }

            //ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_expandable_list_item_1,strings);
            //btList.setAdapter(arrayAdapter);
        }
    }
    public void bluetoothOn() {
        listBtDevices();
        btClientClass = new ClientClass(btDeviceArray[0]);
        btClientClass.start();

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what){
                case STATE_LISTENING:
                   //status.setText("Ready to receive");
                    break;
                case STATE_CONNECTING:
                   // status.setText("Connecting");
                    break;
                case STATE_CONNECTION_FAILED:
                   // status.setText("Connection lost");
                case STATE_RECEIVED:
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1)
        {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MOBILE_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run ()
        {
            try {
                if (BtAdapter.isEnabled()) {
                    socket.connect();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                }
                else {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
        public void stopBT(){
            if (!BtAdapter.isEnabled()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public class SendReceive extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {

            if (BtAdapter.isEnabled()) {
                byte[] buffer = new byte[52];
                int bytes = 0;
                while (bluetoothSocket.isConnected()) {
                    try {
                        Scanner s = new Scanner(inputStream).useDelimiter("><");
                        if (s.hasNext()) {
                                bytes = inputStream.read(buffer);
                                System.out.println(bytes);
                            String tempMsg= new String(buffer,0,bytes);
                            System.out.println(tempMsg);
                            try {
                                tempMsg = tempMsg.replace("<","");
                                tempMsg = tempMsg.replace(">","");
                                tempMsg = tempMsg.trim();
                                JSONObject btDataJSON = new JSONObject("{" + tempMsg + "}");
                                System.out.println("Bt json " + btDataJSON);
                                if( btDataJSON.has("lat")){
                                    BtData.setbtJSON(btDataJSON);
                                    System.out.println("Täsä se on: " + BtData.getBtDataJSON());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Message ULMessage = Message.obtain();
                            ULMessage.what = STATE_NEW_LOCATION;
                            BtData.setLatLon();
                            PetlocationFragment.UpdateLocationHandler.sendMessage(ULMessage);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }
}
