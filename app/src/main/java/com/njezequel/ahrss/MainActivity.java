package com.njezequel.ahrss;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.debug.hv.ViewServer;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MainActivity";

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // TODO: clean layout files (margin padding)

        // Adding rss list fragment to the frame layout
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
            .add(R.id.main_activity_fragment_container, new RssListFragment())
            .commit();

        // ActionBar init
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.main_activity_title);

        // Fragment change listener
        mFragmentManager.addOnBackStackChangedListener(
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    // Show back button on details fragment and hide on list fragment
                    ActionBar bar = getSupportActionBar();
                    if (bar != null)
                        if (mFragmentManager.getBackStackEntryCount() > 0) {
                            // RssListFragment is on the stack
                            bar.setHomeButtonEnabled(true);
                            bar.setDisplayHomeAsUpEnabled(true);
                        } else {
                            bar.setDisplayHomeAsUpEnabled(false);
                            bar.setHomeButtonEnabled(false);
                        }
                }
            }
        );

        // In order to run Hierarchy Viewver on a non developper phone
        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        MenuItem item = menu.add(Menu.NONE, 0, Menu.NONE, "t");
//        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
//        item.setIcon(android.R.drawable.ic_btn_speak_now);
//
//        MenuItem item2 = menu.add(Menu.NONE, 1, Menu.NONE, "ttt");
//        MenuItemCompat.setShowAsAction(item2, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
