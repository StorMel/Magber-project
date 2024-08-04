package com.example.appintrosliders.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.appintrosliders.R;
//https://codinginflow.com/tutorials/android/custom-dialog-interface

/**
 * this class is used once a client clicks the 'suggest' button
 * giving the client the ability to send link recommendations for the host to stream from youtube
 */
public class ClientSuggestDialog extends AppCompatDialogFragment implements View.OnClickListener {
   private EditText suggest;
   private ImageButton YT;
    private ExampleDialogListener listener;

    /**
     * this function creates the dialog shown to the user
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_client_suggest_dialog, null);
        builder.setView(view)
                .setTitle("Suggest a Video")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String suggested = suggest.getText().toString();

                        listener.applyTexts(suggested);
                    }
                });
        suggest = view.findViewById(R.id.ClientSuggestETSuDi);
        YT=view.findViewById(R.id.YTBtnSuDi);
        YT.setOnClickListener(this);
        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    @Override
    public void onClick(View v) {
        if(v==YT){
            //open youtube
            watchYoutubeVideo("");
        }
    }


    /**
     * this function creates an intent which opens youtube's app
     * or if it doesn't exist on the phon, it will be opened from the browser.
     * @param id to specify a curtain video link(no id will open the youtube homepage)
     */
    public void watchYoutubeVideo(String id) {//https://stackoverflow.com/questions/42024058/how-to-open-youtube-video-link-in-android-app
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }
    public interface ExampleDialogListener {
        void applyTexts(String suggest);
    }
}