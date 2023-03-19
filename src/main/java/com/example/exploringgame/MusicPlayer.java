package com.example.exploringgame;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicPlayer extends Service {

    private MediaPlayer mPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = MediaPlayer.create(this, R.raw.game_music);

        if(mPlayer!= null) {
            mPlayer.setLooping(true);
            mPlayer.setVolume(100, 100);

            mPlayer.setOnErrorListener((mp, what, extra) -> {

                mPlayer.reset();
                mPlayer.release();
                return true;
            });

            mPlayer.start();
        }
    }

    public void onDestroy() {
        mPlayer.stop();
        mPlayer.reset();
        mPlayer.release();
    }

}