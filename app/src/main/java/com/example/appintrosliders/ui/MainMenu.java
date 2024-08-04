package com.example.appintrosliders.ui;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.appintrosliders.R;

/**
 * this class comes up after the splashscreen and enables the user to proceed as a host or client
 */
public class MainMenu extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    /**
     * open the client action panel
     * @param view
     */
    public void openClientActionsPanel(View view){
        Intent intent=new Intent(this, ConnectionActionPanelScreen.class);
        intent.putExtra("type","client");
        intent.putExtra("isHost",false);
        startActivity(intent);
    }

    /**
     * opens the host action panel, can be used from sdk ver' 29 abd higher
     * @param view
     */
    public void openHostActionsPanel(View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//api lower than 29

            Intent intent=new Intent(this, ConnectionActionPanelScreen.class);
            //Intent intent=new Intent(this,internalAudioRecTest.class);
            intent.putExtra("type","host");
            intent.putExtra("isHost",true);
            startActivity(intent);
        }else {
            Toast.makeText(MainMenu.this,"not able to use this property",Toast.LENGTH_SHORT);
        }


    }
}