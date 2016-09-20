package infrared5.com.red5proandroid.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public String ignoreName;
    public Runnable finishCall;

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
        if(ctx != null && _instance.getBaseContext() == null){
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

    public void callWithRunnable( final Runnable block ){
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

        final String urlStr = "http://" + config.host + ":5080/" + config.app + "/streams.jsp";

        if(callThread != null) {
            callThread.interrupt();
        }

        callThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    String responseString = "error: somehow string not assigned to?";
                    try {
                        BufferedInputStream in;
                        if (urlConnection.getResponseCode() == 200 && !Thread.interrupted()) {
                            in = new BufferedInputStream(urlConnection.getInputStream());
//                            responseString = readStream(in);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                            responseString = stringBuilder.toString().replaceAll("\\s+", "");
                            bufferedReader.close();
//                            System.out.println("retrieved stream list: " + responseString);
                        } else {
                            responseString = "error: http issue, response code - " + urlConnection.getResponseCode();
                        }
                    } catch (Exception e){
                    }finally {
                        urlConnection.disconnect();
                    }

                    if( !responseString.startsWith("error") && !Thread.interrupted()) {
                        JSONArray list = new JSONArray(responseString);

                        _liveStreams.clear();
                        for (int i = 0; i < list.length(); i++){
                            JSONObject obj = list.getJSONObject(i);
                            if(ignoreName == null || !obj.getString("name").equals(ignoreName))
                                _liveStreams.add(obj.getString("name"));
                        }
                    }
                    else if(!Thread.interrupted()){
                        System.out.println(responseString);
                    }

                    if (!Thread.interrupted()) {
                        if(finishCall != null)
                            finishCall.run();

                        Thread.sleep((long) (_loopDelay * 1000));
                    }

                    if (!Thread.interrupted())
                        makeCall();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        callThread.start();
    }

    private String readStream( BufferedInputStream in ) {

        StringBuffer total = new StringBuffer();
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((line = br.readLine()) != null) {
                total.append(line);
            }
        }catch (Exception e){
            e.printStackTrace();
            return "error: read issue";
        }

        return total.toString();
    }
}
