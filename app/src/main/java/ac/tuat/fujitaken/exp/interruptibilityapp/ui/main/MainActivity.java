package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.receiver.wifi.database.WiPSDBHelper;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.ItemFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.NavigationDrawerFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SpotListFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments.SettingFragment_Ex;  //s 追加


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        SpotListFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private WiPSDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment) fm.findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        dbHelper = new WiPSDBHelper(getApplicationContext());

        check();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    /**
     * 画面を追加するときはここでフラグメントを作成
     * @param position 表示順番
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // run the wifi content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft;
        if(fragmentManager.getBackStackEntryCount() > 0){
            fragmentManager.popBackStack();
        }

        if(position == 0){
            ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, SettingFragment.newInstance(position + 1));
            ft.commit();
        } else if (position == 1) {
            ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, ItemFragment.newInstance(position + 1));
            ft.commit();
        }
        /*
        else if (position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ItemFragment.newInstance(position + 1, true))
                    .commit();
        }
        else if (position == 3) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, MatchingFragment.newInstance(position + 1))
                    .commit();
        }
        else if (position == 4) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SpotListFragment.newInstance(position + 1))
                    .commit();

        }
        */
        else if (position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SettingFragment_Ex.newInstance(position + 1))
                    .commit();
        }
    }

    /**
     * 画面を追加するときはここに名前を追記
     * @param position 表示される順番
     */
    public void onSectionAttached(int position) {
        if(SettingFragment.isServiceActive(getApplicationContext())) {
            return;
        }
        switch (position) {

            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
                /*
                case 3:
                    mTitle = getString(R.string.title_section2);
                    break;
                case 4:
                    mTitle = getString(R.string.title_section4);
                    break;
                case 5:
                    mTitle = getString(R.string.title_section5);
                    break;
                 */
            case 3:
                mTitle = getString(R.string.title_section10);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                return;
            }
            Bundle bundle = getArguments();
            int section = bundle.getInt(ARG_SECTION_NUMBER);
            ((MainActivity) activity).onSectionAttached(section);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Bundle bundle = getArguments();
            int section = bundle.getInt(ARG_SECTION_NUMBER);
            FragmentActivity activity = getActivity();
            ((MainActivity) activity).onSectionAttached(section);
        }
    }

    private boolean check(String permission){
        return PermissionChecker.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED;
    }

    private static final int PERMISSION_REQUEST = 53;

    public void check(){
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE};
        List<String> p = new ArrayList<>();

        for(String s: permissions){
            if(check(s)) {
                p.add(s);
            }
        }

        if(p.size() > 0) {
            String[] authenticatedPermissions = p.toArray(new String[p.size()]);
            ActivityCompat.requestPermissions(this, authenticatedPermissions, PERMISSION_REQUEST);
        }
        else{
            checkUsage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST){
            checkUsage();
        }
    }

    private void checkUsage(){

        if(!ac.tuat.fujitaken.exp.interruptibilityapp.data.settings.Settings.getDeviceSettings().isUsagePermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    public WiPSDBHelper getDbHelper() {
        return dbHelper;
    }
}
