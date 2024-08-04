package com.example.appintrosliders.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

//https://www.youtube.com/watch?v=6Fr2PVYb8Kw

/**
 * this class is used once the user (as a host) clicks the suggest list button.
 * the class creates a dialog for the host to view all the sent links from the clients,
 * which enables access to the video once a link is chosen
 */
public class HostSuggestListDialog extends DialogFragment {
    private String[] List;//this needs to be a universal object

    public final String myPrefName="shared preferences";
    public final String suggestList="suggest list";
    ArrayList<String> arrayList;
    private Intent appIntent,webIntent;

    /**
     * this function gets an arrylist and transfers that data to a list
     * @param arrayList
     */
    public HostSuggestListDialog(ArrayList<String> arrayList){

this.arrayList=arrayList;

int counter=arrayList.size();
List=new String[counter];
       List = arrayList.toArray(List);

//for some reason the second time i try to open the list it crashes.?
        }


    /**
     * fuction to creat the dialog shown to the user
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("suggested by clients");
builder.setItems(List, new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
//opens that link


       watchYoutubeVideo(List[which]);
       DelFromPref(which);


    }
});

        return builder.create();
    }


    /**
     * this function creates an intent which opens youtube's app
     * or if it doesn't exist on the phon, it will be opened from the browser.
     * @param id to specify a curtain video link(no id will open the youtube homepage)
     */
    public void watchYoutubeVideo(String id) {
        if(URLUtil.isValidUrl(id)){  appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(id));//https://stackoverflow.com/questions/4905075/how-to-check-if-url-is-valid-in-android
            webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse( id));}else {
            id="http://www.youtube.com/watch?v= ";
             appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(id));
             webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse( id));}


        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }


    /**
     * this class deletes a chosen link from the shared preferences once it has been clicked
     * @param location  is the location the item is held in the list
     */
    public void DelFromPref(int location){
        SharedPreferences  sharedPreferences=getContext().getSharedPreferences(myPrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove(suggestList);//remove the entire arraylist
        editor.apply();
        ChangeList(location);
        saveData();
    }

    /**
     * removes a curtain item from the list, this function is called from the 'DelFromPref' function
     * @param location is the location the item is held in the list
     */
    private void ChangeList(int location) {
        //remove the item from the arraylist

        arrayList.remove(location);
        if(arrayList.isEmpty()){
            arrayList.add("youtube");
        }
    }

    /**
     * this function saves the current list to the shared preferences
     */
    private void saveData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(myPrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(suggestList, json);
        editor.apply();
    }
}
