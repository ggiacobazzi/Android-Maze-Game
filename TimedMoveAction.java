package com.example.exploringgame;

import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class TimedMoveAction extends TimerTask {

    private float eyeX, eyeZ, centerX, centerZ;
    private float nextEyeX, nextEyeZ, nextCenterX, nextCenterZ;
    private int currentAngle;
    private int nextAngle;
    private int stepCounter = 0;
    private int fullAngle;
    float deltaX;
    float deltaZ;


    private final Handler mesHandler;
    private final boolean awake;
    private final Object lock;



    public TimedMoveAction(float cX, float cZ, float lX,float lZ,Handler mesHandler){
        eyeX = cX;
        eyeZ = cZ;
        centerX = lX;
        centerZ = lZ;

        nextEyeX = cX;
        nextEyeZ = cZ;
        nextCenterX = lX;
        nextCenterZ = lZ;

        nextAngle = 0;
        currentAngle = 0;
        fullAngle = 0;
        deltaX = Math.abs(nextEyeX - eyeX);
        deltaZ = Math.abs(nextEyeZ -eyeZ);
        this.mesHandler = mesHandler;
        lock = new Object();
        awake = true;
    }

    private boolean mustMove(){
        float absDiffCurrentX = Math.abs(nextEyeX-eyeX);
        float absDiffCurrentZ = Math.abs(nextEyeZ -eyeZ);
        float absDiffLookingX = Math.abs(nextCenterX - centerX);
        float absDiffLookingZ = Math.abs(nextCenterZ - centerZ);
        float absDiffAngle = Math.abs(nextAngle - currentAngle);
        float epsilon = 0.01f;
        Log.v("STATUSMOVEMENT", absDiffCurrentX +" " + absDiffLookingZ + " " + absDiffLookingX + " " + absDiffLookingZ + " " + absDiffAngle);


        if(absDiffCurrentX < epsilon && absDiffCurrentZ < epsilon && absDiffLookingX < epsilon && absDiffLookingZ < epsilon && absDiffAngle < epsilon){
            Log.v("STATUSMOVEMENT", "Sending message to not move");
            return false;
        }
        Log.v("STATUSMOVEMENT", "Sending message to MOVE");
        return true;
    }

    public void updateNextPos(float fX, float fZ, float fLX, float fLZ, int fAngle) {
        Log.v(" UPDATEFUTUREPOS", "angolo: " + fAngle);
        stepCounter = 0;
        nextEyeX = fX;
        nextEyeZ = fZ;
        nextCenterX = fLX;
        nextCenterZ = fLZ;
        nextAngle = fAngle;
        fullAngle = nextAngle - currentAngle;
        deltaX = Math.abs(nextEyeX - eyeX);
        deltaZ = Math.abs(nextEyeZ -eyeZ);
    }

    public void updateCurrentPos(float X, float Z, float LX, float LZ, int Angle){
        Log.v("UPDATECURRENTPOS", "angolo: " + Angle);
        eyeX = X;
        eyeZ = Z;
        centerX = LX;
        centerZ = LZ;
        currentAngle = Angle;
    }

    @Override
    public void run() {

        if(awake) {

            int NUM_STEP = 45;
            if (mustMove() && stepCounter < NUM_STEP -1) {
                // direction calc to determine if we have to go upwards or not
                stepCounter +=1;
                if (deltaX >0.000001f){ // X axis
                    Log.v("TIMEDMOVE","X axis movement");
                    if(nextEyeX > eyeX){
                        eyeX += deltaX/ NUM_STEP;
                        centerX += deltaX/ NUM_STEP;
                    }else{
                        eyeX -= deltaX/ NUM_STEP;
                        centerX -= deltaX/ NUM_STEP;

                    }
                    Log.v("X AXIS",eyeX + "  " + centerX);
                }
                if(deltaZ >  0.000001f){ // Z axis
                    Log.v("TIMEDMOVE","Z axis movement");
                    if(nextEyeZ > eyeZ){
                        eyeZ += deltaZ/ NUM_STEP;
                        centerZ += deltaZ/ NUM_STEP;
                    }else{
                        eyeZ -= deltaZ/ NUM_STEP;
                        centerZ -= deltaZ/ NUM_STEP;

                    }
                    Log.v("Z AXIS",eyeZ + "  " + centerZ + " future " + nextCenterZ);
                }
                if(Math.abs(nextAngle-currentAngle) > Math.abs(nextAngle)/ NUM_STEP){
                    Log.v("TIMEDMOVE","angle movement");
                    Log.v("ANGLE MOVEMENT","PRE: "+currentAngle + " - "+ nextAngle);
                    currentAngle +=Math.ceil(fullAngle/ NUM_STEP);
                    rotatePoint(fullAngle/ NUM_STEP);
                }

                if(stepCounter == NUM_STEP -1){
                    Log.v("END_STEP_COUNTER", "max PRE reached"+ stepCounter + " "+nextAngle+" " + currentAngle);
                    currentAngle = nextAngle;
                    eyeX  = nextEyeX;
                    eyeZ = nextEyeZ;
                    centerX = nextCenterX;
                    centerZ = nextCenterZ;
                    Log.v("END_STEP_COUNTER", "max POST reached "+ stepCounter + " "+nextAngle+" " + currentAngle);
                }


                Message m = mesHandler.obtainMessage();
                Bundle bundle = m.getData();
                bundle.putFloat("eyeX",eyeX);
                bundle.putFloat("eyeZ",eyeZ);
                bundle.putFloat("centerX",centerX);
                bundle.putFloat("centerZ",centerZ);
                bundle.putInt("angle", currentAngle);

                m.setData(bundle);
                mesHandler.sendMessage(m);

            }
        }else {
            Log.d("TIMEDMOVE","Sleeping...");
            synchronized(lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("TAG","Waking up...");
        }


    }

    public void rotatePoint(float angle){
        float x;
        float z;
        float  newX,newZ;

        float radiantAngle =  angle  * (float) Math.PI/180;
        float s = (float) Math.sin(radiantAngle);
        float c = (float) Math.cos(radiantAngle);
        //translate to origin
        x = centerX - eyeX;
        z = centerZ - eyeZ;

        //rotate point
        newX = x * c - z * s;
        newZ = x * s + z * c;

        //translate point back
        newX += eyeX;
        newZ += eyeZ;

        Log.v("TIME ANGLE", "PRE: ("+ centerX + "," + centerZ+")");

        centerX = newX;
        centerZ = newZ;

        Log.v("TIME ANGLE", "POST ccw: ("+ newX + "," + newZ+")");
    }

}
