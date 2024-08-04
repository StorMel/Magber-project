package com.example.appintrosliders.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.appintrosliders.R;
import com.example.appintrosliders.backgroundprocess.ClientAudioPlayer;
import com.example.appintrosliders.backgroundprocess.ConnectionService;
import com.example.appintrosliders.backgroundprocess.NCFunctions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
//import com.instacart.library.truetime.TrueTime;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static android.os.Process.setThreadPriority;

public class ConnectionActionPanelScreen extends NCFunctions implements View.OnClickListener, ClientSuggestDialog.ExampleDialogListener {


    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    /** Length of state change animations. */
    private static final long ANIMATION_DURATION = 600;
    private static final String SERVICE_ID =
            "com.google.location.nearby.apps.walkietalkie.manual.SERVICE_ID";
    private State mState = State.UNKNOWN;
    private String mName;
    /** Displays the previous state during animation transitions. */
    private TextView mPreviousStateView;
    /** Displays the current state. */
    private TextView mCurrentStateView;
    /** An animator that controls the animation from previous state to current state. */
    @Nullable
    private Animator mCurrentAnimator;
    private boolean isStreaming=false;
    public ConnectionService exampleService;
    private static final int CREATE_SCREEN_CAPTURE = 1001;
    /** For playing audio from other users nearby. */
    private final Set<ClientAudioPlayer> mAudioPlayers = new HashSet<>();
    /** The phone's original media volume. */
    private int mOriginalVolume;

    /**
     * A Handler that allows us to post back on to the UI thread. We use this to resume discovery
     * after an uneventful bout of advertising.
     */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    /** Starts discovery. Used in a postDelayed manor with {@link #mUiHandler}. */
    private final Runnable mDiscoverRunnable =
            new Runnable() {
                @Override
                public void run() {
                    setState(State.DISCOVERING);
                }
            };


    //setting all the buttons and UI components
    ImageButton startAudio,stopAudio,init, startAdvertising,startDiscovery,disconnect,suggest,HostsuggestList;
    ClientAudioPlayer player;
    Thread syncThread;
    Long offset= Long.valueOf(0);
    String type;
    boolean timeThread,playerInit,sync,isHost,Init=false;
    private ArrayList<String> arrayList;
    private HostSuggestListDialog fragmentDialog;

    //shared pref
    public final String myPrefName="shared preferences";
    public final String suggestList="suggest list";

   //setup and controls

    /**
     * creates the action panel,
     * sets all the buttons,
     * the device name and status
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_action_screen);



        Bundle bundle = getIntent().getExtras();
        isHost = bundle.getBoolean("isHost");
        type=bundle.getString("type");
        mPreviousStateView = (TextView) findViewById(R.id.previous_stateTxtCoAcSc);
        mCurrentStateView = (TextView) findViewById(R.id.statusTxtCoAcSc);
        mName = generateRandomName()+type+")";
        ((TextView) findViewById(R.id.devnameTxtCoAcSc)).setText(mName);




        disconnect=findViewById(R.id.disconnectBtnCoAcSc);
        //host
        startAdvertising=findViewById(R.id.advertiseBtnCoAcSc);

        startAudio=findViewById(R.id.startaudioBtnCoAcSc);
        stopAudio=findViewById(R.id.stopaudioBtnCoAcSc);
        init=findViewById(R.id.initBtnCoAcSc);
        HostsuggestList=findViewById(R.id.suggestListBtnCoAcSc);

        //client
        startDiscovery=findViewById(R.id.discoverBtnCoAcSc);

        suggest=findViewById(R.id.suggestBtnCoAcSc);//ill change it to the suggestion button
//dialog



disconnect.setOnClickListener(this);

if(isHost){
startAdvertising.setOnClickListener(this);

startAudio.setOnClickListener(this);
stopAudio.setOnClickListener(this);
init.setOnClickListener(this);
HostsuggestList.setOnClickListener(this);

    }else {
    startDiscovery.setOnClickListener(this);
    suggest.setOnClickListener(this);
    //pause.setOnClickListener(this);



}

    }

    /**
     * this function sets the action panel page according to the user status(host or client)
     */
    @Override
    protected void onStart() {
        sync=false;
       timeThread=false;
        if(!isHost){
            playerInit=false;
            startAdvertising.setVisibility(View.INVISIBLE);

            startAudio.setVisibility(View.INVISIBLE);
             stopAudio.setVisibility(View.INVISIBLE);
HostsuggestList.setVisibility(View.INVISIBLE);
            init.setVisibility(View.INVISIBLE);


        }else if(isHost){

loadData();

            startDiscovery.setVisibility(View.INVISIBLE);

       //    init.setVisibility(View.INVISIBLE);
suggest.setVisibility(View.INVISIBLE);
            startAudio.setVisibility(View.INVISIBLE);
           stopAudio.setVisibility(View.INVISIBLE);
if(getState()==State.CONNECTED  ){

    if(!Init){init.setClickable(true);
        init.setAlpha((float) 1); }
    else if(Init && !isStreaming){startAudio.setVisibility(View.VISIBLE);}
    else if(Init && isStreaming){stopAudio.setVisibility(View.VISIBLE);}
}else {
    init.setVisibility(View.VISIBLE);
    init.setClickable(false);
    init.setAlpha((float) 0.5);
}
        }
        super.onStart();
    }

