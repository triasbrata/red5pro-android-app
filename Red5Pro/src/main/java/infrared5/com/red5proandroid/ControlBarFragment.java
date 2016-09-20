package infrared5.com.red5proandroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import infrared5.com.red5proandroid.publish.Publish;
import infrared5.com.red5proandroid.subscribe.Subscribe;

public class ControlBarFragment extends Fragment {

    private AppState currentState = null;

    private OnFragmentInteractionListener mListener;

    public ControlBarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control_bar, container, false);

        ImageButton button = (ImageButton) v.findViewById(R.id.btnSettings);
        if(button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSettingsClick();
                }
            });
        }

//        button = (ImageButton) v.findViewById(R.id.btnRecord);
//        if(button != null) {
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                }
//            });
//        }
//
//        button = (ImageButton) v.findViewById(R.id.btnCamera);
//        if(button != null) {
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                }
//            });
//        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean navigate(AppState state) {
        if(setSelection(state)) {
            Intent intent = null;
            switch (state) {
                case PUBLISH:
                    intent = new Intent(getActivity(), Publish.class);
                    break;
                case SUBSCRIBE:
                    intent = new Intent(getActivity(), Subscribe.class);
                    break;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        return false;
    }

    private void unselect(int id, int drawable) {
        ImageButton button = (ImageButton) getActivity().findViewById(id);
        button.setImageResource(drawable);
        button.invalidate();
    }

    public boolean setSelection(AppState state) {
        if(currentState != null && currentState.equals(state)) {
            return false;
        }

//        int id = 0;
//        int drawable = 0;
//        switch (state) {
//            case PUBLISH:
//                id = R.id.btnPublish;
//                drawable = R.drawable.publish_grey;
//                unselect(R.id.btnSubscribe, R.drawable.subscribe);
//                break;
//            case SUBSCRIBE:
//                id = R.id.btnSubscribe;
//                drawable = R.drawable.subscribe_grey;
//                unselect(R.id.btnPublish, R.drawable.publish);
//                break;
//        }
//
//        ImageButton myButton = (ImageButton) getActivity().findViewById(id);
//        if (myButton != null) {
//            myButton.setImageResource(drawable);
//            myButton.invalidate();
//        }
        currentState = state;
        return true;
    }

    public void displayPublishControls(boolean show) {
//        ImageButton recordButton = (ImageButton) getActivity().findViewById(R.id.btnRecord);
        ImageButton cameraButton = (ImageButton) getActivity().findViewById(R.id.btnCamera);
//        if(recordButton != null) {
//            recordButton.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
        if(cameraButton != null) {
            cameraButton.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public interface OnFragmentInteractionListener {
        public void onSettingsClick();
    }

}
