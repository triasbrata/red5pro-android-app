package infrared5.com.red5proandroid.twoway;

import android.os.Bundle;
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

import java.util.ArrayList;

import infrared5.com.red5proandroid.AppState;
import infrared5.com.red5proandroid.ControlBarFragment;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.utilities.StreamListUtility;
import infrared5.com.red5proandroid.utilities.SubscribeList;

/**
 * Created by davidHeimann on 5/26/16.
 */
public class TwoWay extends Publish implements SubscribeList.Callbacks{

    protected ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        override = true;

        super.onCreate(savedInstanceState);

        flipper = new ViewFlipper(this);
        flipper.setAutoStart(false);
        setContentView(flipper);

        //first item is a holder for settings screen?
        flipper.addView(new View(this));
        //stream list
        flipper.addView(View.inflate(this, R.layout.stream_list, null));
        //two way
        flipper.addView(View.inflate(this, R.layout.activity_two_way, null));

        //don't have a publish settings screen for now
        //go straight to choosing the stream to subscribe to
        goToStreamList();
    }

    protected TextView streamNum;
    protected TextView subButton;
    protected SubscribeList streamList;
    protected String subName = "";

    protected void goToStreamList(){

        flipper.setDisplayedChild(1);

        configure();

        final View v =findViewById(android.R.id.content);
        v.setKeepScreenOn(true);

        //publish while selecting stream
        startPublishing();

        TextView streamName = (TextView)findViewById(R.id.publishText);
        streamName.setText(config.name);

        streamNum = (TextView) findViewById(R.id.StreamNum);

        streamList = (SubscribeList) getFragmentManager().findFragmentById(R.id.streamList);
        streamList.mCallbacks = this;

        TextView backBtn = (TextView) findViewById(R.id.Back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StreamListUtility.get_instance().clearAndDisconnect();
                streamList.mCallbacks = null;

//                Go back to the settings page. Either by flipping back, or going back to the previous activity
            }
        });

        //Subscribe hit
        subButton = (TextView) findViewById(R.id.Subscribe);
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subName.isEmpty())
                    return;

                goToStreamView();
            }
        });

        StreamListUtility util = StreamListUtility.get_instance();
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

        flipper.setDisplayedChild(2);

        ControlBarFragment controlBar = (ControlBarFragment)getFragmentManager().findFragmentById(R.id.control_bar);
        controlBar.setSelection(AppState.PUBLISH);
        controlBar.displayPublishControls(true);

        ImageButton rButton = (ImageButton) findViewById(R.id.btnRecord);
        rButton.setOnClickListener(this);

        ImageButton cameraButton = (ImageButton) findViewById(R.id.btnCamera);
        cameraButton.setOnClickListener(this);

        //connect the publisher to a view
        this.surfaceForCamera = (SurfaceView) findViewById(R.id.publishView);
        stream.setView((SurfaceView) findViewById(R.id.publishView));

        //create the subscriber and connect it
        subStream = new R5Stream(new R5Connection(new R5Configuration(R5StreamProtocol.RTSP, Publish.config.host,  Publish.config.port, Publish.config.app, 1.0f)));
        subStream.setView((SurfaceView) findViewById(R.id.surfaceView));
        subStream.play( subName );
    }

    protected void UpdateStreamList(){
        if(!subName.isEmpty() && !StreamListUtility._liveStreams.contains(subName)){
            subName = "";
            subButton.setAlpha(0.5f);
        }
        streamNum.setText(StreamListUtility._liveStreams.size() + "Streams");

        streamList.connectList();
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
