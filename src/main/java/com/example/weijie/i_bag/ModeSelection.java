package com.example.weijie.i_bag;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class ModeSelection extends AppCompatActivity {

    String address, deviceName;
    Switch unlockSwitch, atmSwitch, findMeSwitch;
    TextView lockText;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private static final String TAG = "mode_selection_activity";

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);
        getSupportActionBar().setTitle("Mode Selection");

        getIntentFromMainActivity();
        widgetInit();

        new ConnectBT().execute();

        checkBTState();
        unlockSwitchListener();
        atmSwitchListener();
        findMeSwitchListener();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onStop(){
        super.onStop();
        try {
            btSocket.close();
        }catch(IOException e){

        }
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
                msg("Disconnected from " + deviceName );
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();
        Intent intentMainActivity = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intentMainActivity);
    }

    private void unlockLuggage()
    {
        if (btSocket!=null)
        {
            sendData("2");
        }
    }

    private void lockLuggage()
    {
        if (btSocket!=null)
        {
            sendData("1");
        }
    }

    private void checkBTState(){
        IntentFilter filterConnected = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filterDisconnectRequest = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filterDisconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(BTReceiver, filterConnected);
        this.registerReceiver(BTReceiver, filterDisconnectRequest);
        this.registerReceiver(BTReceiver, filterDisconnected);
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect) {
            Disconnect();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getIntentFromMainActivity(){
        Intent intent = getIntent();

        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        deviceName = intent.getStringExtra(MainActivity.DEVICE_NAME);
    }

    private void widgetInit(){
        findMeSwitch = (Switch) findViewById(R.id.switch_find);
        unlockSwitch = (Switch) findViewById(R.id.unlock_switch);
        atmSwitch = (Switch) findViewById(R.id.atm_switch);
        lockText = (TextView) findViewById(R.id.lock_text);
    }

    private String sendData(String message){
        byte[] msgBuffer = message.getBytes();
        Log.d(TAG,"Send data: " + message + "...");
        try{
            btSocket.getOutputStream().write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String RECEIVED_DATA = receiveData();
        return RECEIVED_DATA;
    }

    private String receiveData(){
        char c;
        int inComingBytes;
        String receivedString = "";

        try{
            InputStream bytes = btSocket.getInputStream();
            InputStreamReader inputStreamreader = new InputStreamReader(bytes);

            try {
                while((inComingBytes = inputStreamreader.read())!= 35) {
                    c = (char)inComingBytes;
                    receivedString += c;
                }
                Log.d(TAG,"Reply: " + receivedString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return receivedString;
    }

    private void theftModeOn(){
        if (btSocket!=null)
        {
            sendData("3");
        }
    }

    private void theftModeOff(){
        if (btSocket!=null)
        {
            sendData("4");
        }
    }

    private void buzzerOn(){
        sendData("5");
    }

    private void buzzerOff(){
        sendData("6");
    }

    private void atmSwitchListener(){
        atmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "state = " + isChecked);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ModeSelection.this);

                    alertDialogBuilder.setTitle("Anti-Theft Mode");
                    alertDialogBuilder
                            .setMessage("Turn on Anti-Theft Mode will give an unpleasant sound to alert you whenever the luggage is moved! ")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(getApplicationContext(), "Anti-Theft Mode activated", Toast.LENGTH_SHORT).show();
                                    theftModeOn();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    atmSwitch.setChecked(false);
                                    theftModeOff();
                                    Toast.makeText(getApplicationContext(), "Aborted", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    sendData("4");
                    Log.d(TAG, "state = " + isChecked);
                }
            }
        });
    }

    private void unlockSwitchListener(){
        unlockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "state = " + isChecked);
                    lockLuggage();
                    lockText.setText("Locked");
                } else {
                    Log.d(TAG, "state = " + isChecked);
                    unlockLuggage();
                    lockText.setText("Unlocked");
                }
            }
        });
    }

    private void findMeSwitchListener() {
        findMeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    buzzerOn();
                }else{
                    buzzerOff();
                }
            }
        });
    }

    private void synWithDevice(){
        gpsUpdateFrequency();
        smsNotificationUpdate();
    }

    private void gpsUpdateFrequency(){
        int frequencyType = reloadPrefInt("freqTypePref","freqTypeInt");
        switch (frequencyType) {
            case 0:
                sendData("7");
                Log.d("Reply","Update every 1 minute");
                break;
            case 1:
                sendData("8");
                Log.d("Reply","Update every 15 minute");
                break;
            case 2:
                sendData("9");
                Log.d("Reply","Update every 30 minute");
                break;
        }
    }

    private void smsNotificationUpdate(){
        boolean smsNotificationState = reloadPrefBool("smsPref","smsPref");
        if(smsNotificationState){
            sendData("a");
        }else{
            sendData("b");
        }
    }

    private int reloadPrefInt(String fileName, String keyName){
        SharedPreferences mypref = getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE);
        int value = mypref.getInt(keyName,0);
        return value;
    }

    private boolean reloadPrefBool(String fileName, String keyName){
        SharedPreferences mypref = getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE);
        boolean value = mypref.getBoolean(keyName,false);
        return value;
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            String str_connection = "Connecting to " + deviceName;
            progress = ProgressDialog.show(ModeSelection.this, str_connection, "Please wait...");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    myBluetooth.cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            synWithDevice();

            if (!ConnectSuccess)
            {
                msg("Connection to " + deviceName + " Failed. Make sure the device is discoverable. Try again.");
                finish();
            }
            else
            {
                msg("Connected to " + deviceName);
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                AlertDialog.Builder builder = new AlertDialog.Builder(ModeSelection.this);
                builder.setMessage("BLUETOOTH DISCONNECTED")
                        .setCancelable(false)
                        .setPositiveButton("RECONNECT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                startActivity(getIntent());
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    };
}