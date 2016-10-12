package infrared5.com.red5proandroid.publish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.source.R5AdaptiveBitrateController;
import com.red5pro.streaming.source.R5Camera;
import com.red5pro.streaming.source.R5Microphone;
import com.red5pro.streaming.view.R5VideoView;

import java.util.ArrayList;
import java.util.List;

import infrared5.com.red5proandroid.AppState;
import infrared5.com.red5proandroid.ControlBarFragment;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.settings.SettingsDialogFragment;

public class Publish extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        ControlBarFragment.OnFragmentInteractionListener, SettingsDialogFragment.OnFragmentInteractionListener {

    protected int cameraSelection = -1;
    protected int cameraOrientation = 0;
    protected Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    protected List<Camera.Size> sizes = new ArrayList<Camera.Size>();
    public static Camera.Size selected_size = null;
    public static String selected_item = null;
    public static int preferedResolution = 0;
    public static PublishStreamConfig config = null;
    protected boolean override = false;
    protected R5Camera r5Cam;
    protected R5Microphone r5Mic;
    protected R5VideoView surfaceForCamera;
    protected SettingsDialogFragment dialogFragment;

    static {
        if(config==null){
            config = new PublishStreamConfig();
        }
    }

    protected Camera camera;
    protected boolean isPublishing = false;

    protected R5Stream stream;

    public final static String TAG = "Preview";

    public void onSettingsClick() {
        stopPublishing();
        stopCamera();
        openSettings();
    }

    public String getStringResource(int id) {
        return getResources().getString(id);
    }

    public int getIntResource(int id) {
        return getResources().getInteger(id);
    }

    public Boolean getBooleanResource(int id) {
        return getResources().getBoolean(id);
    }

    public void onSettingsDialogClose() {
        dialogFragment = null;
        configure();
        configureStream(true);
    }

    //grab user data to be used in R5Configuration
    protected void configure() {
        SharedPreferences preferences = getSharedPreferences(getStringResource(R.string.preference_file), MODE_MULTI_PROCESS);
        config.host = preferences.getString(getStringResource(R.string.preference_host), getStringResource(R.string.preference_default_host));
        config.port = preferences.getInt(getStringResource(R.string.preference_port), getIntResource(R.integer.preference_default_port));
        config.app = preferences.getString(getStringResource(R.string.preference_app), getStringResource(R.string.preference_default_app));
        config.name = preferences.getString(getStringResource(R.string.preference_name), getStringResource(R.string.preference_default_name));
        config.bitrate = preferences.getInt(getStringResource(R.string.preference_bitrate), getIntResource(R.integer.preference_default_bitrate));
        config.audio = preferences.getBoolean(getStringResource(R.string.preference_audio), getBooleanResource(R.bool.preference_default_audio));
        config.video = preferences.getBoolean(getStringResource(R.string.preference_video), getBooleanResource(R.bool.preference_default_video));
        config.adaptiveBitrate = preferences.getBoolean(getStringResource(R.string.preference_adaptive_bitrate), getBooleanResource(R.bool.preference_default_adaptive_bitrate));
        config.debug = preferences.getBoolean(getStringResource(R.string.preference_debug), getBooleanResource(R.bool.preference_default_debug));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(override)
            return;

        //assign the layout view
        setContentView(R.layout.activity_publish);

        //setup properties in configuration
        configure();

        final View v = findViewById(android.R.id.content);

        v.setKeepScreenOn(true);

        setDefaultCamera();

        //activate the camera
        Camera.getCameraInfo(cameraSelection, cameraInfo);
        setOrientationMod();

        ImageButton rButton = (ImageButton) findViewById(R.id.btnRecord);
        rButton.setOnClickListener(this);

        ImageButton cameraButton = (ImageButton) findViewById(R.id.btnCamera);
        cameraButton.setOnClickListener(this);
    }

