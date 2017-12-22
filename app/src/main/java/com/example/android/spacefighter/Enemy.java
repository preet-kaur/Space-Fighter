package com.example.android.spacefighter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by Gurpreet on 12/21/2017.
 */

public class Enemy {

    //bitmap for the enemy
    //we have already pasted the bitmap in the drawable folder
    private Bitmap bitmap;

    //x & y coordinates
    private int x;
    private int y;
    //enemy speed
    private int speed = 1;

    //min & max coordinates to keep enemy inside the screen
    private int maxX;
    private int minX;
    private int maxY;
    private int minY;

    //creating a rect object
    private Rect detectCollision;

    public Enemy(Context context, int screenX, int screenY){
        //getting bitmap from drawable resource
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);

        //initializing min and max coordinates
        maxX = screenX;
        maxY = screenY;

        minX = 0;
        minY = 0;

        //generating a random coordinate to add enemy
        Random generator = new Random();
        speed = generator.nextInt(6) + 10;
        x = screenX;
        y = generator.nextInt(maxY) - bitmap.getHeight();

        //initializing rect object
        detectCollision = new Rect(x,y,bitmap.getWidth(),bitmap.getHeight());
    }

    //updating speed and position of enemy
    public void update(int playerSpeed){

        //decreasing x coordinate so that enemy will move right to left
        x-=playerSpeed;
        x-=speed;

        //if the enemy reaches the left edge
        if(x < minX - bitmap.getWidth() ){
            //adding the enemy again to the right edge
            Random generator = new Random();
            speed = generator.nextInt(10) + 10;
            x = maxX;
            y = generator.nextInt(maxY) - bitmap.getHeight();
        }

        //Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight();
    }

    //adding a setter to x coordinate so that we can change it after collision
    public void setX(int x){
        this.x = x;
    }

    //one more getter for getting the rect object
    public Rect getDetectCollision(){
        return detectCollision;
    }

    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }
}
