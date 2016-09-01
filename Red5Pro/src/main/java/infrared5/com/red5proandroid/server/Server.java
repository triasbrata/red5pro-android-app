package infrared5.com.red5proandroid.server;

import android.animation.Animator;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import infrared5.com.red5proandroid.Main;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.help.HelpDialogFragment;

/**
 * Created by kylekellogg on 4/21/16.
 */
public class Server extends Activity {

    private EditText serverText;
    private EditText portText;

    private TextView errorText;

    private DialogFragment helpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_server);

        final Button submitButton = (Button) findViewById(R.id.serverSubmitBtn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

        final ImageButton help = (ImageButton) findViewById(R.id.btnHelp);
        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });

        serverText = (EditText) findViewById(R.id.serverTextField);
        portText = (EditText) findViewById(R.id.portTextField);

        SharedPreferences preferences = getSharedPreferences(getPreferenceValue(R.string.preference_file), MODE_MULTI_PROCESS);

        String storedServer = preferences.getString(getPreferenceValue(R.string.preference_host), "0.0.0.0");

        if (!storedServer.equals("0.0.0.0")) {
            serverText.setText(storedServer);
        }

        int port = preferences.getInt(getPreferenceValue(R.string.preference_port), 8554);
        portText.setText(String.valueOf(port));

        errorText = (TextView) findViewById(R.id.serverErrorText);

        helpDialog = HelpDialogFragment.newInstance();
    }

    private String getPreferenceValue(int id) {
        return getResources().getString(id);
    }

    private void onSubmit() {
        final String server = serverText.getText().toString().trim();
        final String port = portText.getText().toString().trim();

        if ((server.length() > 0 && !server.equals("0.0.0.0")) && port.length() > 0) {
            SharedPreferences preferences = getSharedPreferences(getPreferenceValue(R.string.preference_file), MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = preferences.edit();

            Log.d("ServerSettings", "Saving " + server + " as " + getPreferenceValue(R.string.preference_host));
            editor.putString(getPreferenceValue(R.string.preference_host), server);
            editor.putInt(getPreferenceValue(R.string.preference_port), Integer.parseInt(port));

            editor.apply();


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

    private void showHelp() {
        helpDialog.show(getFragmentManager().beginTransaction(), "help_dialog");
    }
}
