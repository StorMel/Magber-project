package com.example.appintrosliders.backgroundprocess;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.appintrosliders.R;
import com.example.appintrosliders.ui.SplashScreen;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

import static android.os.Process.THREAD_PRIORITY_AUDIO;
import static android.os.Process.setThreadPriority;
import static com.example.appintrosliders.backgroundprocess.ServiceNotification.CHANNEL_ID;

/**
 * service class
 */
public class ConnectionService extends Service {

    //https://github.com/julioz/AudioCaptureSample
    public static final String ACTION_ALL = "ALL";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    public static final String EXTRA_ACTION_NAME = "ACTION_NAME";
    private static final int RECORDER_SAMPLERATE =44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * the payload pipe is how i transfer the data recorded
     */
    public  static ParcelFileDescriptor[] payloadPipe;//problems from this

    {
        try {
            payloadPipe = ParcelFileDescriptor.createPipe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private   OutputStream mOutputStream=  new ParcelFileDescriptor.AutoCloseOutputStream(payloadPipe[1]);
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    AudioRecord _recorder;



    private MediaProjectionManager _mediaProjectionManager;
    private MediaProjection _mediaProjection;

    Intent _callingIntent;
    Thread getAudioCaptureThread;

    public ConnectionService() {


    }

    /**
     * creates the service
     * sets a notification
     * and sets the media projection manager for later usage of screen audio recording
     */
    @Override
    public void onCreate() {
        super.onCreate();
//https://stackoverflow.com/questions/52543466/how-to-record-internal-audio-on-android-devices-or-record-mediaplayer-audio-stre
        String input = "audio service";
        Intent notificationIntent = new Intent(this, SplashScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("internal audio recorder")
                .setContentText(input)
                .setSmallIcon(R.drawable.myapplogo)
                //.setContentIntent(pendingIntent) idk if this will work
                .build();

        startForeground(1, notification);
        //do heavy work on a background thread
        // return START_NOT_STICKY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            _mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        }

    }




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _callingIntent = intent;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ALL);
        registerReceiver(_actionReceiver, filter);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * sets up the media projection with the MPManager and calls 'startRecording' function with that media projection object
     * @param intent
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecording(Intent intent) {//output stream setup
        //final int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        _mediaProjection = _mediaProjectionManager.getMediaProjection(-1, intent);
        startRecording(_mediaProjection);
    }
//https://stackoverflow.com/questions/57870287/audioplaybackcapture-android-10-not-working-and-recording-empty-sounds

    /**
     * sets the playback capture api,
     * audio recorder
     * and starts recording internal media on the 'getaudiocapturethread' thread
     * this function calls 'writeAudioDataToFile()' function
     * @param mediaProjection
     */
    @TargetApi(29)
    private void startRecording(MediaProjection mediaProjection ) { AudioPlaybackCaptureConfiguration config =
                new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .build();
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(RECORDER_AUDIO_ENCODING)
                .setSampleRate(RECORDER_SAMPLERATE)
                .setChannelMask(RECORDER_CHANNELS)
                .build();
        _recorder = new AudioRecord.Builder()
                //               .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BufferElements2Rec * BytesPerElement)
                .setAudioPlaybackCaptureConfig(config)
                .build();

        _recorder.startRecording();

        getAudioCaptureThread=new Thread(new Runnable() {
            @Override
            public void run() {
                setThreadPriority(THREAD_PRIORITY_AUDIO);
                writeAudioDataToFile();
            }
        });
        getAudioCaptureThread.start();
    }

    /**
     * data write from short to byte
     * (not completely sure how this function really works even though we got a bit of explanation on its functionality)
     * @param sData
     * @return
     */
    @NotNull
    private byte[] short2byte(@NotNull short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    /**
     * writes the output audio in bytes
     * and sends the data through the file descriptor pipe to the clients connected to the host
     */
    private void writeAudioDataToFile() {
        short sData[] = new short[BufferElements2Rec];

        while (!getAudioCaptureThread.isInterrupted()) {
            // gets the voice output from microphone to byte format
            int len= _recorder.read(sData, 0, BufferElements2Rec);
            //System.out.println("Short straeming to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                if(len>=0){
                    mOutputStream.flush();
                    byte bData[] = short2byte(sData);

                    mOutputStream.write(bData, 0, BufferElements2Rec * BytesPerElement);

                }

            } catch (IOException e) {
                e.printStackTrace();
                //broken pipe
            }
        }
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * stops recording (media projection)
     * stops the service
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopRecording() {
        // stops the recording activity
        getAudioCaptureThread.interrupt();
        try {
            getAudioCaptureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (null != _recorder) {

            _recorder.stop();
            _recorder.release();
            _recorder = null;
        }

        _mediaProjection.stop();

        stopSelf();
    }

    /**
     * closes the service and disconnects the communication service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_actionReceiver);
    }

    /**
     * the point of this receiver is to connect between the ui and the service to start and stop streaming, i don't really need this
     * but it has been in the project's to-do list so i added it
      */
    BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, @NotNull Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(ACTION_ALL)) {
                String actionName = intent.getStringExtra(EXTRA_ACTION_NAME);
                if (actionName != null && !actionName.isEmpty()) {
                    if (actionName.equalsIgnoreCase(ACTION_START)) {
                        startRecording(_callingIntent);
                    } else if (actionName.equalsIgnoreCase(ACTION_STOP)){
                        stopRecording();
                    }
                }

            }
        }
    };
}






