package com.example.android.spacefighter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Gurpreet on 12/20/2017.
 */

//Actual game panel


public class GameView extends SurfaceView implements Runnable{

    //boolean variable to track if the game is playing or not
    volatile boolean playing;

    //game thread
    private Thread gameThread = null;

    //adding the player to this class
    private Player player;

    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    //Adding enemies object array
    private Enemy[] enemies;
    //Adding 3 enemies; u may increase the size
    private int enemyCount = 1;

    //created a reference of the class Friend
    private Friend friend;

    //Adding a stars list
    private ArrayList<Star> stars = new ArrayList<Star>();

    //defining a boom object to display blast
    private Boom boom;

    int screenX; //a screenX holder
    int countMisses; //to count the number of Misses
    boolean flag; //indicator that the enemy has just entered the game screen
    private boolean isGameOver; //an indicator if the game is Over

    //score holder
    int score;
    //hih scores holder
    int highScore[] = new int[4];
    //Shared Prefernces to store the High Scores
    SharedPreferences sharedPreferences;

    //the mediaplayer objects to configure the background music
    static MediaPlayer gameOnSound;
    final MediaPlayer killEnemySound;
    final MediaPlayer gameOverSound;

    //context to be used in onTouchEvent to cause the activity transition from GameAvtivity to MainActivity.
    Context context;

    //Class constructor
    public GameView(Context context, int screenX, int screenY) {
        super(context);

        //initializing context
        this.context = context;

        //initializing the media players for the game sounds
        gameOnSound = MediaPlayer.create(context,R.raw.gameon);
        killEnemySound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOverSound = MediaPlayer.create(context,R.raw.gameover);

        //starting the game music as the game starts
        gameOnSound.start();

        //initializing player object
        //this time also passing screen size to player constructor
        player = new Player(context, screenX, screenY);

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        //adding 100 stars; you may increase the number
        int starNums = 100;
        for(int i = 0; i<starNums; i++)
        {
            Star s = new Star(screenX,screenY);
            stars.add(s); //adding the random generated star in the arraylist star
        }

        //initializing enemy object array
        enemies = new Enemy[enemyCount];
        for(int i=0; i<enemyCount ; i++)
        {
            enemies[i] = new Enemy(context,screenX,screenY);
        }

        //initializing boom object
        boom = new Boom(context);

        //initializing friend object
        friend = new Friend(context,screenX,screenY);

        this.screenX = screenX;
        countMisses = 0;
        isGameOver = false;


        //setting the score to 0 initially
        score = 0;
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME",context.MODE_PRIVATE);
        //initializing the array high scores with the previous values
        highScore[0]=sharedPreferences.getInt("score1",0);
        highScore[1]=sharedPreferences.getInt("score2",0);
        highScore[2]=sharedPreferences.getInt("score3",0);
        highScore[3]=sharedPreferences.getInt("score4",0);
    }

    @Override
    public void run() {

        while(playing){

            //to update the frame / coordinates
            update();
            //to draw the frame
            draw();
            //to control the frames per second drawn
            control();
        }

    }