    protected void setDefaultCamera(){
        ControlBarFragment controlBar = (ControlBarFragment)getFragmentManager().findFragmentById(R.id.control_bar);
        controlBar.setSelection(AppState.PUBLISH);
        controlBar.displayPublishControls(true);

        if( Camera.getNumberOfCameras() < 2 ){
            findViewById(R.id.btnCamera).setVisibility(View.GONE);
            cameraSelection = 0;
        }
        else {
            Camera.CameraInfo tempInfo = new Camera.CameraInfo();
            for( int i = 0; i < Camera.getNumberOfCameras(); i++ ){
                Camera.getCameraInfo(i, tempInfo);
                if(tempInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    cameraSelection = i;
                    break;
                }
            }
            if(cameraSelection < 0) cameraSelection = 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openSettings();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopPublishing();
        stopCamera();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void openSettings(){
        try {
            dialogFragment = SettingsDialogFragment.newInstance(AppState.PUBLISH);
            getFragmentManager().beginTransaction().add(R.id.settings_frame, dialogFragment).commit();

            List<String> sb = new ArrayList<String>();
            for(Camera.Size size:this.sizes){
                if((size.width/2)%16!=0){
                    continue;
                }

                String potential = String.valueOf(size.width).trim() +"x"+  String.valueOf(size.height).trim();
                sb.add(potential);
            }
            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item,sb) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextColor(0xffff0000);
                    return v;
                }

                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View v = super.getDropDownView(position, convertView, parent);
                    ((TextView) v).setTextColor(0xffff0000);
                    ((TextView) v).setGravity(Gravity.CENTER);
                    return v;
                }
            };

