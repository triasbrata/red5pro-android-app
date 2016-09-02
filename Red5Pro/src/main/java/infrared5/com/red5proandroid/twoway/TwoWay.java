package infrared5.com.red5proandroid.twoway;

import android.app.ActivityManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
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
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.settings.SettingsDialogFragment;
import infrared5.com.red5proandroid.utilities.StreamListUtility;
import infrared5.com.red5proandroid.utilities.SubscribeList;

/**
 * Created by davidHeimann on 5/26/16.
 */
public class TwoWay extends Publish implements SubscribeList.Callbacks, SettingsDialogFragment.OnFragmentInteractionListener{

    protected ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        override = true;

        super.onCreate(savedInstanceState);

        flipper = new ViewFlipper(this);
        flipper.setAutoStart(false);
        setContentView(flipper);


        //two way
        View twView = View.inflate(this, R.layout.activity_two_way, null);
        flipper.addView(twView);
        //stream list
        flipper.addView(View.inflate(this, R.layout.stream_list, null));

        flipper.setDisplayedChild(0);

        //don't have a publish settings screen for now?
        //go straight to choosing the stream to subscribe to
        //goToStreamList();

        camera = Camera.open(cameraSelection);
        Camera.getCameraInfo(cameraSelection, cameraInfo);
        setOrientationMod();
        camera.setDisplayOrientation((cameraOrientation + (cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT ? 180 : 0)) % 360);
        sizes=camera.getParameters().getSupportedPreviewSizes();

        surfaceForCamera = (R5VideoView) twView.findViewById(R.id.video);
        configure();
    }

    @Override
    protected void openSettings() {
        super.openSettings();

//        dialogFragment.onAttach(this);
    }

    @Override
    public void onSettingsDialogClose() {
        super.onSettingsDialogClose();

        goToStreamList();
//        dialogFragment.onDetach();
    }

    protected TextView streamNum;
    protected TextView subButton;
    protected SubscribeList streamList;
    protected String subName = "";

    protected void goToStreamList(){

        configure();
        //publish while selecting stream
        startPublishing();

        flipper.setDisplayedChild(1);

        final View v =findViewById(android.R.id.content);
        v.setKeepScreenOn(true);

        TextView streamName = (TextView)findViewById(R.id.publishText);
        streamName.setText(config.name);

        streamNum = (TextView) findViewById(R.id.StreamNum);

        streamList = (SubscribeList) getFragmentManager().findFragmentById(R.id.streamList);
        streamList.mCallbacks = this;

        final DrawerLayout drawer = (DrawerLayout) flipper.getCurrentView();

        findViewById(R.id.slideNavBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        v.findViewById(R.id.content_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer.isDrawerOpen(Gravity.LEFT))
                    drawer.closeDrawer(Gravity.LEFT);
            }
        });

        TextView backBtn = (TextView) findViewById(R.id.Back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StreamListUtility.get_instance().clearAndDisconnect();
                streamList.mCallbacks = null;

                //Go back to the settings page. Either by flipping back, or going back to the previous activity
//                flipper.setDisplayedChild(0);
                if(stream != null) {
                    stream.stop();
                    stream = null;
                }

                onBackPressed();
            }
        });

        //Subscribe hit
        subButton = (TextView) findViewById(R.id.submit);
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subName.isEmpty())
                    return;

                goToStreamView();
            }
        });

        StreamListUtility util = StreamListUtility.get_instance(this);
        util.callWithRunnable(new Runnable() {
            @Override
            public void run() {
                UpdateStreamList();
            }
        });
    }

    protected R5Stream subStream;

    protected void goToStreamView(){

        StreamListUtility.get_instance().clearAndDisconnect();
        streamList.mCallbacks = null;

        flipper.setDisplayedChild(0);

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
        subStream.setView((SurfaceView) findViewById(R.id.surfaceView));
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

//        if( camera != null ){
//            try{
//                camera.release();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }

        super.onDestroy();
    }
}
