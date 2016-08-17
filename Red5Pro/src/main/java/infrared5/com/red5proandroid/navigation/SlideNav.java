package infrared5.com.red5proandroid.navigation;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import infrared5.com.red5proandroid.R;

/**
 * Created by davidHeimann on 8/5/16.
 */
public class SlideNav extends Fragment {

    public RelativeLayout navView;
    private Handler mainQueue;
    private boolean screenOut;
    private boolean gestureCapture;
    private boolean gestureActive;
    private Point displaySize;
    private Point gestureStart;
    private float xMod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainQueue = new Handler(inflater.getContext().getMainLooper());

        WindowManager wm = (WindowManager) inflater.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        displaySize = new Point();
        display.getSize(displaySize);

        navView = (RelativeLayout)inflater.inflate(R.layout.fragment_slide_nav, null, false);
        navView.layout( 0 - navView.getMeasuredWidth(), 0,0,0 );

        screenOut = false;

        //all of the buttons need touch handlers... urgh...

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                gestureStart = new Point((int)event.getX(), (int)event.getY());
                gestureStart.x -= xMod * displaySize.x / 3f;

                if( (screenOut && gestureStart.x < displaySize.x/6f) || (!screenOut && gestureStart.x > displaySize.x * 5f/6f))
                    return false;

                gestureCapture = true;
                gestureActive = false;
                return false;
            case (MotionEvent.ACTION_MOVE) :
                if(gestureCapture)
                    slideNavPane( new Point((int)event.getX(), (int)event.getY()) );
                return true;
            case (MotionEvent.ACTION_CANCEL) :
            case (MotionEvent.ACTION_OUTSIDE) :
            case (MotionEvent.ACTION_UP) :
                endGesture();
                return false;
            default :
                return false;
        }
    }

    private void slideNavPane( Point newPoint ){

        float difX = newPoint.x - gestureStart.x;
        float difY = newPoint.y - gestureStart.y;

        if( !gestureActive && Math.abs(difX) + Math.abs(difY) > (displaySize.x + displaySize.y)/100f )
            gestureActive = true;

        if( gestureActive && Math.abs(difX) * 2 < Math.abs(difY) ){
            endGesture();
        }

        xMod = difX/(displaySize.x/3f);

        if( screenOut ){
            if( xMod < -1 ){
                //move off-screen
                navView.layout( 0 - navView.getMeasuredWidth(), 0,0,0 );
                xMod = -1.0f;
            }
            else if( xMod < 0.0f) {
                //move the screen to the right point
                navView.layout( (int)(navView.getMeasuredWidth() * xMod), 0,0,0 );
            }
            else {
                //return it to full-out position
                navView.layout( 0, 0,0,0 );
                xMod = 0.0f;
            }
        }
        else{
            if( xMod > 1 ){
                //move to fully out position
                navView.layout( 0, 0,0,0 );
                xMod = 1.0f;
            }
            else if( xMod > 0.0f) {
                //move the screen to the right point
                navView.layout( 0 - (int)(navView.getMeasuredWidth() * xMod), 0,0,0 );
            }
            else {
                //return to off-screen position
                navView.layout( 0 - navView.getMeasuredWidth(), 0,0,0 );
                xMod = 0.0f;
            }
        }
    }

    private void endGesture(){
        gestureCapture = false;
        gestureActive = false;

        int targetX = 0;
        float modDir = 0.5f / 30f;

        if(screenOut){
            if( xMod < -0.5f ){
                targetX -= navView.getMeasuredWidth();
                modDir *= -1;
            }
        }
        else{
            if(xMod < 0.5f) {
                targetX -= navView.getMeasuredWidth();
                modDir *= -1;
            }
        }

        final int targetLoc = targetX;
        final float modMove = modDir;

        new Thread(new Runnable() {
            @Override
            public void run() {

                while( Math.abs(xMod + modMove) < 1 && xMod * (xMod + modMove) > 0 ){

                    try{
                        Thread.sleep((long)(1000.0/30.0));
                    }catch (Exception e){}

                    if(gestureActive)
                        return;

                    xMod += modMove;

                    if( screenOut )
                        sendLayoutToMain((int)(navView.getMeasuredWidth() * xMod));
                    else
                        sendLayoutToMain(0 - (int)(navView.getMeasuredWidth() * xMod));
                }

                if( (screenOut && xMod + modMove < -1) || (!screenOut && xMod + modMove < 0) ){
                    sendLayoutToMain(0 - navView.getMeasuredWidth());
                }
                else {
                    sendLayoutToMain(0);
                }

                screenOut = !screenOut;
            }
        }).run();
    }

    private void sendLayoutToMain(final int xVal){

        mainQueue.post(new Runnable() {
            @Override
            public void run() {
                navView.layout( xVal,0,0,0 );
            }
        });
    }
}
