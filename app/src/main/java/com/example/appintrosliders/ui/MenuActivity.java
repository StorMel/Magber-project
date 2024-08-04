package com.example.appintrosliders.ui;



import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appintrosliders.R;

/**
 * creates the action bar drop menu
 * which contain the info of every button and its usages.
 */
public class MenuActivity extends AppCompatActivity {
    String description;//describe the button usages
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dialog=new Dialog(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){//once the user clicks an option ill open a dialog with the releveant info
            case R.id.HostMenu:
                description=getApplicationContext().getString(R.string.HostDescription);
                openInstructionDialog("Host", getApplicationContext().getDrawable(R.drawable.host),description);
                return  true;
            case R.id.JoinMenu:
                description=getApplicationContext().getString(R.string.JoinDescription);;
                openInstructionDialog("Join", getApplicationContext().getDrawable(R.drawable.joinicon),description);
                return  true;
            case R.id.DisconnectMenu:
                description=getApplicationContext().getString(R.string.DisconnectDescription);
                openInstructionDialog("Disconnect", getApplicationContext().getDrawable(R.drawable.disconnectbtn),description);
                return  true;
            case R.id.AdvertiseMenu:
                description=getApplicationContext().getString(R.string.AdvertiseDescription);;
                openInstructionDialog("Advertise", getApplicationContext().getDrawable(R.drawable.advertisebtn),description);
                return  true;
            case R.id.DiscoverMenu:
                description=getApplicationContext().getString(R.string.DiscoverDescription);
                openInstructionDialog("Discover", getApplicationContext().getDrawable(R.drawable.discoverbtn),description);
                return  true;
            case R.id.SuggestListMenu:
                description=getApplicationContext().getString(R.string.SuggestListDescription);
                openInstructionDialog("Suggest List", getApplicationContext().getDrawable(R.drawable.hostsuggest),description);
                return  true;
            case R.id.SuggestMenu:
                description=getApplicationContext().getString(R.string.SuggestDescription);
                openInstructionDialog("Suggest", getApplicationContext().getDrawable(R.drawable.clientsuggest),description);
                return  true;
            case R.id.InitMenu:
                description=getApplicationContext().getString(R.string.InitDescription);
                openInstructionDialog("Init", getApplicationContext().getDrawable(R.drawable.initbtn),description);
                return  true;
            case R.id.StartStreamingMenu:
                description=getApplicationContext().getString(R.string.StartStreamDescription);;
                openInstructionDialog("Start Stream", getApplicationContext().getDrawable(R.drawable.startstream),description);
                return  true;
            case R.id.StopStreamingMenu:
                description=getApplicationContext().getString(R.string.StopStreamDescription);
                openInstructionDialog("Stop Stream", getApplicationContext().getDrawable(R.drawable.stopstreaming),description);
                return  true;
            case R.id.menuItemHome:
                Intent intent=new Intent(this, SplashScreen.class);
                startActivity(intent);
                finish();
                return  true;

        }
        return super.onOptionsItemSelected(item);
    }
    public void openInstructionDialog(String S, Drawable drawable,String description){
        dialog.setContentView(R.layout.menu_instruction_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title=dialog.findViewById(R.id.textViewTitle);
        TextView Description=dialog.findViewById(R.id.textViewDescription);
        Button btnOk=dialog.findViewById(R.id.btnOK);
        ImageView icon=dialog.findViewById(R.id.imageViewIcon);

        Description.setText(description);
        icon.setImageDrawable(drawable);
        title.setText(S);
        dialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



}