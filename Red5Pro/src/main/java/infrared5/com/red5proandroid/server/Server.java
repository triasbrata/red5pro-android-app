package infrared5.com.red5proandroid.server;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import infrared5.com.red5proandroid.Main;
import infrared5.com.red5proandroid.R;

/**
 * Created by kylekellogg on 4/21/16.
 */
public class Server extends Activity {

    private EditText serverText;
    private EditText portText;

    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_server);

        final Button submitButton = (Button) findViewById(R.id.serverSubmitBtn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

        serverText = (EditText) findViewById(R.id.serverTextField);
        portText = (EditText) findViewById(R.id.portTextField);

        SharedPreferences preferences = getPreferences(Context.MODE_MULTI_PROCESS);

        String storedServer = preferences.getString(getPreferenceValue(R.string.preference_host), "0.0.0.0");

        if (storedServer != "0.0.0.0") {
            serverText.setText(storedServer);
        }

        String storedPort = preferences.getString((getPreferenceValue(R.string.preference_port)), "8554");
        portText.setText(storedPort);

        errorText = (TextView) findViewById(R.id.serverErrorText);
    }

    private String getPreferenceValue(int id) {
        return getResources().getString(id);
    }

    private void onSubmit() {
        final String server = serverText.getText().toString();
        final String port = portText.getText().toString();

        if (server.length() > 0 && port.length() > 0) {
            SharedPreferences preferences = getPreferences(Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(getPreferenceValue(R.string.preference_host), server);
            editor.putString(getPreferenceValue(R.string.preference_port), port);

            editor.commit();

            startActivity(new Intent(this, Main.class));
        } else {
            errorText.animate()
                    .setDuration(250)
                    .alpha(1.0f)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            errorText.animate()
                                    .setStartDelay(3000)
                                    .setDuration(250)
                                    .alpha(0.0f)
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();
        }
    }
}