            dialogFragment.setSpinnerAdapter(adapter);

        }
        catch(Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Can't open settings: " + e.getMessage());
        }
    }

    protected void toggleCamera() {
        cameraSelection = (cameraSelection + 1) % Camera.getNumberOfCameras();
        try {
            Camera.getCameraInfo(cameraSelection, cameraInfo);
            cameraSelection = cameraInfo.facing;
        }
        catch(Exception e) {
            // can't find camera at that index, set default
            cameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        setOrientationMod();

        stopCamera();
        setCamera();

        if(camera != null)
            camera.startPreview();
    }

    protected void setOrientationMod(){

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_90:
                if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    degrees = 90;
                else
                    degrees = 270;
                break;
            case Surface.ROTATION_270:
                if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    degrees = 270;
                else
                    degrees = 90;
                break;
        }

        degrees += cameraInfo.orientation;

        cameraOrientation = degrees;
    }

    private void stopCamera() {
        if(camera != null) {
            sizes.clear();

            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    //called by record button
    protected void startPublishing() {
        if(!isPublishing) {
            if(stream != null){
                stream.stop();
            }

            beginStream();
        }
    }

    protected void beginStream(){

        configureStream(false);

        surfaceForCamera.showDebugView(config.debug);

        isPublishing = true;
        stream.publish(Publish.config.name, R5Stream.RecordType.Live);

        if(camera != null)
            camera.startPreview();
    }

    protected void configureStream(boolean startPreview){

        final R5Configuration configuration = new R5Configuration(R5StreamProtocol.RTSP, Publish.config.host,  Publish.config.port, Publish.config.app, 0.5f);
        stream = new R5Stream(new R5Connection(configuration));

        stream.setLogLevel(R5Stream.LOG_LEVEL_DEBUG);

        stream.connection.addListener(new R5ConnectionListener() {
            @Override
            public void onConnectionEvent(R5ConnectionEvent event) {
                Log.d("publish","connection event code "+event.value()+"\n");
                switch(event.value()){
                    case 0://open
                        System.out.println("Connection Listener - Open");
                        break;
                    case 1://close
                        System.out.println("Connection Listener - Close");
                        break;
                    case 2://error
                        System.out.println("Connection Listener - Error: " + event.message);
                        break;

                }
            }
        });

        stream.setListener(new R5ConnectionListener() {
            @Override
            public void onConnectionEvent(R5ConnectionEvent event) {
                switch (event) {
                    case CONNECTED:
                        System.out.println("Stream Listener - Connected");
                        break;
                    case DISCONNECTED:
                        System.out.println("Stream Listener - Disconnected");
                        break;
                    case START_STREAMING:
                        System.out.println("Stream Listener - Started Streaming");
                        break;
                    case STOP_STREAMING:
                        System.out.println("Stream Listener - Stopped Streaming");
                        break;
                    case CLOSE:
                        System.out.println("Stream Listener - Close");
                        break;
                    case TIMEOUT:
                        System.out.println("Stream Listener - Timeout");
                        break;
                    case ERROR:
                        System.out.println("Stream Listener - Error: " + event.message);
                        break;
                }
            }
        });

        r5Mic = new R5Microphone();

        if(config.video) {
            setCamera();

            if(camera != null)
                stream.attachCamera(r5Cam);
        }

        //assign the surface to show the camera output
        surfaceForCamera = new R5VideoView(this);
        FrameLayout frame = ((FrameLayout)findViewById(R.id.preview_container));
        frame.removeAllViews();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frame.addView(surfaceForCamera, params);

        surfaceForCamera.attachStream(stream);

        if(config.audio) {
            stream.attachMic(r5Mic);
        }

        if (config.adaptiveBitrate) {
            final R5AdaptiveBitrateController adaptiveBitrateController = new R5AdaptiveBitrateController();
            adaptiveBitrateController.AttachStream(stream);

            if (config.video) {
//                    adaptiveBitrateController.requiresVideo = true;
            }
        }

        if(startPreview && camera != null)
            camera.startPreview();
    }

    protected void setCamera(){

        if(camera == null){
            try{
                camera = Camera.open(cameraSelection);
            }catch (Exception e){
//                e.printStackTrace();
                System.out.println("Cannot connect to camera - moving on without it");
                return;
            }
            Camera.getCameraInfo(cameraSelection, cameraInfo);
            setOrientationMod();
            camera.setDisplayOrientation((cameraOrientation + (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? 180 : 0)) % 360);
            sizes=camera.getParameters().getSupportedPreviewSizes();
        }

        if(camera != null)
            camera.stopPreview();

        //add the camera for streaming
        if(selected_item != null) {
            Log.d("publisher","selected_item "+selected_item);
            String bits[] = selected_item.split("x");
            int pW= Integer.valueOf(bits[0]);
            int pH=  Integer.valueOf(bits[1]);

            if( r5Cam == null ) {
                r5Cam = new R5Camera(camera, pW, pH);
                r5Cam.setBitrate(Publish.config.bitrate);
            }
            else
                r5Cam.setCamera(camera);
        }
        else {

            if( r5Cam == null ) {
                r5Cam = new R5Camera(camera, 320, 240);
                r5Cam.setBitrate(config.bitrate);
            }
            else
                r5Cam.setCamera(camera);
        }

        r5Cam.setOrientation(cameraOrientation);
    }

    protected void stopPublishing() {
        if(stream!=null && isPublishing) {
            if(!override) {
                ImageButton rButton = (ImageButton) findViewById(R.id.btnRecord);
                rButton.setImageResource(R.drawable.empty_red);
            }

            stream.stop();
        }
        isPublishing = false;

        r5Cam = null;
    }

    public void onClick(View view) {
        ImageButton rButton = (ImageButton) findViewById(R.id.btnRecord);

        if(view.getId() == R.id.btnRecord) {
            if(isPublishing) {
                onBackPressed();
            }
            else {
                beginStream();
                rButton.setImageResource(R.drawable.empty);
            }
        }
        else if(view.getId() == R.id.btnCamera) {
            toggleCamera();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try{
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        };

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {}

    @Override
    public void onBackPressed() {
        if(dialogFragment == null || !dialogFragment.advancedOpen)
            super.onBackPressed();
        else
            dialogFragment.forceReturnFromAdvanced();
    }

}
