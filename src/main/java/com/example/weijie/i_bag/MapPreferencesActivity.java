package com.example.weijie.i_bag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MapPreferencesActivity extends AppCompatActivity {

    private int map_type, poi_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_preferences);

        getSupportActionBar().setTitle("Map Preferences");
        radioButtonInitAndSelection();

        SeekBar seekBar=(SeekBar)findViewById(R.id.seekBar);

        final TextView poi_tv = (TextView)findViewById(R.id.poi_tv);
        poi_number = ReloadPreferences("poi_number");
        if(poi_number == 0){
            poi_number = 1;
            seekBar.setProgress(0);
        }else{
            seekBar.setProgress(poi_number - 1);
        }
        poi_tv.setText(String.valueOf(poi_number));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                poi_number = progress + 1;
                SavePreferences("poi_number",poi_number);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                poi_number = ReloadPreferences("poi_number");
                Log.d("SeekBar","SeekBar = " + poi_number);
                poi_tv.setText(String.valueOf(poi_number));
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d("backButton", "onBackPressed Called");
        Intent mapActivity_intent = new Intent(getApplicationContext(), MapsActivity.class);
        mapActivity_intent.putExtra("mapType",map_type);
        mapActivity_intent.putExtra("poiNumber",poi_number);
        startActivity(mapActivity_intent);
    }

    private void SavePreferences(String key, int value){
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private int ReloadPreferences(String key){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MY_SHARED_PREF",MODE_PRIVATE);
        int RETRIVED_DATA = sharedPreferences.getInt(key,0);
        return RETRIVED_DATA;
    }

    private void radioButtonInitAndSelection(){
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        map_type = ReloadPreferences("RADIO_BUTTON_INDEX");

        if(map_type == 1){
            radioGroup.check(R.id.normal);
        }else if(map_type == 2){
            radioGroup.check(R.id.satellite);
        }else if(map_type == 3){
            radioGroup.check(R.id.terrain);
        }else{
            radioGroup.check(R.id.hybrid);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.normal) {
                    map_type = 1;
                    SavePreferences("RADIO_BUTTON_INDEX",1);
                    Toast.makeText(getApplicationContext(), "Normal",
                            Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.satellite) {
                    map_type = 2;
                    SavePreferences("RADIO_BUTTON_INDEX",2);
                    Toast.makeText(getApplicationContext(), "Satellite",
                            Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.terrain) {
                    map_type = 3;
                    SavePreferences("RADIO_BUTTON_INDEX",3);
                    Toast.makeText(getApplicationContext(), "Terrain",
                            Toast.LENGTH_SHORT).show();
                }else {
                    map_type = 4;
                    SavePreferences("RADIO_BUTTON_INDEX",4);
                    Toast.makeText(getApplicationContext(), "Hybrid",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}