    private void draw() {
        //check if surface is valid
        if(surfaceHolder.getSurface().isValid()){
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.BLACK);

            //setting the paint color to white to draw the stars
            paint.setColor(Color.WHITE);

            //drawing all stars
            for(Star s : stars)
            {
                paint.setStrokeWidth(s.getstarWidth());
                canvas.drawPoint(s.getX(),s.getY(),paint);
            }

            //drawing the score on the game screen
            paint.setTextSize(30);
            canvas.drawText("Score: "+score,100,50,paint);

            //drawing the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);

            //drawing enemies
            for(int i=0;i<enemyCount;i++){
                canvas.drawBitmap(
                        enemies[i].getBitmap(),
                        enemies[i].getX(),
                        enemies[i].getY(),
                        paint);
            }

            //drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint);

            //drawing friend image
            canvas.drawBitmap(
                    friend.getBitmap(),
                    friend.getX(),
                    friend.getY(),
                    paint);

            //draw game Over when the game is over
            if (isGameOver){
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos = (int)((canvas.getHeight()/2) - ((paint.descent() + paint.ascent())/2));
                canvas.drawText("Game Over",canvas.getWidth()/2,yPos,paint);
            }

            //unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try{
            gameThread.sleep(17);

        }catch (InterruptedException e){
                e.printStackTrace();
        }
    }

    private void update() {

        //incrementing score as time passes
        score++;

        //updating player position
        player.update();

        //setting boom outside the screen
        boom.setX(-250);
        boom.setY(-250);

        //Updating the stars with player speed
        for(Star s : stars){
            s.update(player.getSpeed());
        }

        //updating the enemy coordinate with respect to player speed
        for(int i=0;i<enemyCount;i++)
        {
            //setting the flag true when the enemy just enters the screen
            if(enemies[i].getX() == screenX){
                flag=true;
            }

            enemies[i].update(player.getSpeed());
            //if collision occurs with player
            if(Rect.intersects(player.getDetectCollision(),enemies[i].getDetectCollision()))
            {
                //displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());

                //playing a sound at the collision between player and the enemy
                killEnemySound.start();
                //moving enemy outside the left edge
                enemies[i].setX(-200);
            }
            // the condition where player misses the enemy
            else{
                //if the enemy has just entered
                if(flag){
                    //if player's x coordinate > enemy's x coordinate
                    //i.e enemy has just crossed the player (missed!)
                    if(player.getDetectCollision().exactCenterX() >= enemies[i].getDetectCollision().exactCenterX()){
                        //increment countMisses
                        countMisses++;
                        //setting the flag false so that the else part is executed only when new enemy enters the screen
                        flag=false;

                        //if no of Misses is equal to 3, then game is over.
                        if(countMisses==3){
                            playing=false;
                            isGameOver=true;

                            //stopping the gameon music
                            gameOnSound.stop();
                            //play the game over sound
                            gameOverSound.start();

                            //Assigning the scores to the highscore integer array
                            for(int j =0;j<4;j++)
                            {
                                if(highScore[j]<score){
                                    final int finalI = j;
                                    highScore[j]=score;
                                    break;
                                }
                            }

                            //storing the scores through shared Preferences
                            SharedPreferences.Editor e = sharedPreferences.edit();
                            for(int k=0;k<4;k++){
                                int j = k+1;
                                e.putInt("score"+j,highScore[k]);
                            }
                            e.apply();


                        }
                    }
                }
            }
        }
        friend.update(player.getSpeed());
        //checking for a collision between player and a friend
        if(Rect.intersects(player.getDetectCollision(),friend.getDetectCollision())){

            //displaying boom image at collision
            boom.setX(friend.getX());
            boom.setY(friend.getY());
            //setting playing false to stop the game
            playing = false;
            //setting the isGameOver true as the game is over
            isGameOver = true;

            //stopping the gameon music
            gameOnSound.stop();
            //play the game over sound
            gameOverSound.start();

            //Assigning the scores to the highscore integer array
            for(int j =0;j<4;j++)
            {
                if(highScore[j]<score){
                    final int finalI = j;
                    highScore[j]=score;
                    break;
                }
            }

            //storing the scores through shared Preferences
            SharedPreferences.Editor e = sharedPreferences.edit();
            for(int k=0;k<4;k++){
                int j = k+1;
                e.putInt("score"+j,highScore[k]);
            }
            e.apply();


        }
    }


    public void pause(){
        //when the game is paused
        //setting the variable to false
        playing = false;

        try{
            //stop the thread;
            gameThread.join();

        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    public void resume(){
        //when the game is resumed
        //starting the thread again
        playing=true;

        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){

            case MotionEvent.ACTION_UP:
                //When the user presses on the screen
                player.stopBoosting();
                break;

            case MotionEvent.ACTION_DOWN:
                //When the user releases the screen
                player.setBoosting();
                break;
        }

        //if the game's over, tappin on game Over screen sends you to MainActivity
        if(isGameOver)
        {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                context.startActivity(new Intent(context,MainActivity.class));
            }
        }
        return true;
    }

    //stop the music on exit
    public static void stopMusic(){
        gameOnSound.stop();
    }

}