    /**
     * if the user closes the app completely, in order for the phone to not freeze
     * the function closes all app related connections, threads and services.
     */
    @Override
    protected void onDestroy() {
        StopAll();
        super.onDestroy();

    }

    /**
     * onclick is the listener function for anytime a button is being clicked
     * when that happens the function executes the buttons functionality
     * @param view
     */
    @Override
    public void onClick(@NotNull View view) {
//switch case
        if(HostsuggestList.equals(view)){

                fragmentDialog=new HostSuggestListDialog(arrayList);
                fragmentDialog.show(getSupportFragmentManager(),"fragmentDialog");

        }else  if (suggest.equals(view)) {
           // if(getState()==State.CONNECTED){
                openDialog();
          //  }else {
                //can not use this property when not connected
            //    Toast.makeText(nearbyconnectionAdd.this, "must connect to a host first", Toast.LENGTH_SHORT).show();
          //  }

        } else if (startAudio.equals(view)) {//permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                if(ContextCompat.checkSelfPermission(ConnectionActionPanelScreen.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                { startRecording();}else {
                    //permission dialog
                    grantPermissions();
                }
            }
        } else if (stopAudio.equals(view)) {
            stopStreaming();
        } else if (startAdvertising.equals(view)) {//permission
            if(ContextCompat.checkSelfPermission(ConnectionActionPanelScreen.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            { setState(State.ADVERTISING);}else {
                //permission dialog
                grantPermissions();
            }
        } else if (startDiscovery.equals(view)) {//permission
            if(ContextCompat.checkSelfPermission(ConnectionActionPanelScreen.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            { setState(State.DISCOVERING);}else {
                //permission dialog
                grantPermissions();
            }


        } else if (disconnect.equals(view)) {
            StopAll();
        }
        else if(init.equals(view)){
            initAudioCapture();
        }
    }

    /**
     * opens the client suggest dialog,  the client can send links to the host from this dialog
     */
    private void openDialog() {
        ClientSuggestDialog exampleDialog = new ClientSuggestDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    /**
     * function for permission approval,
     * the user must accept the permissions in order for the app to audio record
     * and use the device location to locate other devices near by
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void grantPermissions() {
        PermissionListener permissionListener=new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(ConnectionActionPanelScreen.this, "Permission GRANTED", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(ConnectionActionPanelScreen.this, "Permission DENIED", Toast.LENGTH_SHORT).show();

            }
        };
        TedPermission.with(ConnectionActionPanelScreen.this)
                .setPermissionListener(permissionListener)
                .setPermissions( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
                .check();
        //https://developer.android.com/training/permissions/requesting
        //https://www.youtube.com/watch?v=SMrB97JuIoM


    }

    /**
     * function to stop all background processes, stop streaming,
     * threads and connections between devices.
     */
    private void StopAll(){

        if (isPlaying()) {//when the host disconnects the clients dont stop playing fields so it may be causing problems after some time
            stopPlaying();
        }


        if(isStreaming)
        {stopStreaming();}

        setState(State.UNKNOWN);





        mUiHandler.removeCallbacksAndMessages(null);

        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }
        if(isHost){
            init.setVisibility(View.VISIBLE);
            init.setClickable(false);
            init.setAlpha((float) 0.5);
            stopAudio.setVisibility(View.INVISIBLE);
            startAudio.setVisibility(View.INVISIBLE);
        }else if(!isHost){
           // suggest.setVisibility(View.VISIBLE);
           //suggest.setClickable(false);
            //suggest.setAlpha((float) 0.5);
        }
// Restore the original volume.
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }


    //shared pref

    /**
     * functions to load and save data from and to the shared preferences
     */
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(myPrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(suggestList, json);
        editor.apply();
    }
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(myPrefName, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(suggestList, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        arrayList = gson.fromJson(json, type);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            arrayList.add("youtube");
        }
    }


    //connection stats

    /**
     * these functions are from the NCFunctions class.
     * they are responsible for maintaining the connection btween the devices,
     * creating connection between the devices and disconnecting
     *
     * @param endpoint
     */
    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        if (!isConnecting()) {
            connectToEndpoint(endpoint);
        }
    }
    @Override
    protected void onConnectionInitiated(Endpoint endpoint, @NotNull ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll accept the connection immediately.


//if(!isStreaming){
    AlertDialog alertDialog = new AlertDialog.Builder(this)
//set icon
        .setIcon(android.R.drawable.ic_dialog_alert)
//set title
        .setTitle("Accept connection to " + connectionInfo.getEndpointName())
//set message
        .setMessage("Confirm the code matches on both devices: " + connectionInfo.getAuthenticationToken())
//set positive button
        .setPositiveButton("accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //set what would happen when positive button is clicked
                acceptConnection(endpoint);
            }
        })
//set negative button
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //set what should happen when negative button is clicked
                rejectConnection(endpoint);
            }
        })
        .show();
