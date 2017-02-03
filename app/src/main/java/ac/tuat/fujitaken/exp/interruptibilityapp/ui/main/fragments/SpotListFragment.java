package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.Spot;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.data.SpotAdapter;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.database.WiPSDBContract;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.database.WiPSDBHelper;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SpotListFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters

    private SpotAdapter adapter;

    // TODO: Rename and change types of parameters
    public static SpotListFragment newInstance(int param1) {
        SpotListFragment fragment = new SpotListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SpotListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_PARAM1));
        setHasOptionsMenu(true);

        // TODO: Change Adapter to display your content
        adapter = new SpotAdapter(getActivity(), R.layout.spot_layout);

        setListAdapter(adapter);
    }

    private void update(){
        WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();

        List<Spot> spots =  helper.allSpot();

        adapter.clear();
        adapter.addAll(spots);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.wifi, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, RecordFragment.newInstance(-1))
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.action_dump:
                new AlertDialog.Builder(getActivity()).setTitle("Dump")
                        .setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "を\"WIPS\"フォルダに出力します。")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                                helper.dumpDatabase("WIPS/dumped.db");
                            }
                        })
                        .setCancelable(true)
                        .show();
                break;
            case R.id.action_input:
                new AlertDialog.Builder(getActivity())
                        .setTitle("Input")
                        .setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "に\"dumped.db\"を上書きします。")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                                helper.inputDBFile(Environment.getExternalStorageDirectory() + "/WIPS/dumped.db");
                            }
                        })
                        .setCancelable(true)
                        .show();
                break;
            case R.id.action_dumpcsv:
                new AlertDialog.Builder(getActivity())
                        .setTitle("Input")
                        .setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "を\"dumped.csv\"に出力します。")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                                helper.dumpToCsv();
                            }
                        })
                        .setCancelable(true)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Spot item = (Spot)l.getItemAtPosition(position);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, RecordFragment.newInstance(item.id))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final Spot spot = (Spot) adapterView.getItemAtPosition(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Spot");
                builder.setMessage(spot.name + "を削除します");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                        helper.deleteSpot(spot.id);
                        update();
                    }
                });
                builder.setCancelable(true);
                builder.show();
                return true;
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
