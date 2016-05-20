package infrared5.com.red5proandroid.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import infrared5.com.red5proandroid.AppState;
import infrared5.com.red5proandroid.R;
import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.subscribe.Subscribe;

public class SettingsDialogFragment extends DialogFragment {

    private AppState state;
    private OnFragmentInteractionListener mListener;
    private ArrayAdapter adapter;
    public static int defaultResolution = 0;
    public static int bitRate = 1000;

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

        EditText app = getField(v, R.id.settings_appname);
        EditText name = getField(v, R.id.settings_streamname);

        editor.putString(getPreferenceValue(R.string.preference_app), app.getText().toString());
        editor.putString(getPreferenceValue(R.string.preference_name), name.getText().toString());

        if(state == AppState.PUBLISH) {
            CheckBox cb = (CheckBox)v.findViewById(R.id.settings_audio);
            CheckBox cbv = (CheckBox)v.findViewById(R.id.settings_video);
            CheckBox cba = (CheckBox)v.findViewById(R.id.settings_adaptive_bitrate);

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
                case R.id.settings_quality_medium:
                    break;
                case R.id.settings_quality_high:
                    bitRate = 4500;
                    resolutionWidth = 1920;
                    resolutionHeight = 1080;
                    selectedQuality = 2;
                    break;
                default:
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

            editor.putBoolean(getPreferenceValue(R.string.preference_audio), cb.isChecked());
            editor.putBoolean(getPreferenceValue(R.string.preference_video), cbv.isChecked());
            editor.putBoolean(getPreferenceValue(R.string.preference_adaptive_bitrate), cba.isChecked());
        }

        editor.apply();
    }

    private void showUserSettings(View v) {
        SharedPreferences preferences = getActivity().getSharedPreferences(getPreferenceValue(R.string.preference_file), Activity.MODE_MULTI_PROCESS);

        String defaultHost = preferences.getString(getPreferenceValue(R.string.preference_default_host), null);
        String host = preferences.getString(getPreferenceValue(R.string.preference_host), defaultHost);

        Log.d("Settings", "Host will be " + host);
        Publish.config.host = host;

        EditText app = getField(v, R.id.settings_appname);
        EditText name = getField(v, R.id.settings_streamname);

        final RadioGroup group = (RadioGroup) v.findViewById(R.id.settings_quality);

        int quality = preferences.getInt(getResources().getString(R.string.preference_resolutionQuality), 1);
        Log.d("SettingsDialogFragment", "Got quality " + quality);

        switch (quality) {
            case 0:
                ((RadioButton)group.findViewById(R.id.settings_quality_low)).setChecked(true);
                Publish.preferedResolution = 0;
                Publish.selected_item = "426x240";
                Publish.config.bitrate = 400;
                break;
            case 1:
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
            default:
                ((RadioButton)group.findViewById(R.id.settings_quality_medium)).setChecked(true);
                Publish.preferedResolution = 1;
                Publish.selected_item = "854x480";
                Publish.config.bitrate = 1000;
                break;
        }

        name.setText(preferences.getString(getPreferenceValue(R.string.preference_name), getPreferenceValue(R.string.preference_default_name)));

        switch (state) {
            case PUBLISH:
            case SUBSCRIBE:
                app.setText(preferences.getString(getPreferenceValue(R.string.preference_app), getPreferenceValue(R.string.preference_default_app)));
                break;
        }
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.fragment_settings_dialog, null, false);

        ViewGroup streamSettings = (ViewGroup) v.findViewById(R.id.subscribe_settings);
        ViewGroup publishSettings = (ViewGroup) v.findViewById(R.id.publishing_settings);

        switch (state) {
            case SUBSCRIBE:
                publishSettings.setVisibility(View.GONE);
                break;
        }

        ContextThemeWrapper ctx = new ContextThemeWrapper(getActivity(), R.style.AppTheme );
        AlertDialog dialog =  new AlertDialog.Builder(ctx)
                                    .setView(v)
                                    .setPositiveButton("DONE", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {
                                            saveSettings(v);
                                            mListener.onSettingsDialogClose();
                                            dialog.cancel();
                                        }

                                    })
                                    .create();

        showUserSettings(v);
        return dialog;
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

    public interface OnFragmentInteractionListener {
        public void onSettingsDialogClose();
    }

}