//}else if(isHost) { rejectConnection(endpoint);}//host will reject any user trying to join while the stream is running



    }
    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
       /* Toast.makeText(
                this, "connect", Toast.LENGTH_SHORT)
                .show();*/
        setState(State.CONNECTED);
//if streaming i cant connect another phone, must stop stream to restart
//if(isStreaming){//still a bit of a problem
//send the endpoint of the stream to this point
   // sendToEndPoint(Payload.fromStream(ExampleService.payloadPipe[0]),endpoint);
//i need to block the ability to join if a stream is running
//}
        // Set the media volume to max.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

if(isHost ){
    init.setVisibility(View.VISIBLE);
    init.setClickable(true);
    init.setAlpha((float) 1);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC), 0);
    }
}
else {
   // suggest.setVisibility(View.VISIBLE);
   // suggest.setClickable(true);
   // suggest.setAlpha((float) 1);
    audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
}
    }
    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {//something here is wrong
        /*Toast.makeText(
                this, "disconnect", Toast.LENGTH_SHORT)
                .show();*/

        // If we lost all our endpoints, then we should reset the state of our app and go back
        // to our initial state (discovering).
        if (getConnectedEndpoints().isEmpty()) {
            StopAll();
        }
    }
    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        // Let's try someone else.
        /*if (getState() == State.DISCOVERING && !getDiscoveredEndpoints().isEmpty()) {
            connectToEndpoint(pickRandomElem(getDiscoveredEndpoints()));
        }*/
       if(!isHost){
           StopAll();
           //my be make it re try discovering?
       }
    }


   //state adjustments

    /**
     * functions which dispay the status of the device and transition from status to status with a short animation
     * @param state
     */
    private void setState(State state) {
        if (mState == state) {
            Toast.makeText(getApplicationContext(),"state set to "+state+" already in that state",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getApplicationContext(),"state set to: "+state,Toast.LENGTH_SHORT).show();
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }
    private State getState() {
        return mState;
    }
    private void onStateChanged(State oldState, State newState) {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        // Update Nearby Connections to the new state.
        switch (newState) {
            case DISCOVERING:
                if (isAdvertising()) {
                    stopAdvertising();
                }
                disconnectFromAllEndpoints();
                startDiscovering();
                break;
            case ADVERTISING://if the user is connected and you press this to restart advertising it will allow

                if (isDiscovering()) {
                    stopDiscovering();
                }
                disconnectFromAllEndpoints();
                startAdvertising();
                break;
            case CONNECTED:
                if (isDiscovering()) {
                    stopDiscovering();
                } else if (isAdvertising()) {
                    // Continue to advertise, so others can still connect,
                    // but clear the discover runnable.
                    removeCallbacks(mDiscoverRunnable);
                }
                break;
            case UNKNOWN:
                stopAllEndpoints();

                break;
            default:
                // no-op
                break;
        }

        // Update the UI.
        switch (oldState) {
            case UNKNOWN:
                // Unknown is our initial state. Whatever state we move to,
                // we're transitioning forwards.
                transitionForward(oldState, newState);
                break;
            case DISCOVERING:
                switch (newState) {
                    case UNKNOWN:
                        transitionBackward(oldState, newState);
                        break;
                    case ADVERTISING:
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case ADVERTISING:
                switch (newState) {
                    case UNKNOWN:
                    case DISCOVERING:
                        transitionBackward(oldState, newState);
                        break;
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case CONNECTED:
                // Connected is our final state. Whatever new state we move to,
                // we're transitioning backwards.
                transitionBackward(oldState, newState);
                break;
            default:
                // no-op
                break;
        }
    }
    /** Transitions from the old state to the new state with an animation implying moving forward. */
    @UiThread
    private void transitionForward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mPreviousStateView, oldState);
        updateTextView(mCurrentStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(false /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }
    /** Transitions from the old state to the new state with an animation implying moving backward. */
    @UiThread
    private void transitionBackward(State oldState, final State newState) {
        mPreviousStateView.setVisibility(View.VISIBLE);
        mCurrentStateView.setVisibility(View.VISIBLE);

        updateTextView(mCurrentStateView, oldState);
        updateTextView(mPreviousStateView, newState);

        if (ViewCompat.isLaidOut(mCurrentStateView)) {
            mCurrentAnimator = createAnimator(true /* reverse */);
            mCurrentAnimator.addListener(
                    new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            updateTextView(mCurrentStateView, newState);
                        }
                    });
            mCurrentAnimator.start();
        }
    }
    @NonNull
    private Animator createAnimator(boolean reverse) {
        Animator animator;
        if (Build.VERSION.SDK_INT >= 21) {
            int cx = mCurrentStateView.getMeasuredWidth() / 2;
            int cy = mCurrentStateView.getMeasuredHeight() / 2;
            int initialRadius = 0;
            int finalRadius = Math.max(mCurrentStateView.getWidth(), mCurrentStateView.getHeight());
            if (reverse) {
                int temp = initialRadius;
                initialRadius = finalRadius;
                finalRadius = temp;
            }
            animator =
                    ViewAnimationUtils.createCircularReveal(
                            mCurrentStateView, cx, cy, initialRadius, finalRadius);
        } else {
            float initialAlpha = 0f;
            float finalAlpha = 1f;
            if (reverse) {
                float temp = initialAlpha;
                initialAlpha = finalAlpha;
                finalAlpha = temp;
            }
            mCurrentStateView.setAlpha(initialAlpha);
            animator = ObjectAnimator.ofFloat(mCurrentStateView, "alpha", finalAlpha);
        }
        animator.addListener(
                new AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mPreviousStateView.setVisibility(View.GONE);
                        mCurrentStateView.setAlpha(1);
                    }
                });
        animator.setDuration(ANIMATION_DURATION);
        return animator;
    }
    @UiThread
    private void updateTextView(TextView textView, @NotNull State state) {
        switch (state) {
            case DISCOVERING:

                textView.setText("status_discovering");
                break;
            case ADVERTISING:

                textView.setText("status_advertising");
                break;
            case CONNECTED:

                textView.setText("status_connected");
                break;
            default:

                textView.setText("status_unknown");
                break;
        }
    }


