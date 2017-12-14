package edu.wsu.erikbuck.ball;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.twicecircled.spritebatcher.Drawer;
import com.twicecircled.spritebatcher.SpriteBatcher;

import java.util.Date;

import javax.microedition.khronos.opengles.GL10;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BallFullscreenActivity extends AppCompatActivity implements Drawer {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private GLSurfaceView mGLSurfaceView;
    private int mWidthPixels;
    private int mHeightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ball_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mCurrentInstant = new Date();
        mLastUpdateInstant = new Date();

        mGLSurfaceView = (GLSurfaceView) mContentView;
        int[] resourceIDs = new int[]{R.drawable.ball};

        // Set the Renderer for drawing on the GLSurfaceView
        mGLSurfaceView.setRenderer(new SpriteBatcher(this, resourceIDs, this));
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private static final double MILLIS_PER_SEC = 1000.0;
    private static final double MIN_TIME_BETWEEN_UPDATES_SEC = 0.006;
    private static final double MAX_TIME_BETWEEN_UPDATES_SEC = 0.032;
    private Date mCurrentInstant;
    private Date mLastUpdateInstant;
    private float mVectorX = 1.0f;
    private float mVectorY = 1.5f;
    private float mSpeed = 100.0f;
    private int mBallLocationX = 200;
    private int mBallLocationY = 200;
    private int bounceSpeed = 2;

    @Override
    public void onDrawFrame(GL10 gl, SpriteBatcher sb) {
        final Date newCurrentInstant = new Date();
        final double timeDeltaMillis = newCurrentInstant.getTime() - mLastUpdateInstant.getTime();
        double timeDeltaSec = timeDeltaMillis / MILLIS_PER_SEC;
        if (timeDeltaSec < MIN_TIME_BETWEEN_UPDATES_SEC) {
            // Not enough time has passed to be worth drawing
            return;
        } else if (timeDeltaSec > MAX_TIME_BETWEEN_UPDATES_SEC) {
            // Java and Android are so bad that the garbage collector stops the world
            // for a time period large enough to break animations and make animated
            // components overshoot their target positions. Testing has shown 69+ ms wasted
            timeDeltaSec = MAX_TIME_BETWEEN_UPDATES_SEC;
        }
        mLastUpdateInstant = mCurrentInstant;
        mCurrentInstant = newCurrentInstant;
        final float vSpeed = (bounceSpeed * mSpeed) / 100;

        //Defines bounds of "play area"
        if(mBallLocationX >= (mWidthPixels - 64) || mBallLocationX <= 64) {
                mVectorX *= -1.3;
                //Speed changing statement, this will be moved into line contact area
                if(vSpeed < 4)
                    bounceSpeed += 0.5;
                else if (vSpeed <= 5)
                    bounceSpeed += 0.1;
                else if (bounceSpeed > 6 && bounceSpeed <= 7)
                    bounceSpeed += 0.01;
        }
        if( mBallLocationY >= (mHeightPixels - 64) || mBallLocationY <= 64) {
            mVectorY *= -1.0;
            if(vSpeed < 4)
                bounceSpeed += 0.5;
            else if (vSpeed <= 5)
                bounceSpeed += 0.1;
            else if (bounceSpeed > 6 && bounceSpeed <= 7)
                bounceSpeed += 0.01;
        }


        final float scaledVectorX = vSpeed * mVectorX;
        final float scaledVectorY = vSpeed * mVectorY;
        mBallLocationX = (int)((float)mBallLocationX + scaledVectorX);
        mBallLocationY = (int)((float)mBallLocationY + scaledVectorY);
        final Rect ballSourceRect = new Rect(
                0, 0, 64, 64);
        Log.d("Ball X", String.valueOf(mBallLocationX));
        Log.d("Ball Y", String.valueOf(mBallLocationY));
        Log.d("Ball DeltaT", String.valueOf(timeDeltaSec));
        Log.d("Ball vSpeed", String.valueOf(vSpeed));
        sb.draw(R.drawable.ball,
                ballSourceRect,
                new Rect(mBallLocationX - 64,
                        mBallLocationY - 64,
                        128 + mBallLocationX,
                        128 + mBallLocationY)
                );    }
}
