package com.example.appintrosliders.backgroundprocess;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;
import static android.os.Process.THREAD_PRIORITY_AUDIO;
import static android.os.Process.setThreadPriority;


/**
 * A fire-once class. When created, you must pass a {@link InputStream}. Once {@link #start()} is
 * called, the input stream will be read from until either {@link #stop()} is called or the stream
 * ends.
 */

public class ClientAudioPlayer {
    /** The audio stream we're reading from. */
    private final InputStream mInputStream;
    private final int SIZE=1024;
    private final byte[] data=new byte[SIZE*2];

    /**
     * If true, the background thread will continue to loop and play audio. Once false, the thread
     * will shut down.
     */
    private volatile boolean mAlive;

    /** The background thread recording audio for us. */
    private Thread mThread;

    /**
     * A simple audio player.
     *
     * @param inputStream The input stream of the recording.
     */
    public ClientAudioPlayer(InputStream inputStream) {
        mInputStream = inputStream;
    }
    private boolean pause=false;
    /** @return True if currently playing. */
    public boolean isPlaying() {
        return mAlive;
    }

    /** Starts playing the stream. */
    public void start() {
        mAlive = true;
        mThread =
                new Thread() {
                    @Override
                    public void run() {
                        setThreadPriority(THREAD_PRIORITY_AUDIO);


                        AudioTrack audioTrack = new AudioTrack.Builder()//https://developer.android.com/reference/android/media/AudioTrack.Builder
                                .setAudioAttributes(new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        //  .setLegacyStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE)
                                        .build())
                                .setAudioFormat(new AudioFormat.Builder()
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(22050*2)//22050
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                        .build())
                                .setBufferSizeInBytes(SIZE*2)//2048
                                .setTransferMode(AudioTrack.MODE_STREAM)

                                .build();
                        audioTrack.play();

                        int len;
                        try {
                            while (isPlaying() && (len = mInputStream.read(data)) > 0) {
                                if(!pause){audioTrack.write(data, 0, len);}

                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Exception with playing stream", e);
                        } finally {
                           stopInternal();
                            audioTrack.release();
                            onFinish();
                        }
                    }
                };
        mThread.start();
    }

    /**
     * function to pause the data output in case the phone has been detected to be off sync.
     * @param x
     */
    public void setPause(int x){
        if(x==1)
            pause=true;
        else if (x==0){
            pause=false;
        }

    }
    /** Stops playing the stream. */
    private void stopInternal() {
        mAlive = false;
        try {
            mInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close input stream", e);
        }
    }

    /** Stops playing the stream. */
    public void stop() {
        stopInternal();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while joining AudioRecorder thread", e);
            Thread.currentThread().interrupt();
        }
    }

    /** The stream has now ended. */
    protected void onFinish() {}


}