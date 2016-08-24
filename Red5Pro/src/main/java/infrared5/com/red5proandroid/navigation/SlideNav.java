package infrared5.com.red5proandroid.navigation;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

import infrared5.com.red5proandroid.Main;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.help.HelpDialogFragment;
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.server.Server;
import infrared5.com.red5proandroid.settings.Subscribe;
import infrared5.com.red5proandroid.twoway.TwoWay;

/**
 * Created by davidHeimann on 8/5/16.
 */
public class SlideNav extends Fragment {

    public RelativeLayout navView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        navView = (RelativeLayout)inflater.inflate(R.layout.fragment_slide_nav, null, false);

        //server
        navView.findViewById(R.id.serverBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), Server.class));
                getActivity().finish();
            }
        });

        //publish
        navView.findViewById(R.id.publishBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), Publish.class));
                getActivity().finish();
            }
        });

        //subscribe
        navView.findViewById(R.id.subscribeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), Subscribe.class));
                getActivity().finish();
            }
        });

        //two way
        navView.findViewById(R.id.twowayBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), TwoWay.class));
                getActivity().finish();
            }
        });

        //help
        navView.findViewById(R.id.helpBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialogFragment helpDialog = HelpDialogFragment.newInstance();
                helpDialog.show(getFragmentManager().beginTransaction(), "help_dialog");
            }
        });

        return navView;
    }
}
