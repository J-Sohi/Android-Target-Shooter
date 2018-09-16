package jasdip.a4443project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class GameView extends View {

    private static final String TAG = "myDebug";
    Paint paint = new Paint();
    Random random = new Random();
    int controlMode, targetSize, hits, misses;
    private long startTime, hitTime, lastHit, endTime;
    private Drawable crosshair;
    private int activePointerId = -1;
    private float lastTouchX, lastTouchY, xPosition, yPosition;
    private boolean targetSpawn = true;
    private GestureDetector gestureDetector;
    DateFormat formatter = new SimpleDateFormat("ss.S");

    public GameView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public GameView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    public GameView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context){
        crosshair = context.getResources().getDrawable(R.drawable.ic_crosshair);
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        startTime = System.currentTimeMillis();
        lastHit = startTime;
    }

    public void setOptions(int cmode, int tsize){
        controlMode = cmode;
        targetSize = tsize;
    }

    public String[] getResults(){
        endTime = System.currentTimeMillis();
        Date endDate = new Date(endTime-startTime);
        String endFormatted = formatter.format(endDate);

        float accuracy = (float)hits / (float)(hits + misses);
        Log.d(TAG, "Hits: " + hits + " Misses: " + misses + " Accuracy: " + accuracy);

        String[] results = {endFormatted, String.format("%.1f", 100*accuracy)};
        return results;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //if target needs to be spawned, randomize circle position
        if (targetSpawn) {
            xPosition = getWidth() * random.nextFloat();
            yPosition = getHeight() * random.nextFloat();
            targetSpawn = false;
        }

        //paint stuff and draw circle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle(xPosition, yPosition, targetSize, paint);

        //draw crosshair
        int[] bounds = {getWidth()/2 - crosshair.getIntrinsicWidth()/2,
                getHeight()/2  - crosshair.getIntrinsicWidth()/2,
                getWidth()/2 + crosshair.getIntrinsicWidth()/2,
                getHeight()/2+crosshair.getIntrinsicHeight()/2};
        crosshair.setBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
        crosshair.draw(canvas);

        //Red rect in middle of crosshair used for debuging
        //canvas.drawRect(getWidth()/2-5,getHeight()/2-5,getWidth()/2+5,getHeight()/2+5, paint);
        //Log.d(TAG, "x=" + xPosition + " y=" + yPosition);
    }

    public void joystickMove(int ang, int str){
        // use this position to compute the 'delta' for the image
        final double dx = str * Math.cos(Math.toRadians(ang));
        final double dy = str * Math.sin(Math.toRadians(ang));

        //move positions
        xPosition -= 0.1 * dx;
        yPosition += 0.1 * dy;
        checkBounds();
    }

    //stop target from leaving screen and invalidate
    public void checkBounds(){
        //check if target leaves bounds
        if (xPosition < 0)
            xPosition = 0;
        if (yPosition < 0)
            yPosition = 0;
        if (xPosition > getWidth())
            xPosition = getWidth();
        if (yPosition > getHeight())
            yPosition = getHeight();

        //refresh canvas
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        gestureDetector.onTouchEvent(me);

        final int action = me.getAction();
        switch (action & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN: {
                final float x = me.getX();
                final float y = me.getY();
                // save the ID of this pointer
                activePointerId = me.getPointerId(0);
                lastTouchX = x;
                lastTouchY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if(controlMode == 1) {
                    // find the index of the active pointer and fetch its position
                    final int pointerIndex = me.findPointerIndex(activePointerId);
                    final float x = me.getX(pointerIndex);
                    final float y = me.getY(pointerIndex);

                    // use this position to compute the 'delta' for the image
                    final double dx = x - lastTouchX;
                    final double dy = y - lastTouchY;
                    xPosition -= 1.8 * dx;
                    yPosition -= 1.8 * dy;
                    checkBounds();

                    // the current pointer position becomes the last position
                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = -1;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                // extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = me.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
					/*
					 * This was our active pointer going up. Choose a new active pointer and adjust
					 * accordingly. To understand why this code is necessary, read the comments
					 * above -- where activePointerId is declared.
					 */
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = me.getX(newPointerIndex);
                    lastTouchY = me.getY(newPointerIndex);
                    activePointerId = me.getPointerId(newPointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                break;
            }
        }
        invalidate();
        return true;
    }

    // ============================================================================================
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent me)
        {
            //get crosshair center
            final float crossX = getWidth()/2;
            final float crossY = getHeight()/2;

            //check if crosshair center intersects circle
            final float inside = (crossX - xPosition)*(crossX - xPosition) + (crossY - yPosition)*(crossY - yPosition);
            if (inside < (targetSize*targetSize)){
                //record hits and times, times not used in results
                hits++;
                hitTime = System.currentTimeMillis();
                Date hitDate = new Date(hitTime-lastHit);
                String hitFormatted = formatter.format(hitDate);
                lastHit = hitTime;
                Log.d(TAG, "Hit time:" + hitFormatted);
                crosshair.setTint(Color.GREEN);

                //end game if 10 targets hit
                if(hits == 10) {
                    GameActivity context = (GameActivity) getContext();
                    try {
                        context.endGame();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //spawn new target
                targetSpawn = true;
                invalidate();
            }
            else {
                misses++;
                crosshair.setTint(Color.RED);
            }
            resetCrossColor();
            return true;
        }
    }

    private void resetCrossColor(){
        new CountDownTimer(200, 200) {

            public void onTick(long millisUntilFinished) {
                //do nothing
            }

            public void onFinish() {
                crosshair.setTint(Color.BLACK);
                invalidate();
            }
        }.start();
    }

}
