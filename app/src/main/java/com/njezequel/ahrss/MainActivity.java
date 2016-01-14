package com.njezequel.ahrss;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.debug.hv.ViewServer;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MainActivity";

    private View mCoordinatorView;
    private FragmentManager mFragmentManager;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // TODO: clean layout files (margin padding)

        mCoordinatorView = findViewById(R.id.root_coordinator);

        // Adding rss list fragment to the frame layout
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
            .add(R.id.main_activity_fragment_container, new RssListFragment())
            .commit();

        // TODO: init functions

        // ActionBar init
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.main_activity_title);
        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        // Fragment change listener
        mFragmentManager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (mFragmentManager.getBackStackEntryCount() > 0) {
                            // RssListFragment is on the stack
                            // Disabling drawer toggle icon
                            mDrawerToggle.setDrawerIndicatorEnabled(false);

                        } else {
                            mDrawerToggle.setDrawerIndicatorEnabled(true);
                        }
                    }
                }
        );

        // Navigation drawer init
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Navigation item listener
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        mDrawerLayout.closeDrawer(mNavigationView);
                        return true;
                    }
                }
        );

        // Drawer swipe listener
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Navigation back icon listener
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // In order to run Hierarchy Viewver on a non developer phone
        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred
        // and set the material design icon
        mDrawerToggle.syncState();
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
    public void onConfigurationChanged(Configuration newConfig) {
        // Call when android:configChanges="orientation|screenSize" is assigned to the actvity
        // Then, when the orientation changes, the activity is not destroyed
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * mCoordinator getter
     *
     * @return mCoordinatorView
     */
    public View getCoordinatorLayout() {
        return mCoordinatorView;
    }
}
