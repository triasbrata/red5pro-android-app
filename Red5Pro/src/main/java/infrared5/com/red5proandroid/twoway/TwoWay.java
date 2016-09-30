package infrared5.com.red5proandroid.twoway;

import android.app.ActivityManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.source.R5Camera;
import com.red5pro.streaming.source.R5Microphone;
import com.red5pro.streaming.view.R5VideoView;

import java.util.ArrayList;

import infrared5.com.red5proandroid.AppState;
import infrared5.com.red5proandroid.ControlBarFragment;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.navigation.SlideNav;
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.settings.SettingsDialogFragment;
import infrared5.com.red5proandroid.utilities.StreamListUtility;
import infrared5.com.red5proandroid.utilities.SubscribeList;

/**
 * Created by davidHeimann on 5/26/16.
 */
public class TwoWay extends Publish implements SubscribeList.Callbacks, SettingsDialogFragment.OnFragmentInteractionListener{

//    protected ViewFlipper flipper;
    protected DrawerLayout listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        override = true;

        super.onCreate(savedInstanceState);

//        flipper = new ViewFlipper(this);
//        flipper.setAutoStart(false);
//        setContentView(flipper);


        //two way
        View twView = View.inflate(this, R.layout.activity_two_way, null);

        setContentView( twView );
//        flipper.addView(twView);
        //stream list
//        View listView = View.inflate(this, R.layout.stream_list, null);
//        flipper.addView(listView);
//
//        flipper.setDisplayedChild(0);

        camera = Camera.open(cameraSelection);
        Camera.getCameraInfo(cameraSelection, cameraInfo);
        setOrientationMod();
        camera.setDisplayOrientation((cameraOrientation + (cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT ? 180 : 0)) % 360);
        sizes=camera.getParameters().getSupportedPreviewSizes();

        ImageButton rButton = (ImageButton) twView.findViewById(R.id.btnRecord);
        rButton.setImageResource(R.drawable.empty);

        configure();
    }

    @Override
    public void onSettingsClick() {
        if( subStream != null ){
            subStream.stop();
            subStream = null;
        }

        super.onSettingsClick();
    }

    @Override
    protected void openSettings() {
        super.openSettings();

//        dialogFragment.onAttach(this);
    }

    @Override
    public void onSettingsDialogClose() {
//        super.onSettingsDialogClose();
        dialogFragment = null;
        configure();

        goToStreamList();
//        dialogFragment.onDetach();
    }

    protected TextView streamNum;
    protected TextView subButton;
    protected SubscribeList streamList;
    protected String subName = "";
    protected boolean onList = false;

    protected void goToStreamList(){

        if(onList)
            return;

        onList = true;

        listView = (DrawerLayout) View.inflate(this, R.layout.stream_list, null);
        ((FrameLayout)findViewById(R.id.settings_frame)).addView(listView);

        configure();
        //publish while selecting stream
        startPublishing();

//        flipper.setDisplayedChild(1);

        final TwoWay thisParent = this;

        //Delay all of these so that the publish call doesn't kill the runnables
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    Thread.sleep(500);
                }catch (Exception e){}

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final View v = findViewById(android.R.id.content);
                        v.setKeepScreenOn(true);

                        TextView streamName = (TextView)findViewById(R.id.publishText);
                        streamName.setText(config.name);

                        streamNum = (TextView) findViewById(R.id.StreamNum);

                        streamList = (SubscribeList) getFragmentManager().findFragmentById(R.id.streamList);
                        streamList.mCallbacks = thisParent;

                        final DrawerLayout drawer = listView;

                        findViewById(R.id.slideNavBtn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                drawer.openDrawer(Gravity.LEFT);
                            }
                        });

                        TextView backBtn = (TextView) findViewById(R.id.Back);
                        backBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                onBackPressed();
                            }
                        });

                        //Subscribe hit
                        subButton = (Button) findViewById(R.id.submit);
                        subButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("moving on? " + subName.isEmpty());

                                if (subName.isEmpty())
                                    return;

                                goToStreamView();
                            }
                        });

                        StreamListUtility util = StreamListUtility.get_instance(thisParent);
                        util.callWithRunnable(new Runnable() {
                            @Override
                            public void run() {
                                UpdateStreamList();
                            }
                        });
                        util.ignoreName = config.name;
                    }
                });
            }
        }).start();
    }

    protected R5Stream subStream;

    protected void goToStreamView(){

        onList = false;

        StreamListUtility.get_instance().clearAndDisconnect();
        streamList.mCallbacks = null;
        getFragmentManager().beginTransaction().remove(streamList).commit();
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.left_drawer)).commit();

//        flipper.setDisplayedChild(0);

        ((FrameLayout)findViewById(R.id.settings_frame)).removeAllViews();

        ControlBarFragment controlBar = (ControlBarFragment)getFragmentManager().findFragmentById(R.id.control_bar);
        controlBar.setSelection(AppState.PUBLISH);
        controlBar.displayPublishControls(true);

        ImageButton rButton = (ImageButton) findViewById(R.id.btnRecord);
        rButton.setOnClickListener(this);

        ImageButton cameraButton = (ImageButton) findViewById(R.id.btnCamera);
        cameraButton.setOnClickListener(this);

        //connect the publisher to a view
//        this.surfaceForCamera = (SurfaceView) findViewById(R.id.publishView);
//        stream.setView((SurfaceView) findViewById(R.id.publishView));

        //create the subscriber and connect it
        subStream = new R5Stream(new R5Connection(new R5Configuration(R5StreamProtocol.RTSP, Publish.config.host,  Publish.config.port, Publish.config.app, 1.0f)));
        R5VideoView subView = new R5VideoView(this);
        FrameLayout frame = (FrameLayout)findViewById(R.id.subscribe_container);
        frame.removeAllViews();
        frame.addView(subView);
        subView.attachStream(subStream);
        subView.showDebugView(config.debug);
        subStream.play( subName );
    }

    protected void UpdateStreamList(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                streamList.connectList();

                if( !subName.isEmpty() ){
                    if( !StreamListUtility._liveStreams.contains(subName) ){
                        subName = "";
                        subButton.setAlpha(0.5f);
                    }
                    else {
                        streamList.setSelection( StreamListUtility._liveStreams.indexOf(subName) );
                    }
                }

                streamNum.setText(StreamListUtility._liveStreams.size() + "Streams");
            }
        });
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btnRecord) {
            if(isPublishing) {
                stopPublishing();
                onBackPressed();
            }
        }
        else if(view.getId() == R.id.btnCamera) {
            toggleCamera();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            stopPublishing();
            if(subStream != null) {
                subStream.stop();
                subStream = null;
            }

            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int id) {

        subName = StreamListUtility._liveStreams.get(id);

        subButton.setAlpha(1.0f);
    }

    @Override
    protected void onDestroy() {

        StreamListUtility.get_instance().clearAndDisconnect();

        if(subStream != null){
            subStream.stop();
        }
        if(streamList != null){
            streamList.mCallbacks = null;
        }

        super.onDestroy();
    }
}
