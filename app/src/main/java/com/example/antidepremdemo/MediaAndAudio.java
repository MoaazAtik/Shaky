package com.example.antidepremdemo;

import android.media.MediaPlayer;

public class MediaAndAudio {

//    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.soft);

    public void g() {

    }

}

//Running asynchronously
//public class MyService extends Service implements MediaPlayer.OnPreparedListener {
//    private static final String ACTION_PLAY = "com.example.action.PLAY";
//    MediaPlayer mediaPlayer = null;
//
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        ...
//        if (intent.getAction().equals(ACTION_PLAY)) {
//            mediaPlayer = ... // initialize it here
//            mediaPlayer.setOnPreparedListener(this);
//            mediaPlayer.prepareAsync(); // prepare async to not block main thread
//        }
//    }
//
//    /** Called when MediaPlayer is ready */
//    public void onPrepared(MediaPlayer player) {
//        player.start();
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null) mediaPlayer.release();
//    }
//}
