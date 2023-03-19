package com.example.exploringgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private GLSurfaceView surface;
    private boolean isSurfaceCreated;
    private Intent mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mp = new Intent(this, MusicPlayer.class);
        startService(mp);

        //Optional for full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //get a reference to the Activity Manager (AM)
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //from the AM we get an object with our mobile device info
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();

        int supported = 1;

        if(configurationInfo.reqGlEsVersion>=0x30000)
            supported = 3;
        else if(configurationInfo.reqGlEsVersion>=0x20000)
            supported = 2;

        Log.v("TAG","Opengl ES supported >= " +
                supported + " (" + Integer.toHexString(configurationInfo.reqGlEsVersion) + " " +
                configurationInfo.getGlEsVersion() + ")");

        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(supported);
        surface.setPreserveEGLContextOnPause(true);
        GLSurfaceView.Renderer renderer = new ExploringMazeGame2();

        setContentView(surface);
        ((BasicRenderer) renderer).setContextAndSurface(this,surface);
        surface.setRenderer(renderer);
        isSurfaceCreated = true;

    }

    @Override
    public void onResume(){
        super.onResume();
        startService(mp);
        if(isSurfaceCreated)
            surface.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        stopService(mp);
        if(isSurfaceCreated)
            surface.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(mp);
    }


}
