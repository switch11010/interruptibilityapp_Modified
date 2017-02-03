package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;
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

    private SpotAdapter adapter;

    @SuppressWarnings("UnusedDeclaration")
    public static SpotListFragment newInstance(int position) {
        SpotListFragment fragment = new SpotListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POSITION_ARG, position);
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
        Bundle bundle = getArguments();
        int position = bundle.getInt(Constants.POSITION_ARG);
        ((MainActivity) getActivity()).onSectionAttached(position);
        setHasOptionsMenu(true);

        adapter = new SpotAdapter(getActivity(), R.layout.spot_layout);

        setListAdapter(adapter);
    }

    @SuppressWarnings("unchecked")
    private void update(){
        WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();

        List<Spot> spots = helper.allSpot();

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
        FragmentActivity main = getActivity();
        AlertDialog.Builder dialog;
        switch(item.getItemId()) {
            case R.id.action_add:
                FragmentManager fm = main.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.container, RecordFragment.newInstance(-1));
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.action_dump:
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Dump");
                dialog.setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "を\"WIPS\"フォルダに出力します。");
                dialog.setPositiveButton("OK",
                        (dialogInterface, which) ->{
                            WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                            helper.dumpDatabase("WIPS/dumped.db");
                        });
                dialog.setCancelable(true);
                dialog.show();
                break;
            case R.id.action_input:
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Input");
                dialog.setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "に\"dumped.db\"を上書きします。");
                dialog.setPositiveButton("OK",
                        (dialogInterface, which) ->{
                            WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                            helper.inputDBFile(Environment.getExternalStorageDirectory() + "/WIPS/dumped.db");
                        });
                dialog.setCancelable(true);
                dialog.show();
                break;
            case R.id.action_dumpcsv:
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Input");
                dialog.setMessage("\"" + WiPSDBContract.DATABASE_NAME + "\"" + "を\"dumped.csv\"に出力します。");
                dialog.setPositiveButton("OK",
                        (dialogInterface, which)->{
                            WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                            helper.dumpToCsv();
                        });
                dialog.setCancelable(true);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Spot item = (Spot)l.getItemAtPosition(position);
        FragmentActivity activity = getActivity();
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, RecordFragment.newInstance(item.id));
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
        ListView list = getListView();
        list.setOnItemLongClickListener((adapterView, view, i, l)->{
            final Spot spot = (Spot) adapterView.getItemAtPosition(i);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Delete Spot");
            builder.setMessage(spot.name + "を削除します");
            builder.setPositiveButton("OK",
                    (dialogInterface, which)-> {
                        WiPSDBHelper helper = ((MainActivity) getActivity()).getDbHelper();
                        helper.deleteSpot(spot.id);
                        update();
                    });
            builder.setCancelable(true);
            builder.show();
            return true;
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
    @SuppressWarnings("UnusedDeclaration")
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }

}
