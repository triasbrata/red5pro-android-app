package infrared5.com.red5proandroid.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import infrared5.com.red5proandroid.AppState;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.utilities.StreamListUtility;
import infrared5.com.red5proandroid.utilities.SubscribeList;

public class SettingsDialogFragment extends Fragment {

    private AppState state;
    private OnFragmentInteractionListener mListener;
    private SubscribeList streamList;
    private ArrayAdapter adapter;
    private View settingsSubView;
    private View advancedSubView;
    private TextView streamNum;
    private String subSelected;
    public static int defaultResolution = 0;
    public static int bitRate = 1000;
    public boolean advancedOpen;

    public static SettingsDialogFragment newInstance(AppState state) {
        SettingsDialogFragment fragment = new SettingsDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.state = state;
        return fragment;
    }
    public void setSpinnerAdapter(ArrayAdapter list) {
        this.adapter=list;
        defaultResolution=-1;
        boolean has352 = false;
        boolean has160 = false;

        int c = adapter.getCount();
        for(int j=0; j < c; j++){
            String rez = String.valueOf(adapter.getItem(j));
            if("352x288".equals(rez)) {
                defaultResolution=j;//not ideal but...
            }
            else  if("160x120".equals(rez)&& defaultResolution<0) {
              //  defaultResolution=j;//not good enough...
            }
            else  if("176x144".equals(rez)&& defaultResolution<0) {
              //  defaultResolution=j;//not good enough...
            }

            if("320x240".equals(rez)) {
                defaultResolution=j;//perfect
                break;
            }

        }

        Log.d("publisher", "setting default resolution "+defaultResolution);

        if(defaultResolution < 0) {
            defaultResolution=0;
            Log.e("publisher", "no currently supported resolution");
        }
    }

    public SettingsDialogFragment() {

    }

    private EditText getField(View v, int id) {
        return (EditText) v.findViewById(id);
    }

    private String getPreferenceValue(int id) {
        return getResources().getString(id);
    }

    private void saveSettings(View v) {
        SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();

//        EditText app = getField(v, R.id.settings_appname);

//        editor.putString(getPreferenceValue(R.string.preference_app), app.getText().toString());

        if(state == AppState.PUBLISH) {
//            CheckBox cb = (CheckBox)v.findViewById(R.id.settings_audio);
//            CheckBox cbv = (CheckBox)v.findViewById(R.id.settings_video);
//            CheckBox cba = (CheckBox)v.findViewById(R.id.settings_adaptive_bitrate);
            EditText name = getField(v, R.id.settings_streamname);
            editor.putString(getPreferenceValue(R.string.preference_name), name.getText().toString());

            final RadioGroup group = (RadioGroup) v.findViewById(R.id.settings_quality);
            final int checkedID = group.getCheckedRadioButtonId();

            int resolutionWidth = 854;
            int resolutionHeight = 480;
            int selectedQuality = 1;
            bitRate = 1000;

            switch (checkedID) {
                case R.id.settings_quality_low:
                    bitRate = 400;
                    resolutionWidth = 426;
                    resolutionHeight = 240;
                    selectedQuality = 0;
                    break;
                default:
                case R.id.settings_quality_medium:
                    break;
                case R.id.settings_quality_high:
                    bitRate = 4500;
                    resolutionWidth = 1920;
                    resolutionHeight = 1080;
                    selectedQuality = 2;
                    break;
                case R.id.settings_quality_other:
                    bitRate = Publish.config.bitrate;
                    resolutionWidth = Integer.valueOf( Publish.selected_item.split("x")[0] );
                    resolutionHeight = Integer.valueOf( Publish.selected_item.split("x")[1] );
                    selectedQuality = 3;
                    break;
            }

            Log.d("SettingsDialogFragment", "Saving bitRate " + bitRate);
            Log.d("SettingsDialogFragment", "Saving preference_resolutionWidth " + resolutionWidth);
            Log.d("SettingsDialogFragment", "Saving preference_resolutionHeight " + resolutionHeight);
            Log.d("SettingsDialogFragment", "Saving preference_resolutionQuality " + selectedQuality);

            Publish.preferedResolution = selectedQuality;
            Publish.selected_item = resolutionWidth + "x" + resolutionHeight;
            Publish.config.bitrate = bitRate;

            Log.d("SettingsDialogFragment", "Publish.preferedResolution " + selectedQuality);
            Log.d("SettingsDialogFragment", "Publish.selected_item " + resolutionWidth + "x" + resolutionHeight);
            Log.d("SettingsDialogFragment", "Publish.config.bitrate " + bitRate);

            editor.putInt(getPreferenceValue(R.string.preference_bitrate),bitRate);
            editor.putInt(getPreferenceValue(R.string.preference_resolutionWidth), resolutionWidth);
            editor.putInt(getPreferenceValue(R.string.preference_resolutionHeight), resolutionHeight);
            editor.putInt(getPreferenceValue(R.string.preference_resolutionQuality), selectedQuality);

//            editor.putBoolean(getPreferenceValue(R.string.preference_audio), cb.isChecked());
//            editor.putBoolean(getPreferenceValue(R.string.preference_video), cbv.isChecked());
//            editor.putBoolean(getPreferenceValue(R.string.preference_adaptive_bitrate), cba.isChecked());
        }
        else {
            if(subSelected != null)
                editor.putString(getPreferenceValue(R.string.preference_name), subSelected);
        }

        editor.apply();
    }

