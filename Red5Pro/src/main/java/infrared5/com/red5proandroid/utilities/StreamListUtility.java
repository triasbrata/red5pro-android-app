package infrared5.com.red5proandroid.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.publish.PublishStreamConfig;

/**
 * Created by davidHeimann on 5/25/16.
 */
public class StreamListUtility extends Activity {

    public static PublishStreamConfig config = null;
    public static ArrayList<String> _liveStreams;
    public static float _loopDelay = 2.5f;
    public static Runnable finishCall;

    private Thread callThread;
    private static StreamListUtility _instance;
    public static StreamListUtility get_instance(){ return get_instance(null); }
    public static StreamListUtility get_instance(Context ctx) {

        if (_instance == null) {
            _instance = new StreamListUtility();
            if(_liveStreams == null){
                _liveStreams = new ArrayList<String>();
            }
        }
        if(ctx != null){
            _instance.attachBaseContext(ctx);
        }
        return _instance;
    }
    static {
        if(config==null){
            config = new PublishStreamConfig();
        }
    }

    public String getStringResource(int id) {
        return getResources().getString(id);
    }

    private void configure() {
        SharedPreferences preferences = getSharedPreferences(getStringResource(R.string.preference_file), MODE_MULTI_PROCESS);
        config.host = preferences.getString(getStringResource(R.string.preference_host), getStringResource(R.string.preference_default_host));
        config.app = preferences.getString(getStringResource(R.string.preference_app), getStringResource(R.string.preference_default_app));
        config.name = preferences.getString(getStringResource(R.string.preference_name), getStringResource(R.string.preference_default_name));
    }

    public void callWithRunnable( Runnable block ){
        finishCall = block;
        makeCall();
    }

    public void callWithRunnableOnce( final Runnable block ){
        callWithRunnable(new Runnable() {
            @Override
            public void run() {
                callThread.interrupt();
                block.run();
            }
        });
    }

    public void clearAndDisconnect(){
        if(callThread != null){
            callThread.interrupt();
        }
        finishCall = null;
    }

    private void makeCall(){
        configure();

        final String url = "http://" + config.host + ":5080/" + config.app + "/streams.jsp";

        if(callThread != null) {
            callThread.interrupt();
        }

        callThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(new HttpGet(url));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK && !Thread.interrupted()) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        String responseString = out.toString();
                        out.close();

                        JSONArray list = new JSONArray(responseString);

                        _liveStreams.clear();
                        for (int i = 0; i < list.length(); i++){
                            JSONObject obj = list.getJSONObject(i);
                            if(!obj.getString("name").equals(config.name))
                                _liveStreams.add(obj.getString("name"));
                        }
                    }

                    if(finishCall != null)
                        finishCall.run();

                    if(!Thread.interrupted())
                        Thread.sleep((long) (_loopDelay * 1000));

                    if(!Thread.interrupted())
                        makeCall();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        callThread.start();
    }
}
