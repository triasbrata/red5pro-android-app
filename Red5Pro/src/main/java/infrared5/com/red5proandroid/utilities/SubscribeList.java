package infrared5.com.red5proandroid.utilities;

import android.app.ListFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by davidHeimann on 5/27/16.
 */
public class SubscribeList extends ListFragment {

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int id);
    }

    public Callbacks mCallbacks = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        listView.setBackgroundColor(Color.WHITE);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        listView.setDivider(new ColorDrawable(Color.DKGRAY));
        listView.setDividerHeight(1);
    }

    public void connectList(){

        setListAdapter(new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                StreamListUtility._liveStreams
        ));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(mCallbacks != null){
            mCallbacks.onItemSelected(position);
        }
    }
}