    private void showUserSettings(View v) {
        SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);

        String defaultHost = preferences.getString(getPreferenceValue(R.string.preference_default_host), null);
        String host = preferences.getString(getPreferenceValue(R.string.preference_host), defaultHost);

        Log.d("Settings", "Host will be " + host);
        Publish.config.host = host;

//        EditText app = getField(v, R.id.settings_appname);

        switch (state) {
            case PUBLISH:
                EditText name = getField(v, R.id.settings_streamname);
                name.setText(preferences.getString(getPreferenceValue(R.string.preference_name), getPreferenceValue(R.string.preference_default_name)));

                RadioGroup group = (RadioGroup) v.findViewById(R.id.settings_quality);

                int quality = preferences.getInt(getPreferenceValue(R.string.preference_resolutionQuality), getResources().getInteger(R.integer.preference_default_resolutionQuality));
                Log.d("SettingsDialogFragment", "Got quality " + quality);

                switch (quality) {
                    case 0:
                        ((RadioButton)group.findViewById(R.id.settings_quality_low)).setChecked(true);
                        Publish.preferedResolution = 0;
                        Publish.selected_item = "426x240";
                        Publish.config.bitrate = 400;
                        break;
                    case 1:
                    default:
                        ((RadioButton)group.findViewById(R.id.settings_quality_medium)).setChecked(true);
                        Publish.preferedResolution = 1;
                        Publish.selected_item = "854x480";
                        Publish.config.bitrate = 1000;
                        break;
                    case 2:
                        ((RadioButton)group.findViewById(R.id.settings_quality_high)).setChecked(true);
                        Publish.preferedResolution = 2;
                        Publish.selected_item = "1920x1080";
                        Publish.config.bitrate = 4500;
                        break;
                    case 3:
                        ((RadioButton)group.findViewById(R.id.settings_quality_other)).setChecked(true);
                        Publish.preferedResolution = 3;
                        Publish.selected_item = preferences.getInt(getResources().getString(R.string.preference_resolutionWidth), getResources().getInteger(R.integer.preference_default_resolutionWidth)) +
                                "x" + preferences.getInt(getResources().getString(R.string.preference_resolutionHeight), getResources().getInteger(R.integer.preference_default_resolutionHeight));// "854x480";
                        Publish.config.bitrate = preferences.getInt(getResources().getString(R.string.preference_bitrate), getResources().getInteger(R.integer.preference_default_bitrate));
                        break;
                }
                break;
            case SUBSCRIBE:
                streamNum = (TextView) v.findViewById(R.id.StreamNum);

                streamList = (SubscribeList) getFragmentManager().findFragmentById(R.id.streamList);
                streamList.mCallbacks = new SubscribeList.Callbacks() {
                    @Override
                    public void onItemSelected(int id) {
                        subSelected = StreamListUtility._liveStreams.get(id);
                    }
                };
                StreamListUtility util = StreamListUtility.get_instance(getActivity());
                util.callWithRunnable(new Runnable() {
                    @Override
                    public void run() {
                        UpdateStreamList();
                    }
                });
                break;
        }
    }

    protected void UpdateStreamList(){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                streamList.connectList();

                if( subSelected != null ){
                    if( !StreamListUtility._liveStreams.contains(subSelected) && advancedSubView.getParent() == null){
                        subSelected = null;
                    }
                    else {
                        streamList.setSelection( StreamListUtility._liveStreams.indexOf(subSelected) );
                    }
                }

                streamNum.setText(StreamListUtility._liveStreams.size() + "Streams");
            }
        });
    }

    private AdapterView.OnItemSelectedListener getItemSelectedHandlerForResolution(){
         return new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 Log.d("publisher"," selected item "+String.valueOf(adapterView.getSelectedItem()+"  i:" +i+"  l :"+l));
                 Publish.selected_item = String.valueOf(adapterView.getSelectedItem());
                 Publish.preferedResolution = adapterView.getSelectedItemPosition();
             }

             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {

             }
         };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_settings_dialog, null, false);

        final DrawerLayout drawer = (DrawerLayout) v;

        settingsSubView = null;
        switch (state) {
            case SUBSCRIBE:
                settingsSubView = View.inflate(inflater.getContext(), R.layout.activity_settings_subscribe, null);
                advancedSubView = inflater.inflate(R.layout.activity_settings_subscribe_advanced, null, false);
                break;
            case PUBLISH:
                settingsSubView = inflater.inflate(R.layout.activity_settings_publish, null, false);
                advancedSubView = inflater.inflate(R.layout.activity_settings_publish_advanced, null, false);
                break;
        }

        ((LinearLayout)v.findViewById(R.id.settings_frame)).addView(settingsSubView);

        //add the nav slide thing here
        View navBtn = (View)v.findViewById(R.id.slideNavBtn);

        v.findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer.isDrawerOpen(Gravity.LEFT))
                    drawer.closeDrawer(Gravity.LEFT);
            }
        });

        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        v.findViewById(R.id.advanced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings(v);
                switchToAdvanced(v.findViewById(R.id.settings_frame));
            }
        });

        final Fragment thisFragment = this;
        TextView subText = (TextView) v.findViewById(R.id.submit);
        subText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View t) {
                if( state == AppState.SUBSCRIBE && subSelected == null)
                    return;

                saveSettings(v);
                mListener.onSettingsDialogClose();

                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }
        });

        showUserSettings(v);
        return v;
    }

    public void switchToAdvanced(View v){

        SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);

        String val;
        //server
        val = preferences.getString(getPreferenceValue(R.string.preference_host), getResources().getString(R.string.preference_default_host));
        getField(advancedSubView, R.id.serverText).setText(val);
        //port
        val = "" + preferences.getInt(getPreferenceValue(R.string.preference_port), getResources().getInteger(R.integer.preference_default_port));
        getField(advancedSubView, R.id.portText).setText(val);
        //app
        val = preferences.getString(getPreferenceValue(R.string.preference_app), getResources().getString(R.string.preference_default_app));
        getField(advancedSubView, R.id.appText).setText(val);
        //debug check
        buildCheckHandle(R.string.preference_debug, R.bool.preference_default_debug, R.id.debugCheck);

        if(state == AppState.PUBLISH) {
            //name
            val = getField(settingsSubView, R.id.settings_streamname).getText().toString();
            getField(advancedSubView, R.id.nameText).setText(val);
            //bitrate
            getField(advancedSubView, R.id.rateText).setText("" + Publish.config.bitrate);
            //resolution
            getField(advancedSubView, R.id.resolutionText).setText(Publish.selected_item);
            //audio check
            buildCheckHandle(R.string.preference_audio, R.bool.preference_default_audio, R.id.audioCheck);
            //video check
            buildCheckHandle(R.string.preference_video, R.bool.preference_default_video, R.id.videoCheck);
            //adaptive check
            buildCheckHandle(R.string.preference_adaptive_bitrate, R.bool.preference_default_adaptive_bitrate, R.id.adaptiveCheck);
        }
        else {
            //name
            val = preferences.getString(getPreferenceValue(R.string.preference_name), getPreferenceValue(R.string.preference_default_name));
            getField(advancedSubView, R.id.nameText).setText(subSelected != null ? subSelected : val);
        }

        // Back
        advancedSubView.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnFromAdvanced();
            }
        });

        final Fragment thisFragment = this;
        // Publish/Subscribe
        advancedSubView.findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveAdvancedSettings();

                saveSettings(settingsSubView);
                mListener.onSettingsDialogClose();

                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }
        });

        ((LinearLayout)v).removeView(settingsSubView);
        ((LinearLayout)v).addView(advancedSubView);

        advancedOpen = true;
    }

    public void buildCheckHandle( final int prefID, final int defaultID, final int viewID ){
        final SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);

        final int checkEmpty = R.drawable.check_visuals;
        final int checkOn = R.drawable.checkbox;

        final ImageButton check = (ImageButton)advancedSubView.findViewById(viewID);
        if( preferences.getBoolean(getPreferenceValue(prefID), getResources().getBoolean(defaultID)) ){
            check.setBackgroundResource(checkOn);
        }

        check.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                if( preferences.getBoolean(getPreferenceValue(prefID), getResources().getBoolean(defaultID)) ){
                    check.setBackgroundResource(checkEmpty);
                    editor.putBoolean(getPreferenceValue(prefID), false);
                }
                else{
                    check.setBackgroundResource(checkOn);
                    editor.putBoolean(getPreferenceValue(prefID), true);
                }
                editor.apply();
            }
        });
    }

    public void returnFromAdvanced(){
        if(!saveAdvancedSettings())
            return;

        advancedOpen = false;

        if(state == AppState.PUBLISH)
            showUserSettings(settingsSubView);
        else{
            subSelected = getField(advancedSubView, R.id.nameText).getText().toString();
            UpdateStreamList();
        }

        LinearLayout parent = (LinearLayout)advancedSubView.getParent();
        parent.removeView(advancedSubView);
        parent.addView(settingsSubView);
    }

    public void forceReturnFromAdvanced(){
        saveAdvancedSettings();

        advancedOpen = false;

        if(state == AppState.PUBLISH)
            showUserSettings(settingsSubView);
        else{
            subSelected = getField(advancedSubView, R.id.nameText).getText().toString();
            UpdateStreamList();
        }

        LinearLayout parent = (LinearLayout)advancedSubView.getParent();
        parent.removeView(advancedSubView);
        parent.addView(settingsSubView);
    }

    public boolean saveAdvancedSettings(){
        SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();

        //server
        editor.putString(getPreferenceValue(R.string.preference_host), getField(advancedSubView, R.id.serverText).getText().toString());
        //port
        editor.putInt(getPreferenceValue(R.string.preference_port), Integer.parseInt(getField(advancedSubView, R.id.portText).getText().toString()));
        //app
        editor.putString(getPreferenceValue(R.string.preference_app), getField(advancedSubView, R.id.appText).getText().toString());
        //name
        editor.putString(getPreferenceValue(R.string.preference_name), getField(advancedSubView, R.id.nameText).getText().toString());

        if( state == AppState.PUBLISH ) {
            //bitrate/resolution
            int baseBitrate = preferences.getInt(getPreferenceValue(R.string.preference_bitrate), getResources().getInteger(R.integer.preference_default_bitrate));
            int newBitrate = Integer.parseInt( getField(advancedSubView, R.id.rateText).getText().toString() );
            String newResolution = getField(advancedSubView, R.id.resolutionText).getText().toString();

            if(baseBitrate != newBitrate || Publish.selected_item != newResolution ) {

                if (newResolution.contains("x")) {
                    String[] bits = newResolution.split("x");
                    try {
                        Integer.parseInt(bits[0]);
                        Integer.parseInt(bits[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        getField(advancedSubView, R.id.resolutionText).setTextColor(Color.RED);

                        editor.apply();

                        return false;
                    }
                    getField(advancedSubView, R.id.resolutionText).setTextColor(Color.BLACK);
                } else {
                    getField(advancedSubView, R.id.resolutionText).setTextColor(Color.RED);

                    editor.apply();

                    return false;
                }
                editor.putInt(getPreferenceValue(R.string.preference_resolutionQuality), 3);
                editor.putInt(getPreferenceValue(R.string.preference_bitrate), newBitrate);
                editor.putInt(getPreferenceValue(R.string.preference_resolutionWidth), Integer.parseInt(newResolution.split("x")[0]));
                editor.putInt(getPreferenceValue(R.string.preference_resolutionHeight), Integer.parseInt(newResolution.split("x")[1]));

                Publish.selected_item = newResolution;
            }
        }

        editor.apply();

        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;

            ImageButton settingsButton = (ImageButton) activity.findViewById(R.id.btnSettings);
            if(settingsButton != null) {
                settingsButton.setImageResource(R.drawable.settings_red);
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        ImageButton settingsButton = (ImageButton) getActivity().findViewById(R.id.btnSettings);
        if(settingsButton != null) {
            settingsButton.setImageResource(R.drawable.settings_grey);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        StreamListUtility.get_instance().clearAndDisconnect();

        if(streamList != null){
            streamList.mCallbacks = null;
        }
    }

    public interface OnFragmentInteractionListener {
        public void onSettingsDialogClose();
    }

}
