package com.example.exploringgame;

import android.os.Message;
import android.util.Log;
import android.os.Handler;

public class MoveActionHandler extends Handler{

    private ExploringMazeGame2 gameInstance;

    public MoveActionHandler(ExploringMazeGame2 gameInstance){
        setGameInstance(gameInstance);
    }


    @Override
    public void handleMessage(Message msg) {

        if(getGameInstance() == null) return;

        if(msg!=null & msg.getData()!=null){
            Log.d("MOVEMENT_HANDLER","callingupdate visual");
            getGameInstance().updateCamera(msg.getData().getFloat("eyeX"),
                    msg.getData().getFloat("eyeZ"),
                    msg.getData().getFloat("centerX"),
                    msg.getData().getFloat("centerZ"),
                    msg.getData().getInt("angle"));
        }
    }

    public ExploringMazeGame2 getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(ExploringMazeGame2 gameInstance) {
        this.gameInstance = gameInstance;
    }
}
