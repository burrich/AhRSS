package com.njezequel.ahrss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation de la listview de RssListFragment

        ListFragment rssListFragment = (ListFragment) getSupportFragmentManager().findFragmentById(
                R.id.rss_list_fragment
        );
        List<Map<String, String>> data = RssListFragment.generateData();

        // Mapping clés / view des éléments d'une ligne
        String[] viewsKeys = {"1", "2"};
        int[] views = {R.id.text1_rss_list, R.id.text2_rss_list};

        // Adapter
        SimpleAdapter rssListAdapter = new SimpleAdapter(
                this, data, R.layout.item_rss_list, viewsKeys, views
        );
        rssListFragment.setListAdapter(rssListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
