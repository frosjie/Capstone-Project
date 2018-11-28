package com.example.weijie.i_bag;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.InputStream;

public class MainMenuActivity extends AppCompatActivity {
    private int backButtonPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getSupportActionBar().setTitle("Menu");

        ImageButton btn_map = findViewById(R.id.imageButtonMap);
        ImageButton btn_control = findViewById(R.id.imageButtonControl);

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapActivityIntent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(mapActivityIntent);
            }
        });

        btn_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(mainActivityIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent_settings = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent_settings);
                return true;

            case R.id.logOut:
                Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent_login);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(backButtonPressed < 1){
            Toast.makeText(getApplicationContext(), "Press 1 more time to exit", Toast.LENGTH_SHORT).show();
            backButtonPressed ++;
        }else{
            moveTaskToBack(true);
            backButtonPressed = 0;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        backButtonPressed = 0;
    }
}