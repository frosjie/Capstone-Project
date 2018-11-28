package com.example.weijie.i_bag;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingActivity";
    int lastSpinnerOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setTitle("Settings");

        powerSaveSwitch();
        loginRememberSwitch();
        GPSupdateFrequency();
        smsNotificationSwitch();
    }

    private void powerSaveSwitch() {
        final Switch power_save_switch = (Switch) findViewById(R.id.power_saving_switch);
        power_save_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "state = " + isChecked);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingActivity.this);

                    alertDialogBuilder.setTitle("Power Saving Mode");
                    alertDialogBuilder
                            .setMessage("Are you sure want to turn on? This will switch off the luggage's GPS module!")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(getApplicationContext(), "Power mode is turned on", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    power_save_switch.setChecked(false);
                                    Toast.makeText(getApplicationContext(), "Aborted", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Log.d(TAG, "state = " + isChecked);
                }
            }
        });
    }

    private void loginRememberSwitch() {
        SharedPreferences loginPref = getApplicationContext().getSharedPreferences("loginPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = loginPref.edit();
        Switch login_remember_switch = (Switch) findViewById(R.id.login_remember_switch);


        login_remember_switch.setChecked(loginPref.getBoolean("loginPref", true));

        login_remember_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "state = " + isChecked);
                    editor.putBoolean("loginPref", true).commit();
                } else {
                    Log.d(TAG, "state = " + isChecked);
                    editor.putBoolean("loginPref", false).commit();
                }
            }
        });
    }

    private void smsNotificationSwitch() {
        SharedPreferences loginPref = getApplicationContext().getSharedPreferences("smsPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = loginPref.edit();
        Switch smsNotificationSwitch = (Switch) findViewById(R.id.sms_notification_switch);


        smsNotificationSwitch.setChecked(loginPref.getBoolean("smsPref", false));

        smsNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "state = " + isChecked);
                    editor.putBoolean("smsPref", true).commit();
                } else {
                    Log.d(TAG, "state = " + isChecked);
                    editor.putBoolean("smsPref", false).commit();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        String frequencyType_str;
        int frequencyType = 0;
        frequencyType_str = parent.getItemAtPosition(position).toString();

        switch(frequencyType_str){
            case "1 Minute":
                frequencyType = 0;
                Log.d("Spinner","frequencyType = " + frequencyType);
                break;

            case "15 Minute":
                frequencyType = 1;
                Log.d("Spinner","frequencyType = " + frequencyType);
                break;

            case "30 Minute":
                frequencyType = 2;
                Log.d("Spinner","frequencyType = " + frequencyType);
                break;
        }
        savePrefInt("freqTypePref","freqTypeInt",frequencyType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void savePrefInt(String fileName, String keyName, int valueName){
        SharedPreferences freqTypePref = getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = freqTypePref.edit();
        editor.putInt(keyName, valueName);
        editor.commit();
    }

    private int reloadPrefInt(String fileName, String keyName){
        SharedPreferences freqTypePref = getApplicationContext().getSharedPreferences(fileName, MODE_PRIVATE);
        int value = freqTypePref.getInt(keyName,0);
        return value;
    }

    private void GPSupdateFrequency(){
        Spinner spinner;
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        lastSpinnerOption = reloadPrefInt("freqTypePref","freqTypeInt");
        spinner.setSelection(lastSpinnerOption);
    }
}
