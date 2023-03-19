package com.example.exploringgame;

import java.util.Random;

public class Maze {
    private int[][] maze;

    private float currentPosX;
    private float currentPosY;
    private float currentPosZ;
    private float lookingPosX;
    private float lookingPosY;
    private float lookingPosZ;

    public Maze(int MAZE_SIZE) {
        generateMaze(MAZE_SIZE);
    }

    public void generateMaze(int MAZE_SIZE) {
        maze = new int[MAZE_SIZE][MAZE_SIZE];
        Random random = new Random();

        setInnerWalls(MAZE_SIZE);
        setBorderWalls(MAZE_SIZE);

        int entranceX = random.nextInt(MAZE_SIZE - 2) + 1;
        int exitX = random.nextInt(MAZE_SIZE - 2) + 1;

        maze[entranceX][0] = 2;
        maze[exitX][MAZE_SIZE - 1] = 3;

        createPath(entranceX, exitX, MAZE_SIZE);

        setCurrentPosX(entranceX*2);
        setCurrentPosY(0f);
        setCurrentPosZ(2f);

        setLookingPosX(entranceX*2);
        setLookingPosY(0.0f);
        setLookingPosZ(entranceX*2 + 2.5f);
    }

    private void setBorderWalls(int MAZE_SIZE) {
        for (int i = 0; i < MAZE_SIZE; i++) {
            maze[0][i] = 1;
            maze[MAZE_SIZE - 1][i] = 1;
            maze[i][0] = 1;
            maze[i][MAZE_SIZE - 1] = 1;
        }
    }

    private void setInnerWalls(int MAZE_SIZE) {
        Random random = new Random();
        for (int x = 1; x < MAZE_SIZE - 1; x++) {
            for (int y = 1; y < MAZE_SIZE - 1; y++) {
                if (random.nextDouble() < 0.3) {
                    maze[x][y] = 1;
                }
            }
        }
    }

    public void createPath(int entranceX, int exitX, int MAZE_SIZE) {
        Random random = new Random();

        int centerToMove = random.nextInt(MAZE_SIZE-2);

        for(int i = 1; i <= centerToMove; i++){
            maze[entranceX][i] = 0;
        }

        boolean upwards = entranceX > exitX;

        if(upwards){
            for(int i = entranceX; i != exitX; i--){
                maze[i][centerToMove] = 0;
            }
        }else{
            for(int i = entranceX; i != exitX; i++){
                maze[i][centerToMove] = 0;
            }
        }

        for(int i = centerToMove; i <= MAZE_SIZE-2; i++){
            maze[exitX][i] = 0;
        }


    }

    public int[][] getMaze() {
        return maze;
    }

    public float getCurrentPosX() {
        return currentPosX;
    }

    public void setCurrentPosX(float currentPosX) {
        this.currentPosX = currentPosX;
    }

    public float getCurrentPosY() {
        return currentPosY;
    }

    public void setCurrentPosY(float currentPosY) {
        this.currentPosY = currentPosY;
    }

    public float getCurrentPosZ() {
        return currentPosZ;
    }

    public void setCurrentPosZ(float currentPosZ) {
        this.currentPosZ = currentPosZ;
    }

    public float getLookingPosX() {
        return lookingPosX;
    }

    public void setLookingPosX(float lookingPosX) {
        this.lookingPosX = lookingPosX;
    }

    public float getLookingPosY() {
        return lookingPosY;
    }

    public void setLookingPosY(float lookingPosY) {
        this.lookingPosY = lookingPosY;
    }

    public float getLookingPosZ() {
        return lookingPosZ;
    }

    public void setLookingPosZ(float lookingPosZ) {
        this.lookingPosZ = lookingPosZ;
    }
}