//functions for steaming

    /**
     * returns the device time in milliseconds
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NotNull
    public static Long getTrueTime() {
        // Date date = TrueTime.isInitialized() ? TrueTime.now() : new Date();
        long instant=Instant.now().toEpochMilli();
        return instant;
    }

    /**
     * this function heps complete the communication between the phones,
     * once a phone sends payload it is delivered to other phones through the file descriptor pipe to this function.
     * here i check the type of payload and operate accordingly
     * @param endpoint The sender.
     * @param payload The data.
     */
    @Override
    protected void onReceive(Endpoint endpoint, @NotNull Payload payload) {
        //if a user is connected but wont get the payload the app will collapse once receiving the time data
        /**
         * if the payload is stream, the client creates a 'player' which will play what is sent to him from the host
         * only the clients receive stream payload from the host
         */
        if (payload.getType() == Payload.Type.STREAM) {
            //player=null;//idk if this does anything

            player =
                    new ClientAudioPlayer(payload.asStream().asInputStream()) {
                        @WorkerThread
                        @Override
                        protected void onFinish() {
                            final ClientAudioPlayer audioPlayer = this;
                            post(
                                    new Runnable() {
                                        @UiThread
                                        @Override
                                        public void run() {
                                            mAudioPlayers.remove(audioPlayer);
                                        }
                                    });
                        }
                    };
            mAudioPlayers.add(player);
            player.start();
          //  pause.setEnabled(true);
            //need to make an animation once the payload is accepted

        }
        /**
         * this payload type is bytes, i use this method in order to transfer the suggested links from the clients to the host
         * or to send check signals of time from the host to the clients to keep the sync
         */
        else if (payload.getType()== Payload.Type.BYTES){//if i send a message

            final   byte[] receivedBytes = payload.asBytes();
    if(!isHost){
            Long tclient =System.currentTimeMillis();//getUTCTime();//getTrueTime().getTime();//
            Long tHost=Long.valueOf(new String(receivedBytes));// long timeTaken=(timestamp-Long.valueOf(new String(receivedBytes)));
            if((tclient-offset)!=tHost && Math.abs((tclient-offset)-tHost)>=5){//if false and the millisecond gap is over 5, the offset needs an update
                //pause
                player.setPause(1);
                Long hostTimePlusXSec=tHost+100;//x=+0.1 seconds
                offset=tclient-tHost;

                while ((System.currentTimeMillis()-offset)<hostTimePlusXSec){
                    //basically now i need to wait till the time is right

                }//unpause playback
                player.setPause(0);
            }




        }else {//if the host gets a suggest link from client
//open the dialog with a recycle view of all the links sent by clients


arrayList.add(new String(receivedBytes));
        saveData();

}
        }// need to make a thread that checks every 10 seconds if the delay changed
    }

    /**
     * the host uses this function to send the clients the check signal for the sync test
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendPayLoad() {//sync
        // Long timeStamp = System.currentTimeMillis();
        // String currentTimeStamp = timeStamp.toString();
        //long time=getTrueTime().getTime();//returns ms from jan 1 1970 00:00
        final String T=String.valueOf(getTrueTime());//String.valueOf(getTrueTime().getTime());
        Payload bytesPayload = Payload.fromBytes(T.getBytes());

        send(bytesPayload);

        //needs to send true time from ntp server and then ill have to check on the client its time

    }

    /**
     * client can send the host link strings with the suggest dialog
     * this function checks that they are still connected to the host and sends the text(link) to the host's shared preferences
     * @param suggest
     */
    @Override
    public void applyTexts(@NotNull String suggest) {//the ling string the client sends to the host
        if(suggest.length()>0){ Payload bytesPayload = Payload.fromBytes(suggest.getBytes());
            if(getState()==State.CONNECTED){
                send(bytesPayload);//client sends to the host
            }else {
                Toast.makeText(ConnectionActionPanelScreen.this, "failed to send", Toast.LENGTH_SHORT).show();
            }
        }

    }
    /** Stops all currently streaming audio tracks. */
    private void stopPlaying() {


        for (ClientAudioPlayer player : mAudioPlayers) {
            player.stop();
            Toast.makeText(getApplicationContext(),"stop playing ",Toast.LENGTH_SHORT).show();
        }
        mAudioPlayers.clear();
    }
    private boolean isPlaying() {
        return !mAudioPlayers.isEmpty();
    }
    /** Starts recording internal sound and streaming it to all connected devices. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording() {
        if(Init){
            Toast.makeText(getApplicationContext(),"start recording... ",Toast.LENGTH_SHORT).show();

            // Send the first half of the payload (the read side) to Nearby Connections.
            // send(Payload.fromStream(payloadPipe[0]));
            send(Payload.fromStream(ConnectionService.payloadPipe[0]));


            /**
             *this thread is the thread for synchronising th audio
             * the host sends a check signal every 5000ms (5 sec) and the other devices check if
             * their time with the offset is the same as the host's.
             * if it isnt then the device stops playing, the offset is updated and the device continues
             * to play with an updated offset to keep the sync
             */
            syncThread=new Thread(new Runnable() {//idk why but the thread is not working
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    try {
                        syncThread.sleep(2000);
                        stopAudio.setClickable(true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (!syncThread.isInterrupted())
                    {

                        sendPayLoad();//sends the time payload
                        Long time=getTrueTime();
                        while (Instant.now().toEpochMilli()<=(time+5000)){//seems to be working better than system sleep
                            //this way if the app crashes the loop wont freeze the device
                            if(syncThread.isInterrupted())
                            { break;}

                        }

                    }
                }
            });

            // syncThread.run();

            startStreaming();
            //sendPayLoad();
            syncThread.start();isStreaming=true;
            stopAdvertising();//this will also block players from joining the stream
startAudio.setVisibility(View.INVISIBLE);
stopAudio.setClickable(false);
            stopAudio.setVisibility(View.VISIBLE);
        }
        else {
            initAudioCapture();

        }
    }


    //internal audio functions

    /**
     * this function is initiated after the host accepts the internal audio recording on the device
     *
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)//needs to have a button
    private void initAudioCapture() {
        MediaProjectionManager _manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = _manager.createScreenCaptureIntent();
        startActivityForResult(intent, CREATE_SCREEN_CAPTURE);
    }

    /**
     * the function sends through the broadcast receiver to the service class a command to stop streaming
     */
    private void stopStreaming() {
        startAdvertising();
        if(isStreaming){ syncThread.interrupt();
            try {
                syncThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }isStreaming=false;}
        Init=false;
stopAudio.setVisibility(View.INVISIBLE);

        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(exampleService.ACTION_ALL);
        broadCastIntent.putExtra(exampleService.EXTRA_ACTION_NAME, exampleService.ACTION_STOP);
        this.sendBroadcast(broadCastIntent);init.setVisibility(View.VISIBLE) ;init.setClickable(true);
        init.setAlpha((float) 1);   }

    /**
     *the function sends through the broadcast receiver to the service class a command to start streaming
     */
    private void startStreaming() {
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(exampleService.ACTION_ALL);
        broadCastIntent.putExtra(exampleService.EXTRA_ACTION_NAME, exampleService.ACTION_START);
        // Use the second half of the payload (the write side) in AudioRecorder.
        // broadCastIntent.putExtra("pipe", payloadPipe);//better method of doing it
        this.sendBroadcast(broadCastIntent);
    }

    /**
     * this function is called once the user(host) accepts or declines the device to internal audio record
     * if the user accepts, the function will start the background service.
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (CREATE_SCREEN_CAPTURE == requestCode) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(this, ConnectionService.class);
                i.setAction(exampleService.ACTION_START);
                i.putExtra(exampleService.EXTRA_RESULT_CODE, resultCode);
                i.putExtras(intent);
                this.startService(i);
                Init=true;
                init.setVisibility(View.INVISIBLE);
                startAudio.setVisibility(View.VISIBLE);

            } else {
                // user did not grant permissions
            }
        }
    }




    /**
     * returns the name of the device with its random id number
     * @return
     */
    @Override
    protected String getName() {
        return mName;
    }
    /**
     * returns the service id
     * @return
     */
    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }
    /**
     * returns the strategy used in the nearby connection api
     * @return
     */
    @Override
    public Strategy getStrategy() {
        return STRATEGY;
    }
    protected void post(Runnable r) {
        mUiHandler.post(r);
    }
    protected void removeCallbacks(Runnable r) {
        mUiHandler.removeCallbacks(r);
    }

    /**
     * generates a random number id for the device name plus the device type
     * @return
     */
    @NotNull
    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name+"("+getDeviceName()+")";
    }
    @NotNull
    @Contract("null -> !null")
    /**
     * turns all letters in string s into capital letters
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * returns the device name(type)
     * @return
     */
    @NotNull
    public static  String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * listener for the animation which displays the device status.
     */
    private abstract static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

    /** States that the UI goes through. */
    public enum State {
        UNKNOWN,
        DISCOVERING,
        ADVERTISING,
        CONNECTED
    }
}