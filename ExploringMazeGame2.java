package com.example.exploringgame;

import static android.opengl.GLES10.GL_TEXTURE0;
import static android.opengl.GLES10.GL_TEXTURE9;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_NEAREST;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE3;
import static android.opengl.GLES20.GL_TEXTURE4;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFrontFace;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES30.glBindVertexArray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.exploringgame.utils.PlyObject;
import com.example.exploringgame.utils.ShaderCompiler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Timer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ExploringMazeGame2 extends BasicRenderer{

    private int[] VAO;
    private int shaderHandle;
    private int MVPloc;

    private final float[] viewM;
    private final float[] modelM;
    private final float[] projM;
    private final float[] MVP;
    private final float[] temp;

    private final float[] viewMiniM;
    private final float[] modelMiniM;
    private final float[] projMiniM;
    private final float[] MVPMini;
    private final float[] tempMini;

    private int[] texObjId;
    private int[] texObjMinimapId;
    private final int drawMode;
    private int countFacesToElement;
    private int countFacesToElementMap;
    private int countFacesToElementTriangle;

    private float eyeX = 5f;
    private float eyeY = 0f;
    private float eyeZ = 5f;
    private float centerX = 5f;
    private float centerY = 0f;
    private float centerZ = 5;
    private float nextEyeX, nextEyeZ, nextCenterX, nextCenterZ;

    private int angle = 0;
    private int nextAngle;

    private float miniMapX;
    private float miniMapY;
    private float miniMapZ;
    private float miniMapDim;

    private static final int MAZE_SIZE = 20;
    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private static final int STEP = 2;

    private Maze maze;
    private GestureDetector gestureDetector;
    private boolean endGameStatus;

    private TimedMoveAction timedMovAction;
    private MoveActionHandler moveHandler;
    private Timer timer;

    public ExploringMazeGame2(){
        drawMode = GL_TRIANGLES;

        viewM = new float[16];
        modelM = new float[16];
        projM = new float[16];
        MVP = new float[16];
        temp = new float[16];
        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(projM, 0);
        Matrix.setIdentityM(MVP, 0);


        viewMiniM = new float[16];
        modelMiniM = new float[16];
        projMiniM = new float[16];
        MVPMini = new float[16];
        tempMini = new float[16];
        Matrix.setIdentityM(viewMiniM, 0);
        Matrix.setIdentityM(modelMiniM, 0);
        Matrix.setIdentityM(projMiniM, 0);
        Matrix.setIdentityM(MVPMini, 0);

        setTimer(new Timer());
        setMoveHandler(new MoveActionHandler(this));
        setTimedMovAction(new TimedMoveAction(eyeX, eyeZ, centerX,
                centerZ, getMoveHandler()));
        getTimer().scheduleAtFixedRate(getTimedMovAction(),10,16);

        createMaze();
        setVars();
        Matrix.setLookAtM(viewM, 0, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ, 0f, 1.0f, 0.0f);
    }

   @SuppressLint("ClickableViewAccessibility")
   @Override
   public void setContextAndSurface(Context context, GLSurfaceView surface) {
       super.setContextAndSurface(context, surface);

       gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               float absDiffCurrentX = Math.abs(nextEyeX-eyeX);
               float absDiffCurrentZ = Math.abs(nextEyeZ-eyeZ);
               float absDiffLookingX = Math.abs(nextCenterX - centerX);
               float absDiffLookingZ = Math.abs(nextCenterZ - centerZ);
               float absDiffAngle = Math.abs(nextAngle - angle);

               float deltaX = e2.getX() - e1.getX();
               float deltaY = e2.getY() - e1.getY();
               if(absDiffCurrentX > 0 || absDiffCurrentZ > 0 || absDiffLookingX > 0 || absDiffLookingZ > 0 || absDiffAngle > 0) {
                   Log.v("ON_TOUCH","Touch action blocked");
               }else{
                   if (Math.abs(deltaX) > Math.abs(deltaY)) {
                       if (Math.abs(deltaX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                           rotateCamera(deltaX > 0);
                           return true;
                       }
                   } else {
                       if (Math.abs(deltaY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                           // bottom swipe -> go forward
                           // upward swipe -> go backwards
                           move(deltaY < 0);
                       }
                       return true;
                   }
                   return false;
               }

               return true;
           }
       });

       surface.setOnTouchListener((v, event) -> {
           gestureDetector.onTouchEvent(event);
           return true;
       });
   }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        float aspect = ((float) width) / ((float) (height == 0 ? 1 : height));

        // main view
        Matrix.perspectiveM(projM, 0, 45f, aspect, 0.1f, 100f);
        Matrix.setLookAtM(viewM, 0, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                0, 1, 0);

        // minimap
        Matrix.orthoM(projMiniM,0,-1f*aspect, aspect,-1,1,-0.1f,100f);

        Matrix.setLookAtM(viewMiniM, 0, 10, 0,  0,
                0, 0, 0,
                0, 1, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        String vertexSrc = "#version 300 es\n" +
                "\n" +
                "layout(location = 1) in vec3 vPos;\n" +
                "layout(location = 2) in vec2 texCoord;\n" +
                "uniform mat4 MVP;\n" +
                "uniform vec2 texScaling;\n" +
                "out vec2 varyingTexCoord;\n" +
                "\n" +
                "void main(){\n" +
                "varyingTexCoord = texCoord * texScaling;\n" +
                "gl_Position = MVP * vec4(vPos,1);\n" +
                "}";

        String fragmentSrc = "#version 300 es\n" +
                "\n" +
                "precision mediump float;\n" +
                "\n" +
                "uniform sampler2D tex;\n" +
                "in vec2 varyingTexCoord;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() {\n" +
                "fragColor = texture(tex, varyingTexCoord);\n" +
                "}";

        shaderHandle = ShaderCompiler.createProgram(vertexSrc, fragmentSrc);

        if(shaderHandle < 0) {
            Log.v(TAG,"Error in shader(s) compile. Check SHADER_COMPILER logcat tag. Exiting");
            System.exit(-1);
        }

        InputStream is;
        float[] vertices=null;
        int[] indices=null;

        try {
            is = context.getAssets().open("cube.ply");
            PlyObject po = new PlyObject(is);
            po.parse();
            vertices = po.getVertices();
            indices = po.getIndices();

        }catch(IOException | NumberFormatException e){
            e.printStackTrace();
        }

        FloatBuffer vertexData =
                ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);

        IntBuffer indexData =
                ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexData.put(indices);
        indexData.position(0);

        countFacesToElement = indices.length;

        int[] VBO = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBO, 0);

        VAO = new int[3]; //one VAO to bind both vpos and color
        // 0 = Cube
        // 1 = Plane
        // 2 = Triangle

        GLES30.glGenVertexArrays(3, VAO, 0);

        GLES30.glBindVertexArray(VAO[0]);
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexData.capacity(),
                vertexData, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5*Float.BYTES, 0); //vpos
        glVertexAttribPointer(2,2,GL_FLOAT, false, 5*Float.BYTES, 3*Float.BYTES); //color/normal
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexData.capacity(), indexData,
                GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);

        float[] verticesMap = new float[] {
                -1,0,-1, 0,1,
                1,0,1, 1,0,
                1,0,-1, 1,1,
                -1,0,1, 0,0
        };

        int[] indicesMap=new int[]{
                0, 1, 2,
                0, 3, 1
        };

        FloatBuffer vertexMap =
                ByteBuffer.allocateDirect(verticesMap.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexMap.put(verticesMap);
        vertexMap.position(0);

        IntBuffer indexMap =
                ByteBuffer.allocateDirect(indicesMap.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexMap.put(indicesMap);
        indexMap.position(0);

        countFacesToElementMap = indicesMap.length;

        int[] VBOMap = new int[2];
        glGenBuffers(2, VBOMap, 0);

        glBindVertexArray(VAO[1]);
        glBindBuffer(GL_ARRAY_BUFFER, VBOMap[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexMap.capacity(),
                vertexMap, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES*5, 0); // vpos
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*5, 3*Float.BYTES); // texcoord
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOMap[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexMap.capacity(), indexMap,
                GL_STATIC_DRAW);

        glBindVertexArray(0);


        // TRIANGLE -> minimap player
        float[] verticesTriangle = new float[]{
                -0.5f, -1f, 1f, 1f,
                0.5f, -1f,  1f, 0f,
                0f, 1f,   0f, 0f,
        };

        int[] indicesTriangle=new int[]{
                0, 1, 2
        };
        FloatBuffer vertexDataTriangle =
                ByteBuffer.allocateDirect(verticesTriangle.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexDataTriangle.put(verticesTriangle);
        vertexDataTriangle.position(0);

        IntBuffer indexDataTriangle =
                ByteBuffer.allocateDirect(indicesTriangle.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexDataTriangle.put(indicesTriangle);
        indexDataTriangle.position(0);

        countFacesToElementTriangle = indicesTriangle.length;

        int[] VBOTriangle = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBOTriangle, 0);

        GLES30.glBindVertexArray(VAO[2]);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTriangle[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexDataTriangle.capacity(),
                vertexDataTriangle, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Float.BYTES*4, 0); //vpos
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*4, 2*Float.BYTES); //texcoord
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOTriangle[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexDataTriangle.capacity(), indexDataTriangle,
                GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);


        MVPloc = glGetUniformLocation(shaderHandle, "MVP");
        int texUnit = glGetUniformLocation(shaderHandle, "tex");
        int texScalingLoc = glGetUniformLocation(shaderHandle, "texScaling");

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GLES20.GL_CCW);


        texObjId = new int[5];
        glGenTextures(5, texObjId, 0);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled=false;

        // BLOCK
        Bitmap bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.old_brick,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,texObjId[0]);
        glUseProgram(shaderHandle);
        glUniform1i(texUnit,0); //0 because active texture is GL_TEXTURE0.
        glUniform2f(texScalingLoc, 1,1); //No scaling...
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);

        bitmap.recycle();


        // ENTRANCE DOOR
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.old_door,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId[1]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D,texObjId[1]);
        glUseProgram(shaderHandle);
        glUniform1i(texUnit,1); //0 because active texture is GL_TEXTURE0.
        glUniform2f(texScalingLoc, 1,1); //No scaling...
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);

        bitmap.recycle();


        // EXIT
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.old_exit,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        glBindTexture(GL_TEXTURE_2D, texObjId[2]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D,texObjId[2]);
        glUseProgram(shaderHandle);
        glUniform1i(texUnit,2); //0 because active texture is GL_TEXTURE0.
        glUniform2f(texScalingLoc, 1,1); //No scaling...
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);

        bitmap.recycle();


        // FLOOR
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.grass,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        glBindTexture(GL_TEXTURE_2D, texObjId[3]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D,texObjId[3]);
        glUseProgram(shaderHandle);
        glUniform1i(texUnit,3); //0 because active texture is GL_TEXTURE0.
        glUniform2f(texScalingLoc, 1,1); //No scaling...
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);

        bitmap.recycle();


        // SKY
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.sky,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId[4]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D,texObjId[4]);
        glUseProgram(shaderHandle);
        glUniform1i(texUnit,4); //0 because active texture is GL_TEXTURE0.
        glUniform2f(texScalingLoc, 1,1); //No scaling...
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);

        bitmap.recycle();


        // MINIMAP
        texObjMinimapId = new int[5];
        glGenTextures(5, texObjMinimapId, 0);

        // PLAYER
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.player,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjMinimapId[4]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjMinimapId[4]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 9); //0 because active texture is GL_TEXTURE3.
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);

        bitmap.recycle();

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // MAIN
        // FLOOR
        Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);
        Matrix.setIdentityM(modelM,0);
        Matrix.translateM(modelM,0,MAZE_SIZE-1,-1,MAZE_SIZE-1);
        Matrix.rotateM(modelM,0,180,1,0,0);
        Matrix.scaleM(modelM,0,MAZE_SIZE,0,MAZE_SIZE);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);
        glBindTexture(GL_TEXTURE_2D, texObjId[3]); // FLOOR

        glUseProgram(shaderHandle);
        glBindVertexArray(VAO[0]);

        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
        glUseProgram(0);

        // SKY
        Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);
        Matrix.setIdentityM(modelM,0);
        Matrix.translateM(modelM,0,MAZE_SIZE-1,1,MAZE_SIZE-1);
        Matrix.rotateM(modelM,0,180,1,0,0);
        Matrix.scaleM(modelM,0,MAZE_SIZE,0,MAZE_SIZE);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);
        glBindTexture(GL_TEXTURE_2D, texObjId[4]); // FLOOR

        glUseProgram(shaderHandle);
        glBindVertexArray(VAO[0]);

        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
        glUseProgram(0);

        for(int x = 0; x < MAZE_SIZE; x+=1){
            for(int y = 0; y < MAZE_SIZE; y+=1){
                Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);

                Matrix.setIdentityM(modelM,0);
                Matrix.translateM(modelM,0,x*STEP,0,y*STEP);
                Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);

                if(maze.getMaze()[x][y] == 1){
                    glBindTexture(GL_TEXTURE_2D, texObjId[0]); // BLOCK
                    glUseProgram(shaderHandle);
                    glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }
                else if(maze.getMaze()[x][y] == 2){
                    glBindTexture(GL_TEXTURE_2D, texObjId[1]); // DOOR
                    glUseProgram(shaderHandle);
                    glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }
                else if(maze.getMaze()[x][y] == 3){
                    glBindTexture(GL_TEXTURE_2D, texObjId[2]); // EXIT
                    glUseProgram(shaderHandle);
                    glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }
            }
        }


        // MINIMAP
        for(int x = 0; x < MAZE_SIZE; x+=1){
            for(int y = 0; y < MAZE_SIZE; y+=1){
                Matrix.multiplyMM(tempMini, 0, projMiniM, 0, viewMiniM, 0);

                Matrix.setIdentityM(modelMiniM,0);
                Matrix.translateM(modelMiniM,0,miniMapX,
                        miniMapY - miniMapDim*x*2,miniMapZ+ miniMapDim*y*2);
                Matrix.scaleM(modelMiniM, 0, 0, miniMapDim, miniMapDim);
                Matrix.rotateM(modelMiniM, 0, 180, 1,1,0);
                Matrix.multiplyMM(MVPMini, 0, tempMini, 0, modelMiniM, 0);

                if(maze.getMaze()[x][y] == 1){
                    glBindTexture(GL_TEXTURE_2D, texObjId[0]); // BLOCK
                }
                else if(maze.getMaze()[x][y] == 2){
                    glBindTexture(GL_TEXTURE_2D, texObjId[1]); // DOOR
                }
                else if(maze.getMaze()[x][y] == 3){
                    glBindTexture(GL_TEXTURE_2D, texObjId[2]); // EXIT
                    Matrix.rotateM(modelMiniM, 0, -90, 0,1,0);
                    Matrix.multiplyMM(MVPMini, 0, tempMini, 0, modelMiniM, 0);
                }
                else{
                    glBindTexture(GL_TEXTURE_2D, texObjId[3]); // FLOOR
                }
                glUseProgram(shaderHandle);
                glBindVertexArray(VAO[1]);

                glUniformMatrix4fv(MVPloc, 1, false, MVPMini, 0);
                glDrawElements(drawMode, countFacesToElementMap, GL_UNSIGNED_INT, 0);
                glBindVertexArray(0);
                glBindTexture(GL_TEXTURE_2D,0);
                glUseProgram(0);
            }
        }

        Matrix.multiplyMM(tempMini, 0, projMiniM, 0, viewMiniM, 0);

        Matrix.setIdentityM(modelMiniM,0);
        Matrix.translateM(modelMiniM,0,miniMapX,
                miniMapY - miniMapDim*eyeX,miniMapZ+ miniMapDim*eyeZ);
        Matrix.scaleM(modelMiniM, 0, 0, miniMapDim, miniMapDim);
        Matrix.rotateM(modelMiniM, 0, 90, 0,1,0);
        Matrix.rotateM(modelMiniM, 0, -angle+90,0,0,1);
        Matrix.multiplyMM(MVPMini, 0, tempMini, 0, modelMiniM, 0);

        glBindTexture(GL_TEXTURE_2D, texObjMinimapId[4]);
        glUseProgram(shaderHandle);
        glBindVertexArray(VAO[2]);
        glUniformMatrix4fv(MVPloc, 1, false, MVPMini, 0);
        glDrawElements(GL_TRIANGLES, countFacesToElementTriangle,GL_UNSIGNED_INT,0); //num of indices, not vertices!

        glBindTexture(GL_TEXTURE_2D,0);
        GLES30.glBindVertexArray(0);
        glUseProgram(0);
    }


    public void move(boolean forwardDir){

        float deltaX = centerX - eyeX;
        float deltaZ = centerZ - eyeZ;

        float predictedX = Float.MAX_VALUE;
        float predictedZ = Float.MAX_VALUE;
        float oldCenterX = centerX;
        float oldCenterZ = centerZ;

        if (deltaX != 0){ // X axis
            if ( deltaX > 0){ // facing right
                if(forwardDir){
                    predictedX = eyeX + STEP;
                    nextCenterX = centerX + STEP;
                }else{
                    predictedX = eyeX - STEP;
                    nextCenterX = centerX - STEP;
                }
            }
            else{ // facing left
                if(forwardDir){
                    predictedX = eyeX - STEP;
                    nextCenterX = centerX - STEP;
                }else{
                    predictedX = eyeX + STEP;
                    nextCenterX = centerX + STEP;
                }
            }
        }else if(deltaZ != 0){ // Z axis
            if(deltaZ > 0){ // facing down
                if(forwardDir){
                    predictedZ = eyeZ + STEP;
                    nextCenterZ = centerZ + STEP;
                }else{
                    predictedZ = eyeZ - STEP;
                    nextCenterZ = centerZ - STEP;
                }
            }else{ // facing up
                if(forwardDir){
                    predictedZ = eyeZ - STEP;
                    nextCenterZ = centerZ - STEP;
                }else{
                    predictedZ = eyeZ + STEP;
                    nextCenterZ = centerZ + STEP;
                }
            }
        }

        if(predictedX != Float.MAX_VALUE){
            if(!checkCollision(predictedX, eyeZ)) {
                Log.v("INFO-NOCOL", "NO-COLL X: " + nextEyeX);
                nextEyeX = predictedX;
                Log.v("INFO-NOCOL", "NO-COLL X: " + nextEyeX);

                getTimedMovAction().updateCurrentPos(eyeX, eyeZ, centerX, centerZ, angle);
                getTimedMovAction().updateNextPos(nextEyeX, nextEyeZ, nextCenterX, nextCenterZ, nextAngle);
            }
            else{
                Log.v("INFO-COL", "COLLISION X: " + nextCenterX);
                nextCenterX = oldCenterX;
                Log.v("INFO-COL", "COLLISION X: " + nextCenterX);
            }
        }else if(predictedZ != Float.MAX_VALUE) {
            if(!checkCollision(eyeX, predictedZ)) {
                Log.v("INFO-NOCOL", "NO-COLL Z: " + nextEyeZ);
                nextEyeZ = predictedZ;
                Log.v("INFO-NOCOL", "NO-COLL Z: " + nextEyeZ);

                getTimedMovAction().updateCurrentPos(eyeX, eyeZ, centerX, centerZ, angle);
                getTimedMovAction().updateNextPos(nextEyeX, nextEyeZ, nextCenterX, nextCenterZ, nextAngle);

            }else{
                Log.v("INFO-COL", "COLLISION Z: " + nextCenterZ);
                nextCenterZ = oldCenterZ;
                Log.v("INFO-COL", "COLLISION Z: " + nextCenterZ);
            }
        }
    }

    private void rotateCamera(boolean clockwise) {

        Log.v("UPDATEPOSMAIN", "pos: " + eyeX + " "+  eyeZ + " " + centerX + " " + centerZ);

        if (clockwise) {
            nextCenterX = eyeX + eyeZ - centerZ;
            nextCenterZ = centerX + eyeZ - eyeX;
        } else {
            nextCenterX = centerZ - eyeZ + eyeX;
            nextCenterZ = -(centerX - eyeX) + eyeZ;
        }
        Log.v("UPDATEPOSMAIN", "pos: " + nextCenterX + " " + nextCenterZ);

        nextEyeX = eyeX;
        nextEyeZ = eyeZ;

        Log.v("UPDATEPOSMAIN", "pos: " + nextEyeX + " " + nextEyeZ);
        nextAngle = clockwise ? angle + 90 : angle - 90;

        getTimedMovAction().updateCurrentPos(eyeX, eyeZ, centerX, centerZ, angle);
        getTimedMovAction().updateNextPos(nextEyeX, nextEyeZ, nextCenterX, nextCenterZ, nextAngle);
    }

    private boolean checkCollision(float X, float Z){

        Log.v("INFO", "X: " + X + " | " + centerX);
        Log.v("INFO", "Z: " + Z + " | " + centerZ);
        int block = maze.getMaze()[(int) (X/2)][(int) (Z/2)];
        Log.v("INFO", "BLOCK: " + block);
        switch (block){
            case 0:
                Log.v("INFO-COL", "NO COLLISION");
                return false;
            case 1:
                Log.v("INFO-COL", "COLLISION");
                return true;
            case 2:
                endGameStatus = true;
                Log.v("INFO-COL", "COLLISION");
                Toast.makeText(context, "Checking new pipe...", Toast.LENGTH_SHORT).show();
                return false;
            case 3:
                endGameStatus = true;
                Log.v("INFO-COL", "NO COLLISION");
                Toast.makeText(context, "Finished maze!", Toast.LENGTH_SHORT).show();
                return false;
        }
            return true;
    }

    public void updateCamera(float eX, float eZ, float centX,
                             float centZ, int ang){

        Log.v("INFO", "X: " + eX);
        Log.v("INFO", "Z: " + eZ);
        Log.v("INFO", "centX: " + centX);
        Log.v("INFO", "centZ: " + centZ);
        Log.v("INFO", "angle: " + ang);
        eyeX = eX;
        eyeZ = eZ;
        centerX = centX;
        centerZ = centZ;
        if(ang == 360 || ang == -360){
            Log.v("UPDATEVISUAL", "reset angolo");
            ang = 0;
            nextAngle = 0;
        }

        angle = ang;

        Matrix.setLookAtM(viewM, 0, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                0, 1, 0);

        if(endGameStatus) {
            newGame();
        }
    }

    private void createMaze() {
        setMaze(new Maze(MAZE_SIZE));
    }

    private void newGame(){
        setMaze(new Maze(MAZE_SIZE));
        setVars();
        getTimedMovAction().updateCurrentPos(eyeX,eyeZ,centerX,centerZ,angle);
        getTimedMovAction().updateNextPos(nextEyeX,nextEyeZ,nextCenterX,nextCenterZ,nextAngle);

        Matrix.setLookAtM(viewM, 0, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ, 0f, 1.0f, 0.0f);
    }

    private void setMaze(Maze maze) {
        this.maze = maze;
    }

    public void setVars(){
        eyeX = maze.getCurrentPosX();
        eyeY = maze.getCurrentPosY();
        eyeZ = maze.getCurrentPosZ();
        centerX = maze.getLookingPosX();
        centerY = maze.getLookingPosY();
        centerZ = maze.getLookingPosZ();

        nextEyeX = eyeX;
        nextEyeZ = eyeZ;
        nextCenterX = centerX;
        nextCenterZ = centerZ;
        angle = 0;
        nextAngle = angle;

        miniMapX = 0f;
        miniMapY = 0.03f;
        miniMapZ = -2.01f;
        miniMapDim = (1.0f/2.0f)/(MAZE_SIZE-1);

        endGameStatus = false;
    }

    public TimedMoveAction getTimedMovAction() {
        return timedMovAction;
    }

    public void setTimedMovAction(TimedMoveAction timedMovAction) {
        this.timedMovAction = timedMovAction;
    }

    public MoveActionHandler getMoveHandler() {
        return moveHandler;
    }

    public void setMoveHandler(MoveActionHandler moveHandler) {
        this.moveHandler = moveHandler;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

